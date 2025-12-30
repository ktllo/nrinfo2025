package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.model.Schedule;
import org.leolo.nrinfo.model.ScheduleAssociation;
import org.leolo.nrinfo.model.ScheduleDetail;
import org.leolo.nrinfo.util.CommonUtil;
import org.leolo.nrinfo.util.ScheduleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

@Repository
public class ScheduleDao extends BaseDao {

    private Logger log = LoggerFactory.getLogger(ScheduleDao.class);
    @Autowired private DataSource ds;

    public DatabaseOperationResult insertAssociation(Collection<ScheduleAssociation> associations) throws SQLException {
        DatabaseOperationResult result = new DatabaseOperationResult();
        try (
                Connection conn = ds.getConnection();
                PreparedStatement psChk = conn.prepareStatement(
                        "SELECT 1 FROM schedule_association WHERE " +
                                "base_uid = ? AND assoc_uid = ? AND start_date = ? AND end_date = ? AND stp_indicator = ?"
                );
                PreparedStatement psIns = conn.prepareStatement(
                        "INSERT IGNORE INTO schedule_association (" +
                                "base_uid, assoc_uid, start_date, end_date, assoc_days, " +
                                "stp_indicator, assoc_date, assoc_location, base_suffix, assoc_suffix, " +
                                "assoc_category, assoc_type, created_date" +
                                ") VALUES " +
                                "(" +
                                "?,?,?,?,?," +
                                "?,?,?,?,?," +
                                "?,?,NOW())"
                );
        ) {
            conn.setAutoCommit(false);
            for (ScheduleAssociation association : associations) {
                psChk.setString(1, association.getBaseUid());
                psChk.setString(2, association.getAssocUid());
                setTimestamp(psChk, 3, association.getStartDate());
                setTimestamp(psChk, 4, association.getEndDate());
                setString(psChk, 5, association.getAssocDays());
                setString(psChk, 6, association.getStpIndicator());
                try (ResultSet rs = psChk.executeQuery()) {
                    if (!rs.next()) {
                        result.addInserted();
                        psIns.setString(1, association.getBaseUid());
                        psIns.setString(2, association.getAssocUid());
                        setTimestamp(psIns, 3, association.getStartDate());
                        setTimestamp(psIns, 4, association.getEndDate());
                        setString(psIns, 5, association.getAssocDays());
                        setString(psIns, 6, association.getStpIndicator());
                        psIns.setInt(7, association.getAssocDate());
                        setString(psIns, 8, association.getAssocLocation());
                        setString(psIns, 9, association.getBaseSuffix());
                        setString(psIns, 10, association.getAssocSuffix());
                        setString(psIns, 11, association.getAssocCategory());
                        setString(psIns, 12, association.getAssocType());
                        psIns.addBatch();
                    }
                }
            }
            psIns.executeBatch();
            conn.commit();
        }
        return result;
    }

