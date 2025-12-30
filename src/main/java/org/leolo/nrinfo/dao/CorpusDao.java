package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.model.Corpus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

@Repository
public class CorpusDao extends BaseDao{

    @Autowired private DataSource ds;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public DatabaseOperationResult upsertCorpus(Corpus[] corpuses) throws SQLException {
        int inserted = 0;
        int updated = 0;
        DatabaseOperationResult result = new DatabaseOperationResult();
        try (
                Connection conn = ds.getConnection();
                PreparedStatement psChk = conn.prepareStatement(
                        "SELECT 1 FROM corpus WHERE nlc_code = ?"
                );
                PreparedStatement psIns = conn.prepareStatement(
                        "INSERT INTO corpus (stanox, uic_code, crs_code, tiploc_code, nlc_desc, nlc_desc_16, nlc_code) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)"
                );
                PreparedStatement psUpd = conn.prepareStatement(
                        "UPDATE corpus SET " +
                                "stanox=?, uic_code=?, crs_code=?, tiploc_code=?, nlc_desc=?, nlc_desc_16=? " +
                                "WHERE nlc_code=?"
                )
        ) {
            conn.setAutoCommit(false);
            HashSet<String> insertedKeys = new HashSet<>();
            int batchSize = 0;
            for (Corpus corpus : corpuses) {
                psChk.setString(1, corpus.getNlcCode());
                PreparedStatement psOpt = null;
                try (ResultSet rs = psChk.executeQuery()) {
                    if (rs.next()) {
                        psOpt = psUpd;
                        updated++;
                    } else {
                        if (insertedKeys.contains(corpus.getNlcCode())) {
                            logger.warn("Duplicated key: {}; New Data: {}", corpus.getNlcCode(), corpus);
                            continue;
                        }
                        insertedKeys.add(corpus.getNlcCode());
                        psOpt = psIns;
                        inserted++;
                    }
                }
                batchSize++;
                setString(psOpt, 1, corpus.getStanoxCode());
                setString(psOpt, 2, corpus.getUicCode());
                setString(psOpt, 3, corpus.getCrsCode());
                setString(psOpt, 4, corpus.getTiplocCode());
                setString(psOpt, 5, corpus.getNlcDescription());
                setString(psOpt, 6, corpus.getShortNlcDescription());
                psOpt.setString(7, corpus.getNlcCode());
                psOpt.addBatch();
                if (batchSize >= 1000) {
                    psIns.executeBatch();
                    psUpd.executeBatch();
                    conn.commit();
                    batchSize = 0;
                    result = result.add(new DatabaseOperationResult(true, inserted, updated, 0));
                    logger.info("Batch executed");
                    inserted = 0;
                    updated = 0;
                }
            }
            psIns.executeBatch();
            psUpd.executeBatch();
            conn.commit();
            result = result.add(new DatabaseOperationResult(true, inserted, updated, 0));
        }
        return result;
    }

}
