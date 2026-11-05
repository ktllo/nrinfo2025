package org.leolo.nrinfo.controller.web;

import jakarta.servlet.http.HttpServletResponse;
import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.model.report.GeneratedReport;
import org.leolo.nrinfo.model.report.Report;
import org.leolo.nrinfo.model.report.ReportGroup;
import org.leolo.nrinfo.model.report.ReportParameters;
import org.leolo.nrinfo.service.CurrentUserService;
import org.leolo.nrinfo.service.ReportService;
import org.leolo.nrinfo.service.UserPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class WebReportGenerationController {

    @Autowired private UserPermissionService userPermissionService;
    @Autowired private CurrentUserService currentUserService;
    @Autowired private ReportService reportService;

    private Logger logger = LoggerFactory.getLogger(WebReportGenerationController.class);

    @GetMapping("/report")
    public String getReportList(Model model, RedirectAttributes redirectAttributes) {
        if (!currentUserService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute(Constants.Model.LOGIN_REDIRECT_DESTINATION, "/report");
            return "redirect:/login";
        }
        if (!userPermissionService.hasPermission(Constants.Permission.GENERATE_REPORTS)) {
            redirectAttributes.addFlashAttribute(Constants.Model.GENERIC_POPUP_MESSAGE, "You do not have permission to view this page");
            return "redirect:/";
        }
        List<ReportGroup> reports = reportService.getReportGroupsForUser();
        model.addAttribute("report_groups", reports);
        return "reports/list";
    }

    @GetMapping("/report/{reportId}")
    public String getReport(Model model, RedirectAttributes redirectAttributes, @PathVariable String reportId) {
        if (!currentUserService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute(Constants.Model.LOGIN_REDIRECT_DESTINATION, "/report/" + reportId);
            return "redirect:/login";
        }
        if (!userPermissionService.hasPermission(Constants.Permission.GENERATE_REPORTS)) {
            redirectAttributes.addFlashAttribute(Constants.Model.GENERIC_POPUP_MESSAGE, "You do not have permission to view this page");
            return "redirect:/";
        }
        //Check does the user have access to this report
        if (!reportService.checkUserPermissionForReport(reportId)) {
            redirectAttributes.addFlashAttribute(Constants.Model.GENERIC_POPUP_MESSAGE, "You do not have permission to view this page");
            return "redirect:/";
        }
        Report report = reportService.getReportByReportId(reportId);
        model.addAttribute("report", report);
        List<ReportParameters> rp = reportService.getReportParametersForReport(reportId);
        logger.debug("Number of report parameters: {}" , rp.size());
        model.addAttribute("parameters", rp);
        model.addAttribute("isSpecialReport", "special".equalsIgnoreCase(report.reportMode()));
        return "reports/input";
    }

    @PostMapping("/report/{reportId}")
    public ModelAndView generateReport(
            RedirectAttributes redirectAttributes,
            HttpServletResponse response,
            @PathVariable String reportId,
            @RequestParam Map<String, String> parameters
    ) {

        if (!currentUserService.isAuthenticated()) {
            redirectAttributes.addFlashAttribute(Constants.Model.LOGIN_REDIRECT_DESTINATION, "/report/" + reportId);
            return new ModelAndView("redirect:/login");
        }
        if (
                !userPermissionService.hasPermission(Constants.Permission.GENERATE_REPORTS) ||
                        !reportService.checkUserPermissionForReport(reportId)
        ) {
            redirectAttributes.addFlashAttribute(Constants.Model.GENERIC_POPUP_MESSAGE, "You do not have permission to view this page");
            return new ModelAndView("redirect:/");
        }
        logger.info("Generating report for report: {}", reportId);
        Report report = reportService.getReportByReportId(reportId);
        boolean needReenterData = false;
        List<ReportParameters> reportParameters = reportService.getReportParametersForReport(reportId);
        String reportFormat = parameters.get("format");
        //Attempt to fill input parameter map
        for (ReportParameters rp : reportParameters) {
            if (parameters.containsKey("rep-"+Integer.toString(rp.getPosition()))) {
                rp.setParameterValue(parameters.get("rep-"+Integer.toString(rp.getPosition())));
            }
            if (parameters.containsKey("autc-"+Integer.toString(rp.getPosition()))) {
                rp.setDisplayedValue(parameters.get("autc-"+Integer.toString(rp.getPosition())));
            }
            if ("STATION".equalsIgnoreCase(rp.getParameterType())) {
                needReenterData = true;
            }
        }
        if (!reportService.validateRequest(reportParameters)) {
            ModelAndView mav = new ModelAndView("reports/input");
            mav.addObject("parameters",reportParameters);
            mav.addObject("report", report);
            if (needReenterData) {
                mav.addObject(Constants.Model.GENERIC_POPUP_MESSAGE, "Due to technical restriction, you may have to re-enter the report generation parameters.");
            }
            return mav;
        }
        logger.debug("Generating report for report {} - Validation OK", reportId);
        GeneratedReport generatedReport = reportService.getGeneratedReportByReportId(
                reportId,
                reportParameters,
                reportFormat
        );
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        if (generatedReport.getMimeType() != null) {
            response.setContentType(generatedReport.getMimeType());
        } else {
            response.setContentType("application/octet-stream");
        }
        if (generatedReport.getFileName() != null && !generatedReport.getFileName().isBlank()) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + generatedReport.getFileName() + "\"");
        }
        try {
            response.getOutputStream().write(generatedReport.getData());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
