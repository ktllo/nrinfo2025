package org.leolo.nrinfo.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class TrainOperatorDao extends BaseDao {

    @Autowired
    private DataSource ds;

    private static Logger log = LoggerFactory.getLogger(TrainOperatorDao.class);

    public String getTrainOperatorName(String atocCode) throws SQLException {
        try (
                Connection connection = ds.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        """
                            SELECT name
                            FROM train_operator
                            WHERE atoc_code=?
                            ORDER BY priority
                            LIMIT 1
                            """
                )
                ) {
            ps.setString(1, atocCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    log.info("Mapped {} -> {}", atocCode, rs.getString("name"));
                    return rs.getString("name");
                }
            }
        }
        return null;
    }
}
