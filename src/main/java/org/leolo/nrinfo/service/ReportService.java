package org.leolo.nrinfo.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.dao.ReportDao;
import org.leolo.nrinfo.model.report.*;
import org.leolo.nrinfo.report.OutputTransformer;
import org.leolo.nrinfo.report.ReportProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class ReportService {

    @Autowired private CurrentUserService currentUserService;
    @Autowired private UserPermissionService userPermissionService;
    @Autowired private ReportDao reportDao;
    @Autowired private ApplicationContext applicationContext;
    @Autowired private TiplocService tiplocService;

    private Logger logger = LoggerFactory.getLogger(ReportService.class);

    public List<Report> getReportForUser() {
        if (!currentUserService.isAuthenticated()) {
            return List.of();
        }
        try {
            List<Report> reportList;
            if (userPermissionService.hasPermission(Constants.Permission.GENERATE_ANY_REPORTS)) {
                reportList = reportDao.getAllReports();
            } else {
                reportList = reportDao.getReportsForUser(currentUserService.getUserId());
            }
            reportList.sort(Report.reportComparator);
            return reportList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ReportGroup> getReportGroupsForUser() {
        if (!currentUserService.isAuthenticated()) {
            return List.of();
        }
        try {
            List<ReportGroup> reportGroupList;
            if (userPermissionService.hasPermission(Constants.Permission.GENERATE_ANY_REPORTS)) {
                reportGroupList = reportDao.getReportGroups();
            } else {
                reportGroupList = reportDao.getReportGroupsForUser(currentUserService.getUserId());
            }
            reportGroupList.sort(Comparator.comparing(ReportGroup::reportGroupName));
            for (ReportGroup reportGroup : reportGroupList) {
                reportGroup.reports().sort(Report.reportComparator);
            }
            return reportGroupList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkUserPermissionForReport(String reportId) {
        if (!currentUserService.isAuthenticated()) {
            return false;
        }
        if (userPermissionService.hasPermission(Constants.Permission.GENERATE_ANY_REPORTS)) {
            //Short circuit check
            return true;
        }
        try {
            return reportDao.checkUserPermissionForReport(reportId, currentUserService.getUserId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Report getReportByReportId(String reportId) {
        if (!checkUserPermissionForReport(reportId)) {
            return null;
        }
        try {
            return reportDao.getReportByReportId(reportId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validateRequest(List<ReportParameters> parameters) {
        boolean valid = true;
        TreeMap<String, ReportParameters> pairCheckMap = new TreeMap<>();
        //Stage 1: Are all required field filled in?
        for (ReportParameters parameter: parameters) {
            if (parameter.isRequired()) {
                String value = parameter.getParameterValue();
                if (value == null || value.isBlank()) {
                    valid = false;
                    parameter.setValidationMessage(parameter.getParameterName()+" is required");
                    logger.info("[VALIDATION FAILED] Report {} - Parameter {} is required", parameter.getReportId(), parameter.getParameterName());
                }
            }
            if (parameter.getValidationGroup()!=null) {
                String validationGroup = parameter.getValidationGroup();
                if (!pairCheckMap.containsKey(validationGroup)) {
                    pairCheckMap.put(validationGroup, parameter);
                } else {
                    logger.error("Duplicated validation group for Report {} - {}", parameter.getReportId(), parameter.getValidationGroup());
                }
            }
        }
        //Stage 2: Check paired logic
        HashSet<String> validatedGroups = new HashSet<>();
        for (String groupMember : pairCheckMap.keySet()) {
            if (validatedGroups.contains(groupMember)) {
                //This one already validated
                continue;
            }
            ReportParameters mainParameter = pairCheckMap.get(groupMember);
            if ("DATE_RANGE".equals(mainParameter.getParameterType())) {
                //Validating date range
                String mainGroup = mainParameter.getValidationGroup();
                String groupName = mainGroup.substring(0, mainGroup.length() - 1);
                if (!pairCheckMap.containsKey(groupName + "1") || !pairCheckMap.containsKey(groupName + "2")) {
                    logger.warn("[ERROR] Report {} - Validation group {} is not valid", mainParameter.getReportId(), groupName);
                    continue;
                }
                String strStartDate = pairCheckMap.get(groupName + "1").getParameterValue();
                String strEndDate = pairCheckMap.get(groupName + "2").getParameterValue();
                if (strStartDate == null) {
                    strStartDate = "";
                }
                if (strEndDate == null) {
                    strEndDate = "";
                }
                if (strStartDate.isBlank() && strEndDate.isBlank()) {
                    //Empty value, OK
                    validatedGroups.add(groupName + "1");
                    validatedGroups.add(groupName + "2");
                    continue;
                } else if (strStartDate.isBlank()) {
                    pairCheckMap.get(groupName + "1").setValidationMessage("The start date cannot be empty if end date is specified");
                    validatedGroups.add(groupName + "1");
                    validatedGroups.add(groupName + "2");
                    valid = false;
                    continue;
                } else if (strEndDate.isBlank()) {
                    pairCheckMap.get(groupName + "2").setValidationMessage("The end date cannot be empty if start date is specified");
                    validatedGroups.add(groupName + "1");
                    validatedGroups.add(groupName + "2");
                    valid = false;
                    continue;
                }
                ZonedDateTime startDate = LocalDate.parse(strStartDate).atStartOfDay(ZoneId.systemDefault());
                ZonedDateTime endDate = LocalDate.parse(strEndDate).atStartOfDay(ZoneId.systemDefault());
                if (startDate.isAfter(endDate)) {
                    pairCheckMap.get(groupName + "1").setValidationMessage("The start date cannot be after the end date");
                    validatedGroups.add(groupName + "1");
                    validatedGroups.add(groupName + "2");
                    valid = false;
                }
            } else {
                logger.warn("Validation group {} contains an unknown type {}", groupMember, mainParameter.getParameterType());
            }
        }
        logger.debug("Validation results : {}", valid);
        return valid;
    }

    public static final int GEN_TIME_START = 0;
    public static final int GEN_TIME_START_EXEC = 1;
    public static final int GEN_TIME_EXEC_DONE = 2;
    public static final int GEN_TIME_TRANSFORM_DONE = 3;
    public static final int GEN_TIME_OUTPUT_DONE = 4;

    public GeneratedReport getGeneratedReportByReportId(String reportId, List<ReportParameters> parameters, String format) {
        long[]  timestamps = new long[5];
        timestamps[GEN_TIME_START] = System.currentTimeMillis();
        try {
            Report report = reportDao.getReportByReportId(reportId);
            //Verify that given parameter are corrects and for this report
            for (ReportParameters rp: parameters) {
                if (!rp.getReportId().equals(reportId)) {
                    throw new IllegalArgumentException("Report ID " + reportId + " does not match Report ID " + rp.getReportId());
                }
            }
            ExecutionResult executionResult;
            if ("sp".equalsIgnoreCase(report.reportMode())) {
                timestamps[GEN_TIME_START_EXEC] = System.currentTimeMillis();
                executionResult = reportDao.executeReport(report, parameters);
                timestamps[GEN_TIME_EXEC_DONE] = System.currentTimeMillis();
            } else if ("java".equalsIgnoreCase(report.reportMode())) {
                Class<?> reportClass;
                try {
                    reportClass = Class.forName(report.reportExecName());
                    if (!ReportProvider.class.isAssignableFrom(reportClass)) {
                        throw new IllegalArgumentException("Report class " + reportClass.getName() + " is not a subclass of ReportProvider");
                    }
                    ReportProvider reportProvider = (ReportProvider) applicationContext.getBean(reportClass);
                    timestamps[GEN_TIME_START_EXEC] = System.currentTimeMillis();
                    executionResult = reportProvider.executeReport(parameters);
                    timestamps[GEN_TIME_EXEC_DONE] = System.currentTimeMillis();
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Report class " + report.reportExecName() + " does not exist");
                } catch (Exception e) {
                    logger.warn("[ERROR] Report {} - {}", reportId, e.getMessage());
                    throw new RuntimeException(e);
                }
            } else {
                throw new IllegalArgumentException("Report mode " + report.reportMode() + " is not supported");
            }
            logger.debug("Response size: {}r {}c", executionResult.getRowCount(), executionResult.getColCount());
            ReportOutputTransformList transformList = reportDao.getReportOutputTransform(reportId);
            Map<String, OutputTransformer> transformerMap = new HashMap<>();
            for (ReportOutputTransform rot: transformList.stream().toList()) {
                String className = rot.transformClass();
                if (!transformerMap.containsKey(className)) {
                    try {
                        Class<?> transformClass = Class.forName(className);
                        if (!OutputTransformer.class.isAssignableFrom(transformClass)) {
                            throw new IllegalArgumentException("Transformer class " + className + " is not a subclass of OutputTransformer");
                        }
                        OutputTransformer transformer = (OutputTransformer) transformClass.getDeclaredConstructor().newInstance();
                        transformerMap.put(className, transformer);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalArgumentException("Transform class " + className + " does not exist");
                    } catch (InvocationTargetException|InstantiationException|IllegalAccessException|NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            for (int col=0; col<executionResult.getRowCount(); col++) {
                //Index in database are 1-indexed
                List<ReportOutputTransform> transforms = transformList.getTransformsForPosition(col+1);
                if (transforms != null && !transforms.isEmpty()) {
                    for (ReportOutputTransform transform: transforms) {
                        //Transform header
                        OutputTransformer transformer = transformerMap.get(transform.transformClass());
                        if (transformer == null) {
                            throw new IllegalArgumentException("Transformer class " + transform.transformClass() + " does not exist");
                        }
                        executionResult.setHeader(col, transformer.transformTitle(format, executionResult.getHeader(col)));
                        //Transform Body
                        for (int row=0; row<executionResult.getRowCount(); row++) {
                            executionResult.setData(row, col, transformer.transformData(format, executionResult.getData(row, col)));
                            if ("html".equalsIgnoreCase(format)) {
                                executionResult.appendCssClass(row, col, transformer.getHtmlCssClass(executionResult.getOriginalRow(row).get(col)));
                            }
                        }
                    }
                }
            }
            timestamps[GEN_TIME_TRANSFORM_DONE] = System.currentTimeMillis();
            GeneratedReport generatedReport = new GeneratedReport();
            if ("csv".equalsIgnoreCase(format)) {
                generatedReport.setMimeType("text/csv");
                generatedReport.setFileName(reportId+".csv");
                try (
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        OutputStreamWriter osw = new OutputStreamWriter(outputStream);
                        CSVPrinter out = new CSVPrinter(osw, CSVFormat.RFC4180)
                ) {
                    out.printRecord(executionResult.getHeader());
                    for (List<String> row: executionResult.getData()) {
                        out.printRecord(row);
                    }
                    out.flush();
                    generatedReport.setData(outputStream.toByteArray());
                } catch (IOException e) {
                    logger.error("Error while generating report {}", e.getMessage(), e);
                }
            } else if ("html".equalsIgnoreCase(format)) {
                generatedReport.setMimeType("text/html");
                //No file name as this is designed for direct display
                try (
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        PrintWriter out = new PrintWriter(outputStream)
                ) {
                    out.println("<!DOCTYPE html>");
                    out.println("<html>");
                    out.println("<head>");
                    out.println("  <meta http-equiv=\"Content-Type\" content=\"text/html\">");
                    out.println(String.format("  <title>%s</title>", reportId));
                    if (report.htmlStyle() != null && !report.htmlStyle().isBlank()) {
                        //TODO: Add space before each line
                        out.println("  <style>");
                        for (String line: report.htmlStyle().split("\n")) {
                            out.print("    ");
                            out.println(line.replace("\r", ""));
                        }
                        out.println("  </style>");
                    }
                    out.println("</head>");
                    out.println("<body>");
                    out.println(String.format("  <h1 style='text-align:center'>%s - %s</h1>", reportId, report.reportName()));
                    out.println("  <h2 class='para-h2'>Generation Parameters</h2>");
                    out.println("  <table class='para-table' style='border-collapse: collapse;'>");
                    for (ReportParameters rp: parameters) {
                        String oddEven = (rp.getPosition()%2) == 0 ? "even":"odd";
                        if ("station".equalsIgnoreCase(rp.getParameterType())) {
                            out.println(String.format("    <tr class='para-tr-%s'><th class='para-th-%04X'>%s</th><td class='para-td-%04X'>%s</td></tr>",
                                    oddEven, rp.getPosition(), rp.getParameterName(), rp.getPosition(), rp.getDisplayedValue()));
                        } else {
                            out.println(String.format("    <tr class='para-tr-%s'><th class='para-th-%04X'>%s</th><td class='para-td-%04X'>%s</td></tr>",
                                    oddEven, rp.getPosition(), rp.getParameterName(), rp.getPosition(), rp.getParameterValue()));
                        }
                    }
                    out.println(String.format("    <tr><th>Generation Date:</th><td>%s</td></tr>",
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                    ));
                    out.println("</table>");
                    out.println("  <hr>");
                    out.println("  <h2 class='data-h2'>Report Body</h2>");
                    if (executionResult.getRowCount() == 0) {
                        out.println("  <div style='width:100%; font-weight:bold; text-align:center;' class='para-nodata'>** No rows found **</div>");
                    } else {
                        out.println("  <table class='data-table' style='border-collapse: collapse;'>");
                        out.println("    <thead>");
                        printHtmlTableRow(out, executionResult.getHeader(), "th", "data-tr-header", "data-th-%04X");
                        out.println("    </thead>");
                        out.println("    <tbody>");
                        for (int rowNum = 0; rowNum < executionResult.getData().size(); rowNum++) {
                            List<String> row = executionResult.getData().get(rowNum);
                            List<String> css = executionResult.getCssClass().get(rowNum);
                            String oddEven = rowNum%2 == 0 ? "even":"odd";
                            printHtmlTableRow(out, row, "td", "data-tr-"+oddEven, "data-td-%04X", executionResult.getCssClass().get(rowNum));
                            if (rowNum % 100 == 0) {
                                out.flush();
                            }
                        }
                        out.println("    <tbody>");
                        out.println("  </table>");
                    }

                    out.println("  <hr>");
                    out.println("  <div style='width:100%; font-weight:bold; text-align:center;' class='footer'>** End of report **</div>");
                    out.println("</body>");
                    out.println("</html>");
                    out.flush();
                    generatedReport.setData(outputStream.toByteArray());
                } catch (IOException e) {
                    logger.error("Error while generating report {}", e.getMessage(), e);
                }
            } else {
                throw new IllegalArgumentException("Format " + format + " is not supported");
            }
            timestamps[GEN_TIME_OUTPUT_DONE] = System.currentTimeMillis();
            logger.info("[GENTIME] Report {} - row {} col {}; Preexec: {}; Exec:{}; Transform: {}; Output: {}; Total: {}",
                    reportId,
                    executionResult.getRowCount(),
                    executionResult.getColCount(),
                    timestamps[GEN_TIME_START_EXEC] - timestamps[GEN_TIME_START],
                    timestamps[GEN_TIME_EXEC_DONE] - timestamps[GEN_TIME_START_EXEC],
                    timestamps[GEN_TIME_TRANSFORM_DONE] - timestamps[GEN_TIME_EXEC_DONE],
                    timestamps[GEN_TIME_OUTPUT_DONE] - timestamps[GEN_TIME_TRANSFORM_DONE],
                    timestamps[GEN_TIME_OUTPUT_DONE] - timestamps[GEN_TIME_START]);
            return generatedReport;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void printHtmlTableRow(PrintWriter out,List<String> row, String cellType, String rowClass, String cellClassPattern) {
        printHtmlTableRow(out, row, cellType, rowClass, cellClassPattern, null);
    }

    private void printHtmlTableRow(PrintWriter out,List<String> row, String cellType, String rowClass, String cellClassPattern, List<String> additionalClassList) {
        out.println("      <tr class='"+rowClass+"'>");
        for (int i = 0; i < row.size(); i++) {
            String cell = row.get(i);
            String additionalClass = additionalClassList == null ? "" : additionalClassList.get(i);
            if (additionalClass == null || additionalClass.isBlank()) {
                out.print(String.format("        <%s class='%s'>", cellType, String.format(cellClassPattern, i)));
            } else {
//                logger.debug("Additional class {}", additionalClass);
                out.print(String.format("        <%s class='%s %s'>", cellType, String.format(cellClassPattern, i), additionalClass));
            }
            if (cell == null || cell.isBlank()) {
                out.print("&nbsp;");
            } else {
                out.print(cell);
            }
            out.println("</"+cellType+">");
        }
        out.println("      </tr>");
    }



    public List<ReportParameters> getReportParametersForReport(String reportId) {
        try {
            return reportDao.getReportParametersForReport(reportId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}


