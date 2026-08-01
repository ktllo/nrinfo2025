package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.model.User;
import org.leolo.nrinfo.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

@Repository
public class UserDao extends BaseDao{

    private static Logger logger = LoggerFactory.getLogger(UserDao.class);

    @Autowired private DataSource dataSource;

    public User getUserByUsername(String username) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select * from user where username = ?"
                )
        ) {
            preparedStatement.setString(1, username);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return parseUser(rs);
                }
            }
        }
        return null;
    }

    public void markLoginSuccess(int userId) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "update user set " +
                                "last_login = now(), failed_login_count = 0, last_failed_login = null " +
                                "where user_id = ?"
                )
        ) {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        }
    }

    public void markLoginFail(int userId) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "update user set " +
                                "last_failed_login = now(), failed_login_count = failed_login_count + 1 " +
                                "where user_id = ?"
                )
        ) {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        }
    }

    public Set<String> getPermissionForUser(User user) throws SQLException {
        return  getPermissionForUser(user.getUserId());
    }

    public Set<String> getPermissionForUser(int userId) throws SQLException {
        logger.debug("getPermissionForUser - {}", userId);
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select permission_name " +
                                "from " +
                                "user_permission up " +
                                "join permission p ON up.permission_id = p.permission_id " +
                                "where user_id = ? " +
                                "union all " +
                                "select permission_name " +
                                "from " +
                                "permission p " +
                                "join role_permission rp on p.permission_id = rp.permission_id " +
                                "join user_role ur on rp.role_id = ur.role_id " +
                                "where user_id = ?"
                )
        ) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, userId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                Set<String> permissions = new HashSet<>();
                while (rs.next()) {
                    permissions.add(rs.getString("permission_name"));
                }
                return permissions;
            }
        }
    }

    public User getUserById(int userId) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select * from user where user_id = ?"
                )
        ) {
            preparedStatement.setInt(1, userId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return parseUser(rs);
                }
            }
        }
        return null;
    }

    public void updatePassword(int userId, String hashedPassword) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "update user set password = ?, updated_date = now(), last_password_date=NOW() where user_id = ?"
                )
        ){
            preparedStatement.setString(1, hashedPassword);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
        }
    }

    public int getUserIdByPasswordResetToken(String token) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "SELECT user_id " +
                                "FROM password_reset_request " +
                                "WHERE request_key = ? and expiry_date > NOW() and status = 'A'"
                )
        ) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        return -1;
    }

    public void insertPasswordReset(String token, int userId, int validity) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement psOve = connection.prepareStatement(
                        "delete from password_reset_request  where user_id = ?"
                );
                PreparedStatement psIns = connection.prepareStatement(
                        "insert into password_reset_request (" +
                                "request_id, request_key, user_id, requested_date, expiry_date, processed_date, status) " +
                                "values (?, ?, ?, NOW(), NOW() + INTERVAL ? SECOND , null, 'A')"
                )
        ) {
            connection.setAutoCommit(false);
            psOve.setInt(1, userId);
            psOve.executeUpdate();
            psIns.setBytes(1, CommonUtil.uuidToBytes(CommonUtil.generateUUID()));
            psIns.setString(2, token);
            psIns.setInt(3, userId);
            psIns.setInt(4, validity);
            psIns.executeUpdate();
            connection.commit();
        }
    }

    public int getInviteKeyUseLeft(String inviteKey) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "select invite_keys.use_left from invite_keys where invite_key = ? and (expiry_date > NOW() or expiry_date is null)"
                )
        ) {
            preparedStatement.setString(1, inviteKey);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    int useLeft = rs.getInt("use_left");
                    if (useLeft > 0) {
                        return useLeft;
                    }
                    return Integer.MAX_VALUE;
                }
            }
        }
        logger.info("No matching key found");
        return -1;
    }

    public int createUser(String userName, String hashedPassword) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO user (" +
                                "username, password, last_password_date, created_date, updated_date," +
                                "force_password_change, last_login, failed_login_count, last_failed_login)" +
                                "values (" +
                                "?,?, NOW(), now(), now()," +
                                "0, null,0,null" +
                                ")",
                        Statement.RETURN_GENERATED_KEYS
                )
        ) {
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, hashedPassword);
            preparedStatement.executeUpdate();
            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public boolean addRole(int userId, String role) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement psRoleId = connection.prepareStatement(
                        "select role_id from role where role_name = ?"
                );
                PreparedStatement psInsert = connection.prepareStatement(
                        "INSERT INTO user_role (user_id, role_id) VALUES (?,?)"
                )
        ) {
            psRoleId.setString(1, role);
            try (ResultSet rs = psRoleId.executeQuery()) {
                if (rs.next()) {
                    int roleId = rs.getInt("role_id");
                    psInsert.setInt(1, userId);
                    psInsert.setInt(2, roleId);
                    return psInsert.executeUpdate() > 0;
                } else {
                    logger.info("Role {} not found", role);
                    return false;
                }
            }
        }
    }

    private User parseUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setLastPasswordDate(rs.getDate("last_password_date"));
        user.setCreateDate(rs.getDate("created_date"));
        user.setLastLoginDate(rs.getDate("last_login"));
        user.setLastUpdateDate(rs.getDate("updated_date"));
        user.setForcePasswordChange(rs.getBoolean("force_password_change"));
        user.setFailedLoginCount(rs.getInt("failed_login_count"));
        user.setLastFailedLoginDate(rs.getDate("last_failed_login"));
        return user;
    }

}
