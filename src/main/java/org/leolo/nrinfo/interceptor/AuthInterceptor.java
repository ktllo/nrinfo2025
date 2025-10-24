package org.leolo.nrinfo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.leolo.nrinfo.service.APIAuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Enumeration;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    @Autowired
    private APIAuthenticationService authenticationService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Enumeration<String> authHeaders = request.getHeaders("Authorization");
        while (authHeaders.hasMoreElements()) {
            String authHeader = authHeaders.nextElement();
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String authToken = authHeader.substring(7);
                if (authToken.length() == 36) {
                    authenticationService.setAuthToken(authToken);
                    break;
                }
            }
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
