package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.enums.JobMessageType;
import org.leolo.nrinfo.model.Job;
import org.leolo.nrinfo.model.JobRecord;
import org.leolo.nrinfo.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Repository
public class JobDao extends BaseDao{

    @Autowired private DataSource datasource;

    public void insertJob(Job job) throws Exception {
        try (
                Connection connection = datasource.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "insert into job " +
                                "(job_id, job_owner, job_class, submitted_time, job_status)" +
                                "values (?, ?, ?, now(), 'S')"
                )
        ) {
            ps.setBytes(1, CommonUtil.uuidToBytes(job.getJobId()));
            ps.setInt(2, job.getJobOwner());
            ps.setString(3, job.getJobClass());
            ps.executeUpdate();
        }
    }

    public void markJobStart(Job job) throws Exception {
        try (
                Connection connection = datasource.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "update job set job_status = 'R', started_time = now() where job_id = ?"
                )
        ) {
            ps.setBytes(1, CommonUtil.uuidToBytes(job.getJobId()));
            ps.executeUpdate();
        }
    }
    public void markJobDone(Job job) throws Exception {
        try (
                Connection connection = datasource.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "update job set job_status = 'D', finished_time = now() where job_id = ?"
                )
        ) {
            ps.setBytes(1, CommonUtil.uuidToBytes(job.getJobId()));
            ps.executeUpdate();
        }
    }

    public void markJobFailed(Job job, Throwable throwable) throws Exception {

        try (
                Connection connection = datasource.getConnection();
                PreparedStatement psJob = connection.prepareStatement(
                        "update job set job_status = 'F', finished_time = now() where job_id = ?"
                );
                PreparedStatement psOut = connection.prepareStatement(
                        "insert into job_output (job_output_id, job_id, output_type, output_data, message_time) " +
                                "values (?, ?, 'E', ?, NOW())"
                )
        ) {
            connection.setAutoCommit(false);
            psOut.setBytes(1, CommonUtil.uuidToBytes(CommonUtil.generateUUID()));
            psOut.setBytes(2, CommonUtil.uuidToBytes(job.getJobId()));
            String stackTrace = null;
            try (
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintWriter pw = new PrintWriter(baos)
            ) {
                throwable.printStackTrace(pw);
                pw.flush();
                stackTrace = baos.toString();
            }
            psOut.setString(3, stackTrace);
            psOut.executeUpdate();
            psJob.setBytes(1, CommonUtil.uuidToBytes(job.getJobId()));
            psJob.executeUpdate();
            connection.commit();
        }
    }

    public void insertMessage(Job job, String message) throws SQLException {
        insertMessage(job, message, JobMessageType.MESSAGE);
    }
    public void insertMessage(Job job, String message, JobMessageType type) throws SQLException {
        try (
                Connection connection = datasource.getConnection();
                PreparedStatement psOut = connection.prepareStatement(
                        "insert into job_output (job_output_id, job_id, output_type, output_data, message_time) " +
                                "values (?, ?, ?, ?, NOW())"
                )
        ) {
            psOut.setBytes(1, CommonUtil.uuidToBytes(CommonUtil.generateUUID()));
            psOut.setBytes(2, CommonUtil.uuidToBytes(job.getJobId()));
            psOut.setString(3, type.getCode());
            psOut.setString(4, message);
            psOut.executeUpdate();
        }
    }

    public JobRecord getJobRecord(String jobId) throws Exception {
        try (
                Connection connection = datasource.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "select * from job where job_id = ?"
                )
        ) {
            ps.setBytes(1, CommonUtil.uuidToBytes(UUID.fromString(jobId)));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseJobRecord(rs);
                }
            }
        }
        return null;
    }

    private JobRecord parseJobRecord(ResultSet rs) throws SQLException {
        JobRecord jobRecord = new JobRecord();
        jobRecord.setJobId(CommonUtil.bytesToUUID(rs.getBytes(1)));
        jobRecord.setJobOwner(rs.getInt(2));
        jobRecord.setJobClass(rs.getString(3));
        jobRecord.setSubmittedTime(rs.getTimestamp(4));
        jobRecord.setStartTime(rs.getTimestamp(5));
        jobRecord.setFinishedTime(rs.getTimestamp(6));
        jobRecord.setStatus(rs.getString(7));
        return  jobRecord;
    }


}
