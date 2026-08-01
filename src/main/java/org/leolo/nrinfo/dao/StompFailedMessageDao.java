package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@Repository
public class StompFailedMessageDao extends BaseDao{

    private static final Logger log = LoggerFactory.getLogger(StompFailedMessageDao.class);

    @Autowired
    private DataSource dataSource;

    public void insertFailedMessage(
            Class<?> errorSource,
            String messageBody,
            Throwable throwable
    ) throws SQLException, IOException {
        insertFailedMessage(
                errorSource.getName(),
                messageBody,
                throwable
        );
    }

    public void insertFailedMessage(
            String errorSource,
            String messageBody,
            Throwable throwable
    ) throws SQLException, IOException {
        UUID uuid = CommonUtil.generateUUID();
        String stackTrace = null;
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter pw = new PrintWriter(baos)
        ) {
            if (throwable != null) {
                throwable.printStackTrace(pw);
                pw.flush();
                stackTrace = baos.toString();
            }
        }
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO stomp_failed_message (" +
                                "message_id, message_date, error_source, error_trace, message_body) VALUES (" +
                                "?, NOW(), ?, ?, ?" +
                                ")"
                )
        ) {
            conn.setAutoCommit(false);
            ps.setBytes(1, CommonUtil.uuidToBytes(uuid));
            ps.setString(2, errorSource);
            ps.setString(3, stackTrace);
            ps.setString(4, messageBody);
            ps.executeUpdate();
            conn.commit();
        }
    }

}
