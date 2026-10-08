package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.model.UserAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserAttributeDao extends BaseDao {

    @Autowired
    private DataSource dataSource;

    public Map<String, UserAttribute> getAllAttributeForUser(int userId) throws SQLException {
        HashMap<String, UserAttribute> attributeMap = new HashMap<String, UserAttribute>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        """
                            SELECT
                                ua.attribute_id,
                                attribute_name,
                                COALESCE(uav.attribute_value, ga.attribute_value, ua.default_value) as value
                            FROM
                                user u
                                CROSS JOIN user_attribute ua
                                LEFT OUTER JOIN group_attribute ga ON ua.attribute_id = ga.attribute_id AND u.group_id = ga.group_id
                                LEFT OUTER JOIN user_attribute_value uav ON ua.attribute_id=uav.attribute_id AND uav.user_id = u.user_id
                            WHERE
                                u.user_id = ?
                            """
                )
        ) {
            preparedStatement.setInt(1, userId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    UserAttribute userAttribute = new UserAttribute();
                    userAttribute.setAttributeId(rs.getInt("attribute_id"));
                    userAttribute.setAttributeName(rs.getString("attribute_name"));
                    userAttribute.setAttributeValue(rs.getString("value"));
                    attributeMap.put(rs.getString("attribute_name"), userAttribute);
                }
            }
        }
        return attributeMap;
    }

    public Map<String, org.leolo.nrinfo.dto.response.UserAttribute> getUserAttributeForDisplay(int userId) throws SQLException {
        Map<String, org.leolo.nrinfo.dto.response.UserAttribute> attributeMap = new HashMap<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        """
                            SELECT
                                ua.attribute_name,
                                ua.default_value,
                                ga.attribute_value as group_value,
                                uav.attribute_value as user_value
                            FROM
                                user u
                                CROSS JOIN user_attribute ua
                                LEFT OUTER JOIN group_attribute ga ON ua.attribute_id = ga.attribute_id AND u.group_id = ga.group_id
                                LEFT OUTER JOIN user_attribute_value uav ON ua.attribute_id=uav.attribute_id AND uav.user_id = u.user_id
                            WHERE
                                u.user_id = ?
                            """
                )
        ) {
            preparedStatement.setInt(1, userId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    org.leolo.nrinfo.dto.response.UserAttribute userAttribute = new org.leolo.nrinfo.dto.response.UserAttribute();
                    userAttribute.setAttributeName(rs.getString("attribute_name"));
                    userAttribute.setDefaultValue(rs.getString("default_value"));
                    userAttribute.setGroupValue(rs.getString("group_value"));
                    userAttribute.setUserValue(rs.getString("user_value"));
                    attributeMap.put(rs.getString("attribute_name"), userAttribute);
                }
            }
        }
        return attributeMap;
    }

}
