package org.leolo.nrinfo.report;

import org.leolo.nrinfo.model.report.GeneratedReport;
import org.leolo.nrinfo.model.report.Report;
import org.leolo.nrinfo.model.report.ReportGroup;
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
    public GeneratedReport generateReport(List<ReportParameters> parm) {
        GeneratedReport gr = new GeneratedReport();
        gr.setMimeType("text/html");
        try (
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PrintWriter out = new PrintWriter(outputStream)
        ) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<body>");
            out.println("<h1>Report Parameters Report</h1>");
            List<ReportGroup> reportGroups = reportService.getReportGroups();
            out.println("<h2>Report List</h2>");
            out.println("<ul id='replist'>");
            for (ReportGroup reportGroup : reportGroups) {
                out.println("<li>");
                out.println("<span>%s</span>".formatted(reportGroup.getReportGroupName()));
                out.println("<ul>");
                for (Report report: reportGroup.getReports()) {
                    out.println("<li><a href='#rpttab-%s'>%s - %s</a></li>".formatted(
                            report.reportId(),
                            report.reportId(),
                            report.reportName()
                    ));
                }
                out.println("</ul>");
                out.println("</li>");
            }
            out.println("</ul>");
            //Details
            for (ReportGroup reportGroup : reportGroups) {
                for (Report report : reportGroup.getReports()) {
                    out.println("<div id='rpttab-%s'>".formatted(report.reportId()));
                    out.println("<h3>%s - %s</h3>".formatted(report.reportId(), report.reportName()));
                    out.println("<h4>Basic Info</h4>");
                    out.println("<table>");
                    out.println("<tr><th>Report ID</th><td>%s</td></tr>".formatted(report.reportId()));
                    out.println("<tr><th>Report Name</th><td>%s</td></tr>".formatted(report.reportName()));
                    out.println("<tr><th>Execution method</th><td>%s</td></tr>".formatted(report.reportMode()));
                    out.println("<tr><th>Executable</th><td><pre>%s</pre></td></tr>".formatted(report.reportExecName()));
                    out.println("</table>");
                    out.println("<h4>Parameter List</h4>");
                    out.println("<table>");
                    out.println("<tr>");
                    out.println("<th>#</th>");
                    out.println("<th>Parameter Name</th>");
                    out.println("<th>Parameter Type</th>");
                    out.println("<th>Required</th>");
                    out.println("</tr>");
                    List<ReportParameters> parameters = reportService.getReportParametersForReport(report.reportId());
                    for (ReportParameters parameter: parameters) {
                        out.println("<tr>");
                        out.println("<td>%s</td>".formatted(parameter.getPosition()));
                        out.println("<td>%s</td>".formatted(parameter.getParameterName()));
                        out.println("<td>%s</td>".formatted(parameter.getParameterType()));
                        out.println("<td>%s</td>".formatted(parameter.isRequired()?"Yes":"No"));
                        out.println("</tr>");
                    }
                    out.println("</table>");
                    out.println("<a href='#replist'>Back to top</a>");
                    out.println("</div>");
                }
            }
            out.println("</body>");
            out.println("</html>");
            out.flush();
            gr.setData(outputStream.toByteArray());
            gr.setMimeType("text/html");
        } catch (IOException e) {
            logger.error("Error while generating report {}", e.getMessage(), e);
        }
        return gr;
    }
}
