package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.model.Configuration;
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
public class ConfigurationDao extends BaseDao {

    private Logger logger = LoggerFactory.getLogger(ConfigurationDao.class);

    @Autowired
    public DataSource dataSource;

    public Configuration getConfigurationByConfigurationName(String configurationName) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                    "select * from configuration where configuration_name = ?"
                )
        ) {
            preparedStatement.setString(1, configurationName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return parseConfiguration(resultSet);
                }
            }
        }
        return null;
    }

    private Configuration parseConfiguration(ResultSet resultSet) throws SQLException {
        Configuration configuration = new Configuration();
        configuration.setConfigurationName(resultSet.getString("configuration_name"));
        configuration.setConfigurationGroup(resultSet.getString("configuration_group"));
        configuration.setConfigurationValue(resultSet.getString("configuration_value"));
        configuration.setDataType(resultSet.getString("data_type"));
        configuration.setMaxCacheTime(resultSet.getInt("max_cache_time"));
        configuration.setUpdatedBy(resultSet.getString("updated_by"));
        configuration.setUpdatedDate(resultSet.getTimestamp("updated_date"));
        return configuration;
    }


}
