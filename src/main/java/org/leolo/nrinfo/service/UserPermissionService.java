package org.leolo.nrinfo.service;

import lombok.Getter;
import lombok.Setter;
import org.leolo.nrinfo.dao.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserPermissionService {

    private TreeSet<String> permissionCache = new TreeSet<>();
    private long cacheTime = Long.MIN_VALUE;
    @Getter private int userId;
    @Getter
    private boolean hasData = false;
    @Autowired private ConfigurationService configurationService;
    @Autowired private UserDao userDao;
    private final Object SYNC_TOKEN = new Object();
    private Logger logger = LoggerFactory.getLogger(UserPermissionService.class);

    private void checkCacheAge() {
        long maxAge = Long.parseLong(configurationService.getConfiguration("auth.permission_cache","120"))*1000;
        if (System.currentTimeMillis() - cacheTime > maxAge) {
            hasData = false;
            synchronized (SYNC_TOKEN) {
                permissionCache.clear();
            }
        }
    }

    private void loadCache() {
        synchronized (SYNC_TOKEN) {
            cacheTime = System.currentTimeMillis();
            try {
                permissionCache.addAll(userDao.getPermissionForUser(userId));
                hasData = true;
            } catch (SQLException e) {
                hasData = false;
                logger.error("Error loading user permission - {}", e.getMessage(), e);
            }
        }
    }

    public Set<String> getPermissionList() {
        TreeSet<String> permissions = new TreeSet<>();
        if (!hasData) {
            loadCache();
        }
        synchronized (SYNC_TOKEN) {
            permissions.addAll(permissionCache);
        }
        return permissions;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        hasData = false;
        synchronized (SYNC_TOKEN) {
            permissionCache.clear();
        }
    }


}
