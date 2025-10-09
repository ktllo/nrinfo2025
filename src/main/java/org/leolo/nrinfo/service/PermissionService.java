package org.leolo.nrinfo.service;

import org.leolo.nrinfo.dao.PermissionDao;
import org.leolo.nrinfo.model.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Hashtable;

@Service
public class PermissionService {

    @Autowired private PermissionDao permissionDao;

    private final Hashtable<String, Permission> cache = new Hashtable<>();
    private Logger logger = LoggerFactory.getLogger(PermissionService.class);

    public Permission getPermission(String permissionName) {
        Permission permission = cache.get(permissionName);
        if (permission == null) {
            try {
                permission = permissionDao.getPermissionByName(permissionName);
                cache.put(permissionName, permission);
            } catch (SQLException e) {
                logger.error("Unable to get permission info for  {} - {}", permissionName, e.getMessage());
            }
        }
        return permission;
    }

}
