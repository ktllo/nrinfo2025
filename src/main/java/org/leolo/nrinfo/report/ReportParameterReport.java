package org.leolo.nrinfo.report;

import org.leolo.nrinfo.model.report.GeneratedReport;
import org.leolo.nrinfo.model.report.Report;
import org.leolo.nrinfo.model.report.ReportParameters;
import org.leolo.nrinfo.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Component
public class ReportParameterReport extends SpecialReportProvider{

    private static Logger logger = LoggerFactory.getLogger(ReportParameterReport.class);

    @Autowired private ReportService reportService;

    @Override
    public GeneratedReport generateReport(List<ReportParameters> parameters) {
        logger.info("Generating report for " + parameters.size() + " parameters");
        GeneratedReport report = new GeneratedReport();
        report.setMimeType("text/html");
        try (
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PrintWriter out = new PrintWriter(outputStream)
        ) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<body>");
            out.println("<h1>Report Parameters Report</h1>");
            List<Report> reports = reportService.getAllReports();
            out.println("</body>");
            out.println("</html>");
            out.flush();
            report.setData(outputStream.toByteArray());
        } catch (IOException e) {
            logger.error("Error while generating report {}", e.getMessage(), e);
        }
        return report;
    }
}
