package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.model.Tiploc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

@Repository
public class TiplocDao extends BaseDao{

    @Autowired private DataSource ds;

    private Logger log = LoggerFactory.getLogger(TiplocDao.class);

    public Tiploc getTiplocByTiplocCode (String tiplocCode) throws SQLException {
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement("select * from tiploc where tiploc_code = ?")
        ) {
            ps.setString(1, tiplocCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseResultSet(rs);
                }
            }
        }
        return null;
    }

    public DatabaseOperationResult upsertTiplocs(Collection<Tiploc> tiplocs) throws SQLException {
        DatabaseOperationResult result = new DatabaseOperationResult();
        HashSet<String> pendingItems = new HashSet<String>();
        try (
                Connection connection = ds.getConnection();
                PreparedStatement psIns = connection.prepareStatement(
                        "INSERT INTO tiploc " +
                                "(nalco, stanox, crs_code, description, tps_description, tiploc_code) " +
                                "VALUES (?, ?, ?, ?, ?, ?)"
                );
                PreparedStatement psUpdate = connection.prepareStatement(
                        "UPDATE tiploc " +
                                "SET nalco=?, stanox=?, crs_code=?, description=?, tps_description=? " +
                                "WHERE tiploc_code=?"
                )
        ) {
            connection.setAutoCommit(false);
            for (Tiploc tiploc : tiplocs) {
                Tiploc current = getTiplocByTiplocCode(tiploc.getTiplocCode());
                PreparedStatement ps = null;
                if (current == null) {
                    // To insert
                    if (pendingItems.contains(tiploc.getTiplocCode())) {
                        log.warn("TIPLOC code {} already exists", tiploc.getTiplocCode());
                        continue;
                    }
                    pendingItems.add(tiploc.getTiplocCode());
                    ps = psIns;
                    result.addInserted();
                } else if (!current.equals(tiploc)) {
                    // To Update
                    ps = psUpdate;
                    result.addUpdated();
                } else {
                    // The data is not changed
                    continue;
                }
                ps.setString(1, tiploc.getNalco());
                ps.setString(2, tiploc.getStanox());
                ps.setString(3, tiploc.getCrsCode());
                ps.setString(4, tiploc.getShortDescription());
                ps.setString(5, tiploc.getDescription());
                ps.setString(6, tiploc.getTiplocCode());
                ps.addBatch();
            }
            psIns.executeBatch();
            psUpdate.executeBatch();
            connection.commit();
        }
        return result;
    }

    public DatabaseOperationResult updateTiplocs(Collection<Tiploc> tiplocs) throws SQLException {
        DatabaseOperationResult result = new DatabaseOperationResult();
        HashSet<String> pendingItems = new HashSet<String>();
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "UPDATE tiploc " +
                                "SET nalco=?, stanox=?, crs_code=?, description=?, tps_description=? " +
                                "WHERE tiploc_code=?"
                )
        ) {
            connection.setAutoCommit(false);
            for (Tiploc tiploc : tiplocs) {
                Tiploc current = getTiplocByTiplocCode(tiploc.getTiplocCode());
                if (current.equals(tiploc)) {
                     continue;
                }
                result.addUpdated();
                ps.setString(1, tiploc.getNalco());
                ps.setString(2, tiploc.getStanox());
                ps.setString(3, tiploc.getCrsCode());
                ps.setString(4, tiploc.getShortDescription());
                ps.setString(5, tiploc.getDescription());
                ps.setString(6, tiploc.getTiplocCode());
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
        }
        return result;
    }

    public DatabaseOperationResult deleteTiplocs(Collection<Tiploc> tiplocs) throws SQLException {
        DatabaseOperationResult result = new DatabaseOperationResult();
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM tiploc WHERE tiploc_code = ?"
                )
        ) {
            connection.setAutoCommit(false);
            for (Tiploc tiploc : tiplocs) {
                ps.setString(1, tiploc.getTiplocCode());
                ps.addBatch();
                result.addDeleted();
            }
            ps.executeBatch();
            connection.commit();
        }
        return result;
    }

    private Tiploc parseResultSet(ResultSet rs) throws SQLException {
        Tiploc tiploc = new Tiploc();
        tiploc.setTiplocCode(rs.getString("tiploc_code"));
        tiploc.setNalco(rs.getString("nalco"));
        tiploc.setStanox(rs.getString("stanox"));
        tiploc.setCrsCode(rs.getString("crs_code"));
        tiploc.setShortDescription(rs.getString("description"));
        tiploc.setDescription(rs.getString("tps_description"));
        return tiploc;
    }

}
