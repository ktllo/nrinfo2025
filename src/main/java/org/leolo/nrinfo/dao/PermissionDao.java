package org.leolo.nrinfo.dao;

import org.leolo.nrinfo.model.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Repository
public class PermissionDao {

    @Autowired private DataSource dataSource;

    private Map<String, Permission> permissionMap;

    public Permission getPermissionByName(String name) throws SQLException {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement("select * from permission where permission_name = ?");
        ) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Permission(
                            rs.getInt("permission_id"),
                            rs.getString("permission_name"),
                            rs.getString("description")
                    );
                }
            }
        }
        return null;
    }

}
