package org.leolo.nrinfo.service;

import lombok.Getter;
import org.leolo.nrinfo.dao.UserDao;
import org.leolo.nrinfo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import java.sql.SQLException;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class APIAuthenticationService {

    @Autowired private AuthenticationTokenService authenticationTokenService;
    @Autowired private UserPermissionService userPermissionService;
    @Autowired private UserDao userDao;
    private Logger logger = LoggerFactory.getLogger(APIAuthenticationService.class);

    private String authToken;
    private int userId;
    private boolean authenticated;

    private String username = null;

    public void removeToken() {
        authToken = null;
        userId = 0;
        authenticated = false;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
        userId = authenticationTokenService.getTokenOwner(authToken);
        if (userId == 0) {
            authenticated = false;
            userPermissionService.setUserId(0);
        } else {
            authenticated = true;
            authenticationTokenService.extendTokenLife(authToken);
            userPermissionService.setUserId(userId);
        }
    }

    public String getAuthToken() {
        return authToken;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getUsername() {
        if (username == null) {
            //TODO: Fetch username
            try {
                User user = userDao.getUserById(userId);
                username = user.getUsername();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return username;
    }
}
