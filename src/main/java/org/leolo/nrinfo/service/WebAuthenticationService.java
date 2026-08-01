package org.leolo.nrinfo.service;

import jakarta.servlet.http.HttpSession;
import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.dao.UserDao;
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


    private int userId;

    private String username = null;

    public void loadSession(HttpSession session) {
        if (session == null) {
            return;
        }
        Object objUserId = session.getAttribute(Constants.Identifier.Session.USER_ID);
        if (objUserId instanceof Integer) {
            userId = (Integer) objUserId;
        }
    }

    public boolean isAuthenticated() {
        return userId != 0;
    }

}
