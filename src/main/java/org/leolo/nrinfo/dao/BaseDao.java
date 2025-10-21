package org.leolo.nrinfo.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public abstract class BaseDao {

    private Logger log = LoggerFactory.getLogger(this.getClass());


    protected void setString(PreparedStatement ps,int pos, String val) throws SQLException {
        if (val == null || val.isEmpty()) {
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
}
