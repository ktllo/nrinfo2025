package org.leolo.nrinfo.service;

import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentUserService {

    @Getter private int userId;
    @Getter private boolean authenticated = false;

    public void setUserId(int userId) {
        this.userId = userId;
        this.authenticated = true;
    }

}
