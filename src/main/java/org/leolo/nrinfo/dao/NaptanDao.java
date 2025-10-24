package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.model.Naptan;
import org.leolo.nrinfo.model.Nptg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Collection;

@Repository
public class NaptanDao extends BaseDao{

    private Logger logger = LoggerFactory.getLogger(NaptanDao.class);

    @Autowired private DataSource dataSource;

    public void upsertNptg(Collection<Nptg> nptgs) throws SQLException {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement psChk = conn.prepareStatement(
                        "SELECT 1 FROM nptg_localities WHERE nptg_code = ?"
                );
                PreparedStatement psInsert = conn.prepareStatement(
                        "insert into nptg_localities (" +
                                "locality_name, short_name, qualifier_name, admin_area_code, district_code, " +
                                "grid_type, easting, northing, longitude, latitude, " +
                                "updated_date, revision_number, nptg_code" +
                                ") VALUES (" +
                                "?,?,?,?,?," +
                                "?,?,?,?,?," +
                                "?,?,?" +
                                ")"
                );
                PreparedStatement psUpdate = conn.prepareStatement(
                        "UPDATE nptg_localities SET " +
                                "locality_name=?, short_name=?, qualifier_name=?, admin_area_code=?, district_code=?, " +
                                "grid_type=?, easting=?, northing=?, longitude=?, latitude=?, " +
                                "updated_date=?, revision_number=? " +
                                "WHERE nptg_code=?"
                )
        ) {
            conn.setAutoCommit(false);
            int inserted = 0;
            int updated = 0;
            for (Nptg nptg : nptgs) {
                psChk.setString(1, nptg.getNptgCode());
                PreparedStatement psProc = null;
                try (ResultSet rs = psChk.executeQuery()) {
                    if (rs.next()) {
                        updated++;
                        psProc = psUpdate;
                    } else {
                        inserted++;
                        psProc = psInsert;
                    }
                }
                psProc.setString(1, nptg.getLocalityName());
                psProc.setString(2, nptg.getShortName());
                psProc.setString(3, nptg.getQualifierName());
                psProc.setString(4, nptg.getAdminAreaCode());
                psProc.setString(5, nptg.getDistrictCode());
                psProc.setString(6, nptg.getGridCode());
                psProc.setInt(7, nptg.getEasting());
                psProc.setInt(8, nptg.getNorthing());
                psProc.setDouble(9, nptg.getLongitude());
                psProc.setDouble(10, nptg.getLatitude());
//                psProc.setTimestamp(11, new Timestamp(nptg.getUpdatedDate().getTime()));
                setTimestamp(psProc, 11, nptg.getUpdatedDate());
                psProc.setInt(12, nptg.getRevisionNumber());
                psProc.setString(13, nptg.getNptgCode());
                psProc.addBatch();
            }
            long startTime = System.currentTimeMillis();
            psInsert.executeBatch();
            psUpdate.executeBatch();
            conn.commit();
            long endTime = System.currentTimeMillis();
            logger.info("Inserted {} and updated {} NPTG record(s) in {} ms", inserted, updated, endTime - startTime);
        }
    }

    public void upsertNaptan(Collection<Naptan> naptans) throws SQLException {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement psChk = conn.prepareStatement(
                        "SELECT 1 FROM naptan_stoppoint WHERE atoc_code = ?"
                );
                PreparedStatement psInsert = conn.prepareStatement(
                        "insert into naptan_stoppoint (" +
                                "naptan_code, plate_code, cleardown_code, common_name, short_common_name, " +
                                "landmark, street, crossing, indicator, bearing, " +
                                "nptg_code, town, suburb, country, locality_centre, " +
                                "grid_type, easting, northing, longitude, latitude, " +
                                "stop_type, bus_stop_type, updated_date, revision_number, atoc_code" +
                                ") VALUES (" +
                                "?,?,?,?,?," +
                                "?,?,?,?,?," +
                                "?,?,?,?,?," +
                                "?,?,?,?,?," +
                                "?,?,?,?,?" +
                                ")"
                );
                PreparedStatement psUpdate = conn.prepareStatement(
                        "update naptan_stoppoint set " +
                                "naptan_code = ?, plate_code = ?, cleardown_code = ?, common_name = ?, short_common_name = ?, " +
                                "landmark = ?, street = ?, crossing = ?, indicator = ?, bearing = ?, " +
                                "nptg_code = ?, town = ?, suburb = ?, country = ?, locality_centre = ?, " +
                                "grid_type = ?, easting = ?, northing = ?, longitude = ?, latitude = ?, " +
                                "stop_type = ?, bus_stop_type = ?, updated_date = ?, revision_number = ? " +
                                "where atoc_code = ?"
                )
        ) {
            conn.setAutoCommit(false);
            int inserted = 0;
            int updated = 0;
            PreparedStatement psProc = null;
            for (Naptan naptan : naptans) {
                psChk.setString(1, naptan.getAtocCode());
                try (ResultSet rs = psChk.executeQuery()) {
                    if (rs.next()) {
                        updated++;
                        psProc = psUpdate;
                    } else {
                        inserted++;
                        psProc = psInsert;
                    }
                }
                psProc.setString(1, naptan.getNaptanCode());
//                psProc.setString(2, naptan.getPlateCode());
                setStringWithMaxLength(psProc, 2, naptan.getPlateCode(), 12);
                setStringWithMaxLength(psProc, 3, naptan.getCleardownCode(), 12);
//                setString(psProc, 3, naptan.getCleardownCode());
//                psProc.setString(4, naptan.getCommonName());
//                psProc.setString(5, naptan.getShortCommonName());
//                psProc.setString(6, naptan.getLandmark());
//                psProc.setString(7, naptan.getStreet());
//                psProc.setString(8, naptan.getCrossing());
//                psProc.setString(9, naptan.getIndicator());
                setStringWithMaxLength(psProc, 4, naptan.getCommonName(), 48);
                setStringWithMaxLength(psProc, 5, naptan.getShortCommonName(), 48);
                setStringWithMaxLength(psProc, 6, naptan.getLandmark(), 48);
                setStringWithMaxLength(psProc, 7, naptan.getStreet(), 48);
                setStringWithMaxLength(psProc, 8, naptan.getCrossing(), 48);
                setStringWithMaxLength(psProc, 9, naptan.getIndicator(), 48);
                psProc.setString(10, naptan.getBearing());
                psProc.setString(11, naptan.getNptgCode());
//                psProc.setString(12, naptan.getTown());
//                psProc.setString(13, naptan.getSuburb());
//                psProc.setString(14, naptan.getCountry());
                setStringWithMaxLength(psProc, 12, naptan.getTown(), 48);
                setStringWithMaxLength(psProc, 13, naptan.getSuburb(), 48);
                setStringWithMaxLength(psProc, 14, naptan.getCountry(), 48);
//                psProc.setString(15, naptan.getLocalityCentre());
                setBooleanAsString(psProc, 15, naptan.getLocalityCentre());
                psProc.setString(16, naptan.getGridType());
                psProc.setInt(17, naptan.getEasting());
                psProc.setInt(18, naptan.getNorthing());
                psProc.setDouble(19, naptan.getLongitude());
                psProc.setDouble(20, naptan.getLatitude());
                psProc.setString(21, naptan.getStopType());
                psProc.setString(22, naptan.getBusStopType());
//                psProc.setTimestamp(23, new java.sql.Timestamp(naptan.getUpdatedDate().getTime()));
                setTimestamp(psProc, 23, naptan.getUpdatedDate());
                psProc.setInt(24, naptan.getRevisionNumber());
                psProc.setString(25, naptan.getAtocCode());
                psProc.addBatch();
            }
            long startTime = System.currentTimeMillis();
            psInsert.executeBatch();
            psUpdate.executeBatch();
            conn.commit();
            long endTime = System.currentTimeMillis();
            logger.info("Inserted {} and updated {} NaPTAN record(s) in {} ms", inserted, updated, endTime - startTime);
        }
    }
}
