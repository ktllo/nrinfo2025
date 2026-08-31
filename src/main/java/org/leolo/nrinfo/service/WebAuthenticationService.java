package org.leolo.nrinfo.service;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.dao.UserDao;
import org.leolo.nrinfo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WebAuthenticationService {
    @Autowired
    private UserPermissionService userPermissionService;
    @Autowired private UserDao userDao;

    private static Logger log = LoggerFactory.getLogger(WebAuthenticationService.class);


    @Getter
    private int userId;

    @Getter
    private String username = null;
    private boolean sessionLoaded = false;
    @Autowired
    private UserService userService;

    public void loadSession(HttpSession session) {
        if (session == null) {
            return;
        }
        Object objUserId = session.getAttribute(Constants.Identifier.Session.USER_ID);
        if (objUserId instanceof Integer) {
            userId = (Integer) objUserId;
        }
        if (userId != -1) {
            User user = userService.getUserById(userId);
            if (user != null) {
                username = user.getUsername();
            }
        }
        sessionLoaded = true;
    }

    public boolean isAuthenticated() {
        return userId != 0;
    }

    public boolean isReady() {
        return !sessionLoaded;
    }

}
