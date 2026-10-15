package org.leolo.nrinfo.service;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentUserService {

    @Getter private int userId;
    @Getter private boolean authenticated = false;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void setUserId(int userId) {
        if (userId == 0) {
            return;
        }
        this.userId = userId;
        this.authenticated = true;
    }

}
