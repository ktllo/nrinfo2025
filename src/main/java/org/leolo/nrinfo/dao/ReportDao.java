package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.model.report.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ReportDao extends BaseDao {

    @Autowired private DataSource dataSource;
    private Logger log = LoggerFactory.getLogger(ReportDao.class);

    public List<Report> getAllReports() throws SQLException {
        List<Report> reports = new ArrayList<>();
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                            SELECT
                                report_id, report_name, report_mode, report_exec_name, html_style
                            FROM report
                            """
                );
                ResultSet rs = pstmt.executeQuery();
        ) {
            while (rs.next()) {
                reports.add(parseReport(rs));
            }
        }
        return reports;
    }

    public List<Report> getReportsForUser(int userId) throws SQLException {
        List<Report> reports = new ArrayList<>();
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                            SELECT
                                r.report_id, report_name, report_mode, report_exec_name, html_style
                            FROM
                                report r
                                JOIN user_report ur ON r.report_id=ur.report_id
                            WHERE
                                ur.user_id = ?
                            """
                );
        ) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(parseReport(rs));
                }
            }
        }
        return reports;
    }

    public List<ReportGroup> getReportGroups() throws SQLException {
        List<ReportGroup> reportGroups = new ArrayList<>();
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                            SELECT
                                report_group_id, report_group_name
                            FROM
                                report_group
                            ORDER BY
                                display_order, report_group_name
                            """
                );
                ResultSet rs = pstmt.executeQuery();
        ) {
            while (rs.next()) {
                ReportGroup rg = new ReportGroup(rs.getString("report_group_id"), rs.getString("report_group_name"));
                List<Report> reports = getReportsByReportGroupId(rg.reportGroupId());
                if (!reports.isEmpty()) {
                    rg.reports().addAll(reports);
                    reportGroups.add(rg);
                }
            }
        }
        return reportGroups;
    }
    public List<ReportGroup> getReportGroupsForUser(int userId) throws SQLException {
        List<ReportGroup> reportGroups = new ArrayList<>();
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                            SELECT
                                report_group_id, report_group_name
                            FROM
                                report_group
                            """
                );
                ResultSet rs = pstmt.executeQuery();
        ) {
            while (rs.next()) {
                ReportGroup rg = new ReportGroup(rs.getString("report_group_id"), rs.getString("report_group_name"));
                List<Report> reports = getReportsByReportGroupIdForUser(rg.reportGroupId(), userId);
                if (!reports.isEmpty()) {
                    rg.reports().addAll(reports);
                    reportGroups.add(rg);
                }
            }
        }
        return reportGroups;
    }

    public List<Report> getReportsByReportGroupId(String reportGroupId) throws SQLException {
        List<Report> reports = new ArrayList<>();
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                            SELECT
                                report_id, report_name, report_mode, report_exec_name, html_style
                            FROM
                                report r
                            WHERE
                                report_group = ?
                            """
                )
        ) {
            pstmt.setString(1, reportGroupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(parseReport(rs));
                }
            }
            return reports;
        }
    }

    public List<Report> getReportsByReportGroupIdForUser(String reportGroupId, int userId) throws SQLException {
        List<Report> reports = new ArrayList<>();
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                            SELECT
                                r.report_id, report_name, report_mode, report_exec_name, html_style
                            FROM
                                report r
                                JOIN user_report ur ON r.report_id=ur.report_id
                            WHERE
                                report_group = ?
                                AND ur.user_id=?
                            """
                )
        ) {
            pstmt.setString(1, reportGroupId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(parseReport(rs));
                }
            }
            return reports;
        }
    }

    /**
     * Checks whether a user has been explicitly granted access to a report.
     *
     * <p>This method checks for an entry in the {@code user_report} table matching
     * the specified report ID and user ID. It does not check whether the user has
     * the {@code GEN_ANY_REPORT} permission, which grants access to all reports.
     *
     * @param reportId the ID of the report to check access for
     * @param userId   the ID of the user whose access is being checked
     * @return {@code true} if the user has an explicit access entry for the report;
     *         {@code false} otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean checkUserPermissionForReport(String reportId, int userId) throws SQLException {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                            SELECT
                                1
                            FROM
                                user_report ur
                            WHERE
                                ur.report_id=?
                                AND ur.user_id=?
                            """
                )
        ) {
            pstmt.setString(1, reportId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Report getReportByReportId(String reportId) throws SQLException {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                            SELECT
                                report_id, report_name, report_mode, report_exec_name, html_style
                            FROM
                                report
                            WHERE
                                report_id = ?
                            """
                )
        ) {
            pstmt.setString(1, reportId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return parseReport(rs);
                }
            }
        }
        return null;
    }

    public List<ReportParameters> getReportParametersForReport(String reportId) throws SQLException {
        List<ReportParameters> reportParameters = new ArrayList<>();
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                            SELECT
                                report_id, position, parameter_name, parameter_type, required, validation_group
                            FROM
                                report_parameter rp
                            WHERE
                                report_id = ?
                            ORDER BY
                                report_id, position
                            """
                )
        ) {
            pstmt.setString(1, reportId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reportParameters.add(parseReportParameters(rs));
                }
            }
        }
        return reportParameters;
    }

    public ExecutionResult executeReport(Report report, List<ReportParameters> parameters) throws SQLException {
        String sql = "call "+report.reportExecName()+"("+
                String.join(",", Collections.nCopies(parameters.size(), "?")) +
                ")";
        ExecutionResult executionResult = new ExecutionResult();
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            for (int i=0;i<parameters.size();i++) {
                //Filling in parameters
                final int POSITION = i+1;
                final ReportParameters parameter = parameters.get(i);
                if (POSITION != parameter.getPosition()) {
                    log.warn("Position mismatch for parameter {}, {} expected {} in config", report.reportId(), POSITION, parameter);
                }
                if ("date_range".equalsIgnoreCase(parameter.getParameterType())){
                    if (parameter.getParameterValue().isBlank()) {
                        pstmt.setNull(POSITION, Types.DATE);
                    } else {
                        LocalDateTime zdt = LocalDate.parse(parameter.getParameterValue()).atStartOfDay();
                        setTimestamp(pstmt, POSITION, Timestamp.valueOf(zdt));
                    }
                } else {
                    log.warn("Unknown parameter type {}, will be treated as string", parameter.getParameterType());
                    setString(pstmt, POSITION, parameter.getParameterValue());
                }
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                List<String> header = new ArrayList<>();
                ResultSetMetaData rsmd = rs.getMetaData();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    header.add(rsmd.getColumnLabel(i));
                }
                executionResult.setHeader(header);
                log.debug("{} columns found", header.size());
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        row.add(rs.getString(i));
                    }
                    executionResult.addRow(row);
                }
            }
        }
        return executionResult;
    }

    public ReportOutputTransformList getReportOutputTransform(String reportId) throws SQLException {
        ReportOutputTransformList reportOutputTransformList = new ReportOutputTransformList();
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        """
                            SELECT
                                report_id, transform_sequence, output_pos, transform_class
                            FROM
                                report_output_transform
                            WHERE
                                report_id = ?
                            """
                )
        ) {
            pstmt.setString(1, reportId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reportOutputTransformList.add(parseReportOutputTransform(rs));
                }
            }
        }
        return reportOutputTransformList;
    }

    private ReportOutputTransform parseReportOutputTransform(ResultSet rs) throws SQLException {
        return new ReportOutputTransform(
                rs.getString("report_id"),
                rs.getInt("transform_sequence"),
                rs.getInt("output_pos"),
                rs.getString("transform_class")
        );
    }

    private ReportParameters parseReportParameters(ResultSet rs) throws SQLException {
        return new ReportParameters(
                rs.getString("report_id"),
                rs.getInt("position"),
                rs.getString("parameter_name"),
                rs.getString("parameter_type"),
                rs.getInt("required") == 1,
                rs.getString("validation_group")
        );
    }


    private Report parseReport(ResultSet rs) throws SQLException {
        return new Report(
                rs.getString("report_id"),
                rs.getString("report_name"),
                rs.getString("report_mode"),
                rs.getString("report_exec_name"),
                rs.getString("html_style")
        );
    }

}
