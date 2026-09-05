package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.dto.request.ScheduleSearch;
import org.leolo.nrinfo.dto.response.TrainScheduleSummary;
import org.leolo.nrinfo.model.Schedule;
import org.leolo.nrinfo.model.ScheduleAssociation;
import org.leolo.nrinfo.model.ScheduleDetail;
import org.leolo.nrinfo.model.SearchParameter;
import org.leolo.nrinfo.util.CommonUtil;
import org.leolo.nrinfo.util.ScheduleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class ScheduleDao extends BaseDao {

    //Shared SQL
    public static final String SQL_FIND_APPLICABLE_SCHEDULE = """
            SELECT
                schedule_uuid
            FROM schedule s
            WHERE
                train_uid = ?
                and ? between start_date and end_date
                and days_run LIKE get_date_mask(?)
            order by stp_indicator %s
            limit 1
            """;
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
            for (int j : executeResult) {
                if (j > 0) {
                    deleted += j;
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
            if (schedule.getPortionId()!= null && schedule.getPortionId().length() > 1) {
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

    public boolean deleteSchedule(String trainUID, java.util.Date startDate, java.util.Date endDate) throws SQLException {
        if (trainUID == null || startDate == null || endDate == null) {
            return false;
        }
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        """
                            DELETE FROM schedule
                                   WHERE
                                       train_uid = ?
                                        AND start_date = ?
                                        AND end_date = ?
                            """
                )
        ) {
            ps.setString(1, trainUID);
            setDate(ps, 2, startDate);
            setDate(ps, 3, endDate);
            return ps.executeUpdate() > 0;
        }
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
                        """
                                INSERT INTO schedule_map (train_uid, schedule_date, schedule_uuid, updated_date)
                                SELECT
                                    train_uid, ?, schedule_uuid, now() FROM schedule s
                                WHERE
                                    train_uid = ?
                                    and ? between start_date and end_date
                                    and days_run LIKE get_date_mask(?)
                                order by stp_indicator
                                limit 1
                                ON DUPLICATE KEY UPDATE schedule_map.schedule_uuid = s.schedule_uuid, updated_date = now()
                                """
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

    public void pruneCache(Instant date) throws SQLException {
        try (
                Connection connection = ds.getConnection();
                PreparedStatement psMain = connection.prepareStatement(
                        "DELETE FROM schedule_map WHERE schedule_date < ?"
                );
                PreparedStatement psFinal = connection.prepareStatement(
                        "DELETE FROM schedule_map_final WHERE schedule_date < ?"
                )
        ) {
            psMain.setDate(1, new java.sql.Date(date.toEpochMilli()));
            psFinal.setDate(1, new java.sql.Date(date.toEpochMilli()));
            connection.setAutoCommit(false);
            psMain.executeUpdate();
            psFinal.executeUpdate();
            connection.commit();
        }
    }

    public void pruneSchedule(Instant date) throws SQLException {
        try (
                Connection connection = ds.getConnection();
                // Delete association first
                PreparedStatement psAssoc = connection.prepareStatement("""
                   DELETE FROM schedule_association
                       WHERE end_date < ?
                   """
                );
                // Delete details
                PreparedStatement psDetail = connection.prepareStatement(
                        """
                            DELETE FROM schedule_details
                                where schedule_uuid in(
                                        SELECT schedule_uuid
                                            FROM schedule s
                                            WHERE
                                                s.end_date < ?
                                                and not exists (SELECT 1 FROM stared_schedule ss WHERE ss.schedule_id = s.schedule_uuid)
                                )
                            """
                );
                // Finally delete the main record
                PreparedStatement psSchedule = connection.prepareStatement(
                        """
                            DELETE FROM schedule
                                WHERE
                                   end_date < ?
                                   and not exists (SELECT 1 FROM stared_schedule ss WHERE ss.schedule_id = schedule.schedule_uuid)
                            """
                )
        ) {
            connection.setAutoCommit(false);
            psAssoc.setDate(1, new java.sql.Date(date.toEpochMilli()));
            psDetail.setDate(1, new java.sql.Date(date.toEpochMilli()));
            psSchedule.setDate(1, new java.sql.Date(date.toEpochMilli()));
            int assoc = psAssoc.executeUpdate();
            int detail = psDetail.executeUpdate();
            int schedule = psSchedule.executeUpdate();
//            connection.commit();
            connection.rollback();
            log.info("Deleted: {} Association, {} Schedule, {} detail record, {} in total", assoc, schedule, detail, assoc + schedule + detail);
        }
    }

    public UUID checkScheduleCache(String trainUID, java.util.Date date) throws SQLException {
        try(
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        """
                            SELECT schedule_uuid
                            FROM schedule_map
                            WHERE
                                train_uid = ?
                                AND schedule_date = ?
                            """
                )
                ) {
            trainUID = trainUID.strip().toUpperCase();
            ps.setString(1, trainUID);
            ps.setDate(2, new java.sql.Date(date.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return CommonUtil.bytesToUUID(rs.getBytes(1));
                }
            }
        }
        return null;
    }

    public UUID findApplicableSchedule(String trainUID, java.util.Date date) throws SQLException {
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement(SQL_FIND_APPLICABLE_SCHEDULE.formatted("ASC"));
        ) {
            ps.setString(1, trainUID);
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.setDate(3, new java.sql.Date(date.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return CommonUtil.bytesToUUID(rs.getBytes(1));
                }
            }
        }
        return null;
    }
    public UUID findBaseSchedule(String trainUID, java.util.Date date) throws SQLException {
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement(SQL_FIND_APPLICABLE_SCHEDULE.formatted("DESC"));
        ) {
            ps.setString(1, trainUID);
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.setDate(3, new java.sql.Date(date.getTime()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return CommonUtil.bytesToUUID(rs.getBytes(1));
                }
            }
        }
        return null;
    }

    public void forceCacheRebuild(String trainUID, java.util.Date date) throws SQLException {
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO schedule_map (train_uid, schedule_date, schedule_uuid, updated_date)
                SELECT
                    train_uid, ?, schedule_uuid, NOW() FROM schedule s
                WHERE
                    train_uid = ?
                    and ? between start_date and end_date
                    and days_run LIKE get_date_mask(?)
                order by stp_indicator
                limit 1
                ON DUPLICATE KEY UPDATE schedule_map.schedule_uuid = s.schedule_uuid
                """)
        ){
            ps.setDate(1, new java.sql.Date(date.getTime()));
            ps.setString(2, trainUID);
            ps.setDate(3, new java.sql.Date(date.getTime()));
            ps.setDate(4, new java.sql.Date(date.getTime()));
            ps.executeUpdate();
        }
    }

    public Schedule getSchedule(UUID uuid) throws SQLException {
        try (
                Connection connection = ds.getConnection();
                PreparedStatement psSch = connection.prepareStatement(
                        "SELECT * FROM schedule WHERE schedule_uuid = ?"
                );
                PreparedStatement psDetail = connection.prepareStatement(
                        "SELECT * FROM schedule_details WHERE schedule_uuid = ? ORDER BY entry_seq "
                )
        ) {
            Schedule schedule = new Schedule();
            psSch.setBytes(1, CommonUtil.uuidToBytes(uuid));
            try (ResultSet rs = psSch.executeQuery()) {
                if (rs.next()) {
                    schedule.setScheduleUuid(CommonUtil.bytesToUUID(rs.getBytes("schedule_uuid")));
                    schedule.setTrainUid(rs.getString("train_uid"));
                    schedule.setStartDate(rs.getDate("start_date"));
                    schedule.setEndDate(rs.getDate("end_date"));
                    schedule.setDaysRun(rs.getString("days_run"));
                    schedule.setStpIndicator(rs.getString("stp_indicator"));
                    schedule.setTrainStatus(rs.getString("train_status"));
                    schedule.setBankHolidayRuns(rs.getString("bank_holiday_runs"));
                    schedule.setTrainCategory(rs.getString("train_category"));
                    schedule.setSignalHeadcode(rs.getString("signal_headcode"));
                    schedule.setOperator(rs.getString("operator"));
                    schedule.setRetailHeadcode(rs.getString("retail_headcode"));
                    schedule.setTrainServiceCode(rs.getString("train_service_code"));
                    schedule.setPortionId(rs.getString("portion_id"));
                    schedule.setPowerType(rs.getString("power_type"));
                    schedule.setTimingLoad(rs.getString("timing_load"));
                    schedule.setPlannedSpeed(rs.getInt("planned_speed"));
                    schedule.setOperatingCharacteristics(rs.getString("operating_characteristics"));
                    schedule.setFirstClass(rs.getString("has_first_class"));
                    schedule.setSleeper(rs.getString("sleeper"));
                    schedule.setReservations(rs.getString("reservations"));
                    schedule.setCatering(rs.getString("catering"));
                } else {
                    log.debug("No schedule found for uuid: {}", uuid);
                    return null;
                }
            }
            psDetail.setBytes(1, CommonUtil.uuidToBytes(uuid));
            try (ResultSet rs = psDetail.executeQuery()) {
                while (rs.next()) {
                    ScheduleDetail scheduleDetail = new ScheduleDetail();
                    scheduleDetail.setLocation(rs.getString("location"));
                    scheduleDetail.setLocationInstance(rs.getInt("location_instance"));
                    scheduleDetail.setArrivalTime(rs.getTime("arrival_time"));
                    scheduleDetail.setDepartureTime(rs.getTime("departure_time"));
                    scheduleDetail.setPassTime(rs.getTime("pass_time"));
                    scheduleDetail.setPublicArrivalTime(rs.getTime("public_arrival_time"));
                    scheduleDetail.setPublicDepartureTime(rs.getTime("public_departure_time"));
                    scheduleDetail.setPlatform(rs.getString("platform"));
                    scheduleDetail.setLine(rs.getString("line"));
                    scheduleDetail.setPath(rs.getString("path"));
                    scheduleDetail.setEngineeringAllowance(rs.getTime("engineering_allowance"));
                    scheduleDetail.setPathingAllowance(rs.getTime("pathing_allowance"));
                    scheduleDetail.setPerformanceAllowance(rs.getTime("performance_allowance"));
                    schedule.getDetailList().add(scheduleDetail);
                }
            }

            return schedule;
        }
    }

    public List<ScheduleAssociation> getScheduleAssociation(String trainUid, java.util.Date date) throws SQLException {
        List<ScheduleAssociation> list = new ArrayList<>();
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement("""
                    SELECT
                        base_uid, assoc_uid, start_date, end_date, assoc_days,
                        stp_indicator, assoc_date, assoc_location, base_suffix, assoc_suffix,
                        assoc_category, assoc_type
                    FROM
                        schedule_association
                    WHERE
                        (
                            base_uid = ?
                            OR assoc_uid = ?
                        )
                        AND ? BETWEEN start_date AND end_date
                        AND assoc_days LIKE get_date_mask(?)
                    """)
                ) {
            ps.setString(1, trainUid);
            ps.setString(2, trainUid);
            setDate(ps, 3, date);
            setDate(ps, 4, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ScheduleAssociation scheduleAssociation = new ScheduleAssociation();
                    scheduleAssociation.setBaseUid(rs.getString("base_uid"));
                    scheduleAssociation.setAssocUid(rs.getString("assoc_uid"));
                    scheduleAssociation.setStartDate(rs.getDate("start_date"));
                    scheduleAssociation.setEndDate(rs.getDate("end_date"));
                    scheduleAssociation.setAssocDays(rs.getString("assoc_days"));
                    scheduleAssociation.setStpIndicator(rs.getString("stp_indicator"));
                    scheduleAssociation.setAssocDate(rs.getInt("assoc_date"));
                    scheduleAssociation.setAssocLocation(rs.getString("assoc_location"));
                    scheduleAssociation.setBaseSuffix(rs.getString("base_suffix"));
                    scheduleAssociation.setAssocSuffix(rs.getString("assoc_suffix"));
                    scheduleAssociation.setAssocCategory(rs.getString("assoc_category"));
                    scheduleAssociation.setAssocType(rs.getString("assoc_type"));
                    list.add(scheduleAssociation);
                }
            }
        }
        return list;
    }

    public Instant getOldestCacheDate(java.util.Date date) throws SQLException {
        try (
                Connection connection = ds.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(
                        """
                            SELECT MIN(updated_date)
                            FROM schedule_map
                            WHERE schedule_date = ?
                            """
                )
        ){
            setDate(pstmt, 1, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                //We can only get 1 row
                if (rs.next()) {
                    Timestamp oldestDate = rs.getTimestamp(1);
                    return oldestDate == null ? null : oldestDate.toInstant();
                }
            }
        }
        return null;
    }

    public List<UUID> searchTrainSchedule(ScheduleSearch scheduleSearch) throws SQLException {
        List<UUID> list = new ArrayList<>();

        List<SearchParameter> params = new ArrayList<>();
        //Shared part
        List<String> timeFields;
        if (scheduleSearch.isPublicTimetableOnly()) {
            timeFields = new ArrayList<>(List.of("public_departure_time", "public_arrival_time"));
        } else {
            timeFields = new ArrayList<>(List.of("public_departure_time", "departure_time", "public_arrival_time", "arrival_time"));
            if (!scheduleSearch.isCallOnly()) {
                timeFields.add("pass_time");
            }
        }
        StringBuilder timeFilterFields = new StringBuilder();
        timeFilterFields.append("COALESCE(");
        if (scheduleSearch.isHideCancelledTrain()) {
            timeFilterFields.append(timeFields.stream().map(tf -> "sd."+ tf).collect(Collectors.joining(",")));
        } else {
            timeFilterFields.append(timeFields.stream().map(tf -> "sd."+ tf).collect(Collectors.joining(",")));
            timeFilterFields.append(",");
            timeFilterFields.append(timeFields.stream().map(tf -> "bsd."+ tf).collect(Collectors.joining(",")));
        }
        timeFilterFields.append(") ");
        //Build the SQL
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("select distinct s.schedule_uuid ");
        sbSql.append("from schedule s ");
        sbSql.append("left join schedule_map sm on s.schedule_uuid = sm.schedule_uuid ");
        if (scheduleSearch.getLocation() != null) {
            sbSql.append("left join schedule_details sd on s.schedule_uuid = sd.schedule_uuid ");
            if (!scheduleSearch.isStrictLocationMatch()) {
                sbSql.append("left join v_auto_tiploc_group vatg on sd.location = vatg.group_member ");
            }
            if (scheduleSearch.getPreviousVia() != null) {
                sbSql.append("join schedule_details sdp on s.schedule_uuid = sdp.schedule_uuid and sd.entry_seq > sdp.entry_seq ");
                if (!scheduleSearch.isStrictLocationMatch()) {
                    sbSql.append("left join v_auto_tiploc_group vatgp on sdp.location = vatgp.group_member ");
                }
            }
            if (scheduleSearch.getWillGoVia() != null) {
                sbSql.append("join schedule_details sdn on s.schedule_uuid = sdn.schedule_uuid and sd.entry_seq < sdn.entry_seq ");
                if (!scheduleSearch.isStrictLocationMatch()) {
                    sbSql.append("left join v_auto_tiploc_group vatgn on sdn.location = vatgn.group_member ");
                }
            }
        }
        if (!scheduleSearch.isHideCancelledTrain()) {
            sbSql.append("left join schedule bs on s.train_uid = bs.train_uid and ? between bs.start_date and bs.end_date and " +
                    "bs.days_run like get_date_mask(?) and bs.stp_indicator = 'P' and s.stp_indicator = 'C' ");
            if (scheduleSearch.getLocation() != null) {
                sbSql.append("left join schedule_details bsd on bs.schedule_uuid = bsd.schedule_uuid ");
                if (!scheduleSearch.isStrictLocationMatch()) {
                    sbSql.append("left join v_auto_tiploc_group bvatg on bsd.location = bvatg.group_member ");
                }
                if (scheduleSearch.getPreviousVia() != null) {
                    sbSql.append("left join schedule_details bsdp on bs.schedule_uuid = bsdp.schedule_uuid and bsd.entry_seq > bsdp.entry_seq ");
                    if (!scheduleSearch.isStrictLocationMatch()) {
                        sbSql.append("left join v_auto_tiploc_group bvatgp on bsdp.location = bvatgp.group_member ");
                    }
                }
                if (scheduleSearch.getWillGoVia() != null) {
                    sbSql.append("left join schedule_details bsdn on bs.schedule_uuid = bsdn.schedule_uuid and bsd.entry_seq < bsdn.entry_seq ");
                    if (!scheduleSearch.isStrictLocationMatch()) {
                        sbSql.append("left join v_auto_tiploc_group bvatgn on bsdn.location = bvatgn.group_member ");
                    }
                }
            }
            params.add(new SearchParameter(Types.DATE, scheduleSearch.getFromTime()));
            params.add(new SearchParameter(Types.DATE, scheduleSearch.getFromTime()));
        }

        sbSql.append("where 1=1 ");
        sbSql.append("and sm.schedule_date = ? ");
        params.add(new SearchParameter(Types.DATE, scheduleSearch.getFromTime()));

        if (scheduleSearch.getLocation() != null) {
            if (scheduleSearch.isStrictLocationMatch()) {
                if (scheduleSearch.isHideCancelledTrain()) {
                    sbSql.append("and sd.location = ? ");
                } else {
                    sbSql.append("and (sd.location = ?  or bsd.location = ?) ");
                }
            } else {
                if (scheduleSearch.isHideCancelledTrain()) {
                    sbSql.append("and vatg.given_code = ? ");
                } else {
                    sbSql.append("and (vatg.given_code = ? or bvatg.given_code = ?) ");
                }
            }
            params.add(new SearchParameter(Types.VARCHAR, scheduleSearch.getLocation()));
            if (!scheduleSearch.isHideCancelledTrain()) {
                params.add(new SearchParameter(Types.VARCHAR, scheduleSearch.getLocation()));
            }
            //Previous/Will go vias
            if (scheduleSearch.getPreviousVia() != null) {
                if (scheduleSearch.isStrictLocationMatch()) {
                    if (scheduleSearch.isHideCancelledTrain()) {
                        sbSql.append("and sdp.location = ? ");
                    } else {
                        sbSql.append("and (sdp.location = ? or bsdp.location = ?) ");
                    }
                } else {
                    if (scheduleSearch.isHideCancelledTrain()) {
                        sbSql.append("and vatgp.given_code = ? ");
                    } else {
                        sbSql.append("and (vatgp.given_code = ? or bvatgp.given_code = ?) ");
                    }
                }
                params.add(new SearchParameter(Types.VARCHAR, scheduleSearch.getPreviousVia()));
                if (!scheduleSearch.isHideCancelledTrain()) {
                    params.add(new SearchParameter(Types.VARCHAR, scheduleSearch.getPreviousVia()));
                }
            }
            if (scheduleSearch.getWillGoVia() != null) {
                if (scheduleSearch.isStrictLocationMatch()) {
                    if (scheduleSearch.isHideCancelledTrain()) {
                        sbSql.append("and sdn.location = ? ");
                    } else {
                        sbSql.append("and (sdn.location = ? or bsdn.location = ?) ");
                    }
                } else {

                    if (scheduleSearch.isHideCancelledTrain()) {
                        sbSql.append("and vatgn.given_code = ? ");
                    } else {
                        sbSql.append("and (vatgn.given_code = ? or bvatgn.given_code = ?) ");
                    }
                }
                params.add(new SearchParameter(Types.VARCHAR, scheduleSearch.getWillGoVia()));
                if (!scheduleSearch.isHideCancelledTrain()) {
                    params.add(new SearchParameter(Types.VARCHAR, scheduleSearch.getWillGoVia()));
                }
            }
        }
        if (scheduleSearch.getHeadcode() != null) {
            if (!scheduleSearch.isHideCancelledTrain()) {
                sbSql.append("and (s.signal_headcode = ? or bs.signal_headcode = ?) ");
                params.add(new SearchParameter(Types.VARCHAR, scheduleSearch.getHeadcode()));
                params.add(new SearchParameter(Types.VARCHAR, scheduleSearch.getHeadcode()));
            } else {
                sbSql.append("and s.signal_headcode = ?");
                params.add(new SearchParameter(Types.VARCHAR, scheduleSearch.getHeadcode()));
            }
        }
        //Time
        if (scheduleSearch.getLocation() != null) {

            sbSql.append("and ").append(timeFilterFields).append(" BETWEEN ? AND ? ");
            log.debug("Filtering time : {} - {}", scheduleSearch.getFromLocalTime(), scheduleSearch.getToLocalTime());
            params.add(new SearchParameter(Types.TIME, scheduleSearch.getFromLocalTime()));
            params.add(new SearchParameter(Types.TIME, scheduleSearch.getToLocalTime()));
        }
        //Operator
        if (scheduleSearch.getTrainOperator() != null && !scheduleSearch.getTrainOperator().isEmpty()) {
            String inClause = " in (" + String.join(",", Collections.nCopies(scheduleSearch.getTrainOperator().size(), "?")) + ") ";
            if (scheduleSearch.isHideCancelledTrain()) {
                //Normal case
                sbSql.append("and s.operator ").append(inClause);
                for (String operator : scheduleSearch.getTrainOperator()) {
                    params.add(new SearchParameter(Types.VARCHAR, operator));
                }
            } else {
                sbSql.append("and (s.operator ").append(inClause).append("or bs.operator ").append(inClause).append(") ");
                Stream.of(scheduleSearch.getTrainOperator(), scheduleSearch.getTrainOperator()).forEach(
                        l -> l.forEach(
                                o -> params.add(new SearchParameter(Types.VARCHAR, o))
                        )
                );
            }
        }


        //Sort
        if (scheduleSearch.getLocation() != null) {
            sbSql.append("ORDER BY ").append(timeFilterFields);
        } else {
            sbSql.append("ORDER BY s.departure_time ");
        }


        if (scheduleSearch.getPageSize() != 0) {
            sbSql.append("LIMIT ? ");
            params.add(new SearchParameter(Types.INTEGER, scheduleSearch.getPageSize()));
        }



        log.info("Generated SQL: {}", sbSql.toString());
        for (int i=0;i<params.size();i++) {
            log.info(" Param {}: {}", i+1, params.get(i).getValue());
        }
        try (
                Connection connection = ds.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sbSql.toString())
        ) {
            for (int i=0;i<params.size();i++) {
                setSearchParameter(pstmt, i+1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                int rowCount = 0;
                while (rs.next()) {
                    UUID rowUUID = CommonUtil.bytesToUUID(rs.getBytes(1));
                    list.add(rowUUID);
                    rowCount++;
                }
                log.info("Found {} rows", rowCount);
            }
        }
        return list;
    }
}