    public DatabaseOperationResult deleteAssociation(Collection<ScheduleAssociation> associations) throws SQLException {
        int deleted = 0;
        try (
                Connection conn = ds.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM schedule_association " +
                                "WHERE base_uid=? AND assoc_uid=? AND " +
                                "start_date = ? AND end_date = ? AND " +
                                "assoc_days = ? AND stp_indicator = ?"
                )
        ) {
            conn.setAutoCommit(false);
            for (ScheduleAssociation association : associations) {
                ps.setString(1, association.getBaseUid());
                ps.setString(2, association.getAssocUid());
                setTimestamp(ps, 3, association.getStartDate());
                setTimestamp(ps, 4, association.getEndDate());
                setString(ps, 5, association.getAssocDays());
                setString(ps, 6, association.getStpIndicator());
                ps.addBatch();
            }
            int [] executeResult = ps.executeBatch();
            for (int i = 0; i < executeResult.length; i++) {
                if (executeResult[i] > 0) {
                    deleted+=executeResult[i];
                }
            }
        }
        return new DatabaseOperationResult(true, 0, 0, deleted);
    }

    public DatabaseOperationResult insertSchedule(Schedule schedule) throws SQLException {
        DatabaseOperationResult result = new DatabaseOperationResult();
        try (
                Connection conn = ds.getConnection();
                PreparedStatement psChk = conn.prepareStatement(
                        "SELECT schedule_uuid FROM schedule WHERE " +
                                "train_uid = ? and start_date = ? " +
                                "AND end_date = ? AND days_run = ? AND stp_indicator = ?"
                );
                PreparedStatement psSch = conn.prepareStatement(
                        "INSERT INTO schedule (" +
                                "schedule_uuid, train_uid, start_date, end_date, days_run, " +
                                "stp_indicator, train_status, bank_holiday_runs, train_category, signal_headcode, " +
                                "operator, retail_headcode, train_service_code, portion_id, power_type, " +
                                "timing_load, planned_speed, operating_characteristics, has_first_class, sleeper, " +
                                "reservations, catering, origin, departure_time, destination, " +
                                "arrival_time, created_time" +
                                ") VALUES (" +
                                "?,?,?,?,?," +
                                "?,?,?,?,?," +
                                "?,?,?,?,?," +
                                "?,?,?,?,?," +
                                "?,?,?,?,?," +
                                "?, NOW()" +
                                ")"
                );
                PreparedStatement psDetail = conn.prepareStatement(
                        "INSERT INTO schedule_details (" +
                                "schedule_uuid, entry_seq, location, location_instance, arrival_time, " +
                                "departure_time, pass_time, public_arrival_time, public_departure_time, platform, " +
                                "line, path, engineering_allowance, pathing_allowance, performance_allowance" +
                                ") VALUES (" +
                                "?,?,?,?,?," +
                                "?,?,?,?,?," +
                                "?,?,?,?,?" +
                                ")"
                )
        ) {
            conn.setAutoCommit(false);
            psChk.setString(1, schedule.getTrainUid());
            setDate(psChk, 2, schedule.getStartDate());
            setDate(psChk, 3, schedule.getEndDate());
            setString(psChk, 4, schedule.getDaysRun());
            setString(psChk, 5, schedule.getStpIndicator());
            try (ResultSet rs = psChk.executeQuery()) {
                if (rs.next()) {
                    //We already have an entry for this record
                    return result;
                }
            }
            psSch.setBytes(1, CommonUtil.uuidToBytes(schedule.getScheduleUuid()));
            psSch.setString(2, schedule.getTrainUid());
            setDate(psSch, 3, schedule.getStartDate());
            setDate(psSch, 4, schedule.getEndDate());
            setString(psSch, 5, schedule.getDaysRun());
            setString(psSch, 6, schedule.getStpIndicator());
            setString(psSch, 7, schedule.getTrainStatus());
            setString(psSch, 8, schedule.getBankHolidayRuns());
            setString(psSch, 9, schedule.getTrainCategory());
            setString(psSch, 10, schedule.getSignalHeadcode());
            setString(psSch, 11, schedule.getOperator());
            setString(psSch, 12, schedule.getRetailHeadcode());
            setString(psSch, 13, schedule.getTrainServiceCode());
            if (schedule.getPortionId().length() > 1) {
                psSch.setNull(14, Types.CHAR);
            } else {
                setString(psSch, 14, schedule.getPortionId());
            }
            setString(psSch, 15, schedule.getPowerType());
            setString(psSch, 16, schedule.getTimingLoad());
            psSch.setInt(17, schedule.getPlannedSpeed());
            setString(psSch, 18, schedule.getOperatingCharacteristics());
            setString(psSch, 19, schedule.getFirstClass());
            setString(psSch, 20, schedule.getSleeper());
            setString(psSch, 21, schedule.getReservations());
            setString(psSch, 22, schedule.getCatering());
            if(schedule.getDetailList()!= null && !schedule.getDetailList().isEmpty()) {
                ScheduleDetail firstDetail = schedule.getDetailList().getFirst();
                ScheduleDetail lastDetail = schedule.getDetailList().getLast();
                setString(psSch, 23, firstDetail.getLocation());
                psSch.setTime(24, firstDetail.getDepartureTime());
                setString(psSch, 25, lastDetail.getLocation());
                psSch.setTime(26, lastDetail.getArrivalTime());
            } else {
                psSch.setNull(23, Types.CHAR);
                psSch.setNull(25, Types.CHAR);
                psSch.setNull(24, Types.TIME);
                psSch.setNull(26, Types.TIME);

            }
            psSch.executeUpdate();
            psDetail.setBytes(1, CommonUtil.uuidToBytes(schedule.getScheduleUuid()));
            if (schedule.getDetailList() != null) {
                for (int i = 0; i < schedule.getDetailList().size(); ) {
                    ScheduleDetail detail = schedule.getDetailList().get(i++);
                    psDetail.setInt(2, i);
                    psDetail.setString(3, detail.getLocation());
                    psDetail.setInt(4, detail.getLocationInstance());
                    psDetail.setTime(5, detail.getArrivalTime());
                    psDetail.setTime(6, detail.getDepartureTime());
                    psDetail.setTime(7, detail.getPassTime());
                    psDetail.setTime(8, detail.getPublicArrivalTime());
                    psDetail.setTime(9, detail.getPublicDepartureTime());
                    psDetail.setString(10, detail.getPlatform());
                    psDetail.setString(11, detail.getLine());
                    psDetail.setString(12, detail.getPath());
                    psDetail.setTime(13, detail.getEngineeringAllowance());
                    psDetail.setTime(14, detail.getPathingAllowance());
                    psDetail.setTime(15, detail.getPerformanceAllowance());
                    psDetail.addBatch();
                }
                psDetail.executeBatch();
            }
            conn.commit();
        }
        result.addInserted();
        result.setSuccess(true);
        return result;
    }

    public DatabaseOperationResult deleteSchedule(Schedule schedule) throws SQLException{
        DatabaseOperationResult result = new DatabaseOperationResult();
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM schedule WHERE schedule_uuid = ?"
                )
        ) {
            connection.setAutoCommit(false);
            ps.setBytes(1, CommonUtil.uuidToBytes(schedule.getScheduleUuid()));
            ps.executeUpdate();
            connection.commit();
        }
        result.setSuccess(true);
        result.addDeleted();
        return result;
    }

    public Collection<String> getTrainUIDByDate(Instant date) throws SQLException {
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "SELECT DISTINCT train_uid FROM schedule WHERE " +
                                "? BETWEEN start_date AND end_date " +
                                "AND days_run LIKE get_date_mask(?)"
                )
        ) {
            Collection<String> trainUIDs = new ArrayList<>();
            ps.setDate(1, new java.sql.Date(date.toEpochMilli()));
            ps.setDate(2, new java.sql.Date(date.toEpochMilli()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trainUIDs.add(rs.getString(1));
                }
            }
            log.info("{} trains UID matched on {}", trainUIDs.size(), date);
            return trainUIDs;
        }
    }

    public void cacheSchedule(Instant date) throws SQLException {
        try (
                Connection connection = ds.getConnection();
                PreparedStatement psList = connection.prepareStatement(
                        "SELECT DISTINCT train_uid FROM schedule WHERE " +
                                "? BETWEEN start_date AND end_date " +
                                "AND days_run LIKE get_date_mask(?)"
                );
                PreparedStatement psUpsert = connection.prepareStatement(
                        "INSERT INTO schedule_map (train_uid, schedule_date, schedule_uuid) " +
                                "SELECT train_uid, ?, schedule_uuid FROM schedule s " +
                                "WHERE " +
                                "train_uid = ? " +
                                "and ? between start_date and end_date " +
                                "and days_run LIKE get_date_mask(?)" +
                                "order by stp_indicator " +
                                "limit 1 "+
                                "ON DUPLICATE KEY UPDATE schedule_map.schedule_uuid = s.schedule_uuid"
                )
        ) {
            connection.setAutoCommit(false);
            java.sql.Date startDate = new java.sql.Date(date.toEpochMilli());
            psList.setDate(1, startDate);
            psList.setDate(2, startDate);
            int count = 0;
            try (ResultSet rsUID = psList.executeQuery()) {
                while (rsUID.next()) {
                    psUpsert.setDate(1, startDate);
                    psUpsert.setString(2, rsUID.getString(1));
                    psUpsert.setDate(3, startDate);
                    psUpsert.setDate(4, startDate);
                    count++;
                    psUpsert.addBatch();
                    if (count % 1000 == 0) {
                        psUpsert.executeBatch();
                        connection.commit();
                    }
                }
            }
            psUpsert.executeBatch();
            connection.commit();
        }
    }

    public void finalizeCache(Instant date) throws SQLException {
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO schedule_map_final " +
                                "(train_uid, schedule_date, schedule_uuid) " +
                                "SELECT train_uid, schedule_date, schedule_uuid " +
                                "from schedule_map " +
                                "where schedule_date = ?"
                )
        ){
            ps.setDate(1, new java.sql.Date(date.toEpochMilli()));
            ps.executeUpdate();
        }
    }
}
