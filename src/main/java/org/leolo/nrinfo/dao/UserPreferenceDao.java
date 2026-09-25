package org.leolo.nrinfo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.leolo.nrinfo.model.UserPreference;
import org.leolo.nrinfo.model.UserPreferenceOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class UserPreferenceDao extends BaseDao {

    private static Logger log = LoggerFactory.getLogger(UserPreferenceDao.class);

    @Autowired
    private DataSource dataSource;

    public List<UserPreference> getAllUserPreferenceForUserByUserId(int userId) throws SQLException {
        List<UserPreference> userPreferences = new ArrayList<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(
                        """
                            SELECT
                                p.pref_name, p.display_name, p.display_order, p.data_type, p.validation_class,
                                p.default_value, up.pref_value as user_value
                            FROM
                                preference p
                                left outer join user_preference up on p.pref_name = up.pref_name and up.user_id = ?
                            ORDER BY
                                p.display_order, p.pref_name
                            """
                )
        ) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    userPreferences.add(parseUserPreference(rs));
                }
            }
        }
        return userPreferences;
    }

    public UserPreference getUserPreferenceForUser(int userId, String prefName) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(
                        """
                           SELECT
                                p.pref_name, p.display_name, p.display_order, p.data_type, p.validation_class,
                                p.default_value, up.pref_value as user_value
                            FROM
                                preference p
                                left outer join user_preference up on p.pref_name = up.pref_name and up.user_id = ?
                           WHERE
                               p.pref_name = ?
                           """
                )
        ) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, prefName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return parseUserPreference(rs);
                }
            }
        }
        return null;
    }

    public boolean upsertUserPreferenceValue(int userId, String prefName, String value) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement psChk = connection.prepareStatement(
                        """
                            SELECT pref_value FROM user_preference WHERE user_id = ? AND pref_name = ?
                            """
                );
                PreparedStatement psIns = connection.prepareStatement(
                        """
                            INSERT INTO user_preference (pref_value, update_date,user_id, pref_name)
                            VALUES (?, NOW(), ?, ?)
                            """
                );
                PreparedStatement psUpd = connection.prepareStatement(
                        """
                            UPDATE user_preference
                            SET pref_value = ?, update_date = NOW()
                            WHERE user_id = ? AND pref_name = ?
                            """
                )
        ) {
            psChk.setInt(1, userId);
            psChk.setString(2, prefName);
            try (ResultSet rs = psChk.executeQuery()) {
                PreparedStatement ps;
                if (rs.next()) {
                    if (!rs.getString("pref_value").equals(value)) {
                        ps = psUpd;
                    } else {
                        log.debug("Not upserting user preference {} because pref_value is the same", prefName);
                        return false;
                    }
                } else {
                    ps = psIns;
                }
                ps.setString(1, value);
                ps.setInt(2, userId);
                ps.setString(3, prefName);
                int rows = ps.executeUpdate();
                log.info("Upserted user preference ({},{}), rows affected: {}", userId, prefName, rows);
                return rows > 0;
            }
        }
    }

    public List<UserPreferenceOption> getPreferenceOptionsByPreferenceName(String preferenceName) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(
                        """
                            SELECT
                                po.display_value, po.mapped_value, po.sequence
                            FROM
                                preference_option po
                            WHERE
                                po.preference_name = ?
                            ORDER BY
                                po.sequence
                            """
                )
        ) {
            pstmt.setString(1, preferenceName);
            List<UserPreferenceOption> userPreferenceOptions = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserPreferenceOption userPreferenceOption = new UserPreferenceOption();
                    userPreferenceOption.setDisplayName(rs.getString("display_value"));
                    userPreferenceOption.setMappedValue(rs.getString("mapped_value"));
                    userPreferenceOption.setSequence(rs.getInt("sequence"));
                    userPreferenceOptions.add(userPreferenceOption);
                }
            }
            return userPreferenceOptions;
        }
    }

    public Set<String> getAllPreferenceNames() throws SQLException {
        Set<String> preferenceNames = new HashSet<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(
                        """
                            SELECT
                                pref_name
                            FROM
                                preference
                            """
                );
                ResultSet rs = pstmt.executeQuery()
        ) {
            while (rs.next()) {
                preferenceNames.add(rs.getString(1));
            }
        }
        return preferenceNames;
    }

    private UserPreference parseUserPreference(ResultSet rs) throws SQLException {
        UserPreference userPreference = new UserPreference();
        userPreference.setPreferenceName(rs.getString("pref_name"));
        userPreference.setDisplayName(rs.getString("display_name"));
        userPreference.setDisplayOrder(rs.getInt("display_order"));
        userPreference.setDataType(rs.getString("data_type"));
        userPreference.setValidationClass(rs.getString("validation_class"));
        userPreference.setDefaultValue(rs.getString("default_value"));
        userPreference.setUserValue(rs.getString("user_value"));
        return userPreference;
    }

}
