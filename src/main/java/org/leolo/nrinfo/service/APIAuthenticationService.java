package org.leolo.nrinfo.service;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class APIAuthenticationService {

    @Autowired private AuthenticationTokenService authenticationTokenService;
    private Logger logger = LoggerFactory.getLogger(APIAuthenticationService.class);

    private String authToken;
    private int userId;
    private boolean authenticated;

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
        } else {
            authenticated = true;
            authenticationTokenService.extendTokenLife(authToken);
        }
        logger.info("PHEND: User {} AUTHED {}", userId, authenticated);
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
}
