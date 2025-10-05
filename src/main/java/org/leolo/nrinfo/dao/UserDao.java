package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.model.User;
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
