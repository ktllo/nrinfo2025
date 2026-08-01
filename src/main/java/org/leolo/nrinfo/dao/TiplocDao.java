package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.dto.response.StationSearchResult;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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

    public List<StationSearchResult> doSimpleSearch(String query, int maxSize, int offSet) throws SQLException {
        query = query.toUpperCase();
        String processedQuery = query+"%";
        List<StationSearchResult> results = new ArrayList<StationSearchResult>();
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        """
                                ( 
                                  SELECT tiploc_code, crs_code, tps_description, 0 AS rank 
                                  FROM tiploc 
                                  WHERE crs_code = ? 
                                )
                                UNION ALL 
                                ( 
                                  SELECT tiploc_code, crs_code, tps_description, 1 AS rank
                                  FROM tiploc\s
                                  WHERE crs_code IS NOT NULL
                                    AND tps_description = ?
                                    AND crs_code <> ?
                                )
                                UNION ALL
                                (
                                  SELECT tiploc_code, crs_code, tps_description, 2 AS rank
                                  FROM tiploc
                                  WHERE crs_code IS NOT NULL
                                    AND tps_description LIKE ?
                                    AND crs_code <> ?
                                ) 
                                ORDER BY rank, tps_description
                                LIMIT ?,?
                                """
                )
        ) {
            ps.setString(1, query);
            ps.setString(2, processedQuery);
            ps.setString(3, query);
            ps.setString(4, processedQuery);
            ps.setString(5, query);
            ps.setInt(6, offSet);
            ps.setInt(7, maxSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new StationSearchResult(
                            rs.getString(3),
                            rs.getString(1),
                            rs.getString(2)
                    ));
                }
            }
        }
        return results;
    }

    public List<String> findAssociatedTiplocs(String tiploc) throws SQLException {
        ArrayList<String> tiplocs = new ArrayList<>();
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "SELECT group_member from v_auto_tiploc_group " +
                                "where given_code = ? and group_member <> ? " +
                                "order by group_member"
                )
        ) {
            ps.setString(1, tiploc);
            ps.setString(2, tiploc);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tiplocs.add(rs.getString(1));
                }
            }
        }
        return tiplocs;
    }

}
