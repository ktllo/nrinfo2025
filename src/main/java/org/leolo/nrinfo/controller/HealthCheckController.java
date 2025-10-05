package org.leolo.nrinfo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeMap;

@RestController
@RequestMapping("/api")
public class HealthCheckController {

    @Autowired private DataSource dataSource;

    private Logger log = LoggerFactory.getLogger(HealthCheckController.class);

    @RequestMapping(
            value = "/healthcheck",
            method = RequestMethod.GET
    )
    public Object healthCheck() {
        TreeMap<String, Object> response = new TreeMap<String, Object>();
        response.put("status", "OK");
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT NOW()")
        ) {
            resultSet.next();
            response.put("current_time", resultSet.getString(1));
            response.put("database", "OK");

        } catch (SQLException e) {
            log.info(e.getMessage(), e);
            response.put("database", "FAIL");
        }
        return response;
    }

}
