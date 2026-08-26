package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.model.SearchParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
                    if (param.getValue() instanceof java.util.Date) {
                        ps.setTime(pos, new java.sql.Time(((java.util.Date) param.getValue()).getTime() % 86400000L));
                    } else if (param.getValue() instanceof Instant) {
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
            default:
                log.warn("Given searchParameter type is unknown, type is {}, treating it as a string", param.getType());
                ps.setString(pos, param.getValue().toString());
        }
    }
}
