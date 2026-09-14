package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.model.SearchParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public abstract class BaseDao {

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    protected void setString(PreparedStatement ps,int pos, String val) throws SQLException {
        if (val == null || val.isBlank()) {
            ps.setNull(pos, Types.VARCHAR);
        } else {
            ps.setString(pos, val);
        }
    }

    protected void setStringWithMaxLength(PreparedStatement ps,int pos, String val, int maxLength) throws SQLException {
        if (val == null || val.isEmpty()) {
            ps.setNull(pos, Types.VARCHAR);
            return;
        }
        if (val.length() > maxLength) {
            log.warn("Given string is too long, max {}, given {}. Message will be truncated. Given String is {}", maxLength, val.length(), val);
            val = val.substring(0, maxLength);
        }
        ps.setString(pos, val);
    }

    protected void setBooleanAsString(PreparedStatement ps,int pos, String val) throws SQLException {
        if (val == null || val.isEmpty()) {
            ps.setNull(pos, Types.CHAR);
        } else if (val.equalsIgnoreCase("true")||val.equals("1")) {
            ps.setString(pos, "Y");
        } else if (val.equalsIgnoreCase("false")||val.equals("0")) {
            ps.setString(pos, "N");
        } else {
            throw new SQLException("Invalid boolean value: " + val);
        }
    }

    protected void setTimestamp(PreparedStatement ps,int pos, java.util.Date val) throws SQLException {
        if (val == null) {
            ps.setNull(pos, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(pos, new java.sql.Timestamp(val.getTime()));
        }
    }

    protected void setDate(PreparedStatement ps, int pos, java.util.Date val) throws SQLException {
        if (val == null) {
            ps.setNull(pos, Types.DATE);
        } else {
            ps.setDate(pos, new java.sql.Date(val.getTime()));
        }
    }

    protected void setDuration(PreparedStatement ps, int pos, Duration val) throws SQLException {
        if (val == null) {
            ps.setNull(pos, Types.TIME);
            return;
        }
        String formattedString = String.format("%02d:%02d:%02d",
                val.toHours(),
                val.toMinutesPart(),
                val.toSecondsPart());
//        log.debug("Set pos {} to {}", pos, formattedString);
        ps.setString(pos, formattedString);
    }

    protected Duration getDuration(ResultSet rs, int pos) throws SQLException {
        String string = rs.getString(pos);
        if (string == null || rs.wasNull()) {
            return null;
        }
        return parseDuration(string);
    }

    protected Duration getDuration(ResultSet rs, String colName) throws SQLException {
        String string = rs.getString(colName);
        if (string == null || rs.wasNull()) {
            return null;
        }
        return parseDuration(string);
    }

    private Duration parseDuration(String timeString) {
        if (timeString == null || timeString.isEmpty()) return null;

        String[] parts = timeString.split(":");
        if (parts.length != 3) {
            log.warn("Invalid time string: {}", timeString);
            return null;
        }
        long hours = Long.parseLong(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }

    protected java.util.Date getDate(ResultSet rs, int pos) throws SQLException {
        Date date = rs.getDate(pos);
        if (rs.wasNull()) {
            return null;
        }
        return date;
    }

    protected java.util.Date getDate(ResultSet rs, String colName) throws SQLException {
        Date date = rs.getDate(colName);
        if (rs.wasNull()) {
            return null;
        }
        return date;
    }

    protected void setSearchParameter(PreparedStatement ps, int pos, SearchParameter param) throws SQLException {
        boolean isNull = (param.getValue() == null);
        switch (param.getType()) {
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.NCLOB:
                if (isNull) {
                    ps.setNull(pos, Types.CHAR);
                } else {
                    setString(ps, pos, param.getValue().toString());
                }
                break;
            case Types.DATE:
                if (isNull) {
                    ps.setNull(pos, param.getType());
                } else {
                    log.debug("[DATE]Given type is {}", param.getValue().getClass());
                    if (param.getValue() instanceof java.util.Date) {
                        ps.setDate(pos, new java.sql.Date(((java.util.Date) param.getValue()).getTime()));
                    } else if (param.getValue() instanceof Instant) {
                        ps.setDate(pos, new java.sql.Date(((Instant) param.getValue()).toEpochMilli()));
                    } else {
                        log.warn("Invalid date type {}", param.getValue().getClass().getName());
                    }
                }
                break;
            case Types.TIME:
                if (isNull) {
                    ps.setNull(pos, param.getType());
                } else {
                    log.debug("[TIME]Given type is {}", param.getValue().getClass());
                    if (param.getValue() instanceof java.util.Date) {
                        LocalDateTime ldt = ((java.util.Date)param.getValue()).toInstant().atZone(Constants.DEFAULT_TIMEZONE).toLocalDateTime();
                        ps.setObject(pos, ldt.toLocalTime(), Types.TIME);
                    } else if (param.getValue() instanceof LocalTime) {
                        ps.setObject(pos, (LocalTime) param.getValue());
                    }else if (param.getValue() instanceof Instant) {
                        ps.setTime(pos, new java.sql.Time(((Instant) param.getValue()).toEpochMilli() % 86400000L));
                    } else {
                        log.warn("Invalid date type {}", param.getValue().getClass().getName());
                    }
                }
                break;
            case Types.INTEGER:
                if (isNull) {
                    ps.setNull(pos, Types.INTEGER);
                } else if (param.getValue() instanceof Number) {
                    ps.setInt(pos, ((Number) param.getValue()).intValue());
                }
                break;
            case Constants.SQLTypes.ADJUSTED_DATE:
                if (isNull) {
                    ps.setNull(pos, Types.DATE);
                } else {
                    log.debug("[ADJ DATE]Given type is {}", param.getValue().getClass());
                    if (param.getValue() instanceof java.util.Date) {
                        ps.setDate(pos, new java.sql.Date(((java.util.Date) param.getValue()).getTime()));
                    } else if (param.getValue() instanceof Instant) {
                        ps.setDate(pos, new java.sql.Date(((Instant) param.getValue()).toEpochMilli()));
                    } else {
                        log.warn("Invalid date type {}", param.getValue().getClass().getName());
                    }
                }
                break;
            case Constants.SQLTypes.ADJUSTED_DATE_MINUS_DAY:
                if (isNull) {
                    ps.setNull(pos, Types.DATE);
                } else {
                    log.debug("[ADJ DATE]Given type is {}", param.getValue().getClass());
                    if (param.getValue() instanceof java.util.Date) {
                        java.sql.Date date = new java.sql.Date(
                                Instant.ofEpochMilli(
                                                ((java.util.Date) param.getValue()).getTime())
                                        .minus(1, ChronoUnit.DAYS).toEpochMilli()
                        );
                        log.debug("[ACTUAL {}] {}",pos,  date);
                        ps.setDate(pos, date);
                    } else if (param.getValue() instanceof Instant) {
                        ps.setDate(pos, new java.sql.Date(
                                (((Instant) param.getValue()).minus(1, ChronoUnit.DAYS)).toEpochMilli())
                        );
                    } else {
                        log.warn("Invalid date type {}", param.getValue().getClass().getName());
                    }
                }
                break;
            case Constants.SQLTypes.ADJUSTED_DATE_PLUS_DAY:
                if (isNull) {
                    ps.setNull(pos, Types.DATE);
                } else {
                    log.debug("[ADJ DATE]Given type is {}", param.getValue().getClass());
                    if (param.getValue() instanceof java.util.Date) {
                        java.sql.Date date = new java.sql.Date(
                                Instant.ofEpochMilli(
                                                ((java.util.Date) param.getValue()).getTime())
                                        .plus(1, ChronoUnit.DAYS).toEpochMilli()
                        );
                        log.debug("[ACTUAL {}] {}",pos,  date);
                        ps.setDate(pos, date);
                    } else if (param.getValue() instanceof Instant) {
                        ps.setDate(pos, new java.sql.Date(
                                (((Instant) param.getValue()).plus(1, ChronoUnit.DAYS)).toEpochMilli())
                        );
                    } else {
                        log.warn("Invalid date type {}", param.getValue().getClass().getName());
                    }
                }
                break;
            case Constants.SQLTypes.DURATION_AS_TIME:
                if (isNull) {
                    ps.setNull(pos, Types.TIME);
                } else if (param.getValue() instanceof Duration) {
                    setDuration(ps, pos, ((Duration) param.getValue()));
                } else {
                    log.warn("Invalid duration type {}", param.getValue().getClass().getName());
                }
                break;
            case Constants.SQLTypes.DURATION_AS_TIME_PLUS_DAY:
                if (isNull) {
                    ps.setNull(pos, Types.TIME);
                } else if (param.getValue() instanceof Duration) {
                    Duration duration = (Duration) param.getValue();
                    duration = duration.minusDays(1);
                    log.debug("[ACTUAL {}] {}",pos,  duration);
                    setDuration(ps, pos, duration);
                } else {
                    log.warn("Invalid duration type {}", param.getValue().getClass().getName());
                }
                break;
            case Constants.SQLTypes.DURATION_AS_TIME_MINUS_DAY:
                if (isNull) {
                    ps.setNull(pos, Types.TIME);
                } else if (param.getValue() instanceof Duration)  {
                    Duration duration = (Duration) param.getValue();
                    duration = duration.plusDays(1);
                    log.debug("[ACTUAL {}] {}",pos,  duration);
                    setDuration(ps, pos, duration);
                } else {
                    log.warn("Invalid duration type {}", param.getValue().getClass().getName());
                }
                break;
            default:
                log.warn("Given searchParameter type is unknown, type is {}, treating it as a string", param.getType());
                ps.setString(pos, param.getValue().toString());
        }
    }
}
