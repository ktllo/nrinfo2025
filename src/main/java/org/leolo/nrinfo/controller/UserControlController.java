package org.leolo.nrinfo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.leolo.nrinfo.dto.response.PermissionList;
import org.leolo.nrinfo.model.AuthenticationResult;
import org.leolo.nrinfo.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class UserControlController {

    private Logger logger = LoggerFactory.getLogger(UserControlController.class);

    @Autowired private UserService userService;
    @Autowired private AuthenticationTokenService authenticationTokenService;
    @Autowired private APIAuthenticationService apiAuthenticationService;
    @Autowired private UserPermissionService userPermissionService;
    @Autowired
    private PermissionService permissionService;

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public Object logout() {
        String token = apiAuthenticationService.getAuthToken();
        if (token != null) {
            authenticationTokenService.invalidateToken(token);
        }
        TreeMap<String, String> map = new TreeMap<>();
        map.put("success", "true");
        map.put("message", "Logout successful");
        return map;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public DeferredResult<ResponseEntity<?>> login(
            HttpServletRequest request,
            @RequestParam String username,
            @RequestParam String password
    ) {
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        CompletableFuture.runAsync(() -> {
            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                logger.debug("Bad request - username or password is empty");
                TreeMap<String, String> map = new TreeMap<>();
                map.put("success", "false");
                map.put("message", "Bad request - username or password is empty");
                deferredResult.setResult(ResponseEntity.badRequest().body(map));
                return;
            }
            if (request.getHeaders("Authorization").hasMoreElements()) {
                logger.debug("Bad request - already logged in");
                TreeMap<String, String> map = new TreeMap<>();
                map.put("success", "false");
                map.put("message", "Bad request - already logged in");
                deferredResult.setResult(ResponseEntity.status(HttpServletResponse.SC_CONFLICT).body(map));
                return;
            }
            AuthenticationResult ar = userService.authenticate(username, password);
            if (ar.isSuccess()) {
                TreeMap<String, String> map = new TreeMap<>();
                map.put("success", "true");
                map.put("message", ar.getMessage());
                map.put("token", authenticationTokenService.generateTokenForUser(ar.getUserId()));
                deferredResult.setResult(ResponseEntity.status(HttpServletResponse.SC_OK).body(map));
            } else {
                TreeMap<String, String> map = new TreeMap<>();
                map.put("success", "false");
                map.put("message", ar.getMessage());
                deferredResult.setResult(ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(map));
            }
        });
        return deferredResult;
    }

    @RequestMapping("/users/permission")
    public ResponseEntity getUserPermissions() {
        if (!apiAuthenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        Set<String> permissions = userPermissionService.getPermissionList();
        PermissionList permissionList = new PermissionList();
        permissionList.setStatus("OK");
        for (String permission : permissions) {
            permissionList.getPermissions().add(permissionService.getPermission(permission).convertToDTO());
        }
        return ResponseEntity.ok(permissionList);
    }
}
