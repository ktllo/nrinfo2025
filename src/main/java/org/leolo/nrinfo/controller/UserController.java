package org.leolo.nrinfo.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.leolo.nrinfo.dto.request.PasswordReset;
import org.leolo.nrinfo.dto.request.UserRegister;
import org.leolo.nrinfo.model.AuthenticationResult;
import org.leolo.nrinfo.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired private ConfigurationService configurationService;
    @Autowired private UserService userService;
    @Autowired private AuthenticationTokenService authenticationTokenService;
    @Autowired private APIAuthenticationService authenticationService;
    @Autowired private UserPermissionService userPermissionService;
    private Logger authLogger = LoggerFactory.getLogger("auth");

    @RequestMapping(path = "register", method = RequestMethod.POST)
    public ResponseEntity register(
            HttpServletRequest request,
            @RequestBody UserRegister user
    ) {
        if (authenticationService.isAuthenticated()) {
            //User is authenticated
            return ResponseUtil.buildForbiddenResponse();
        }
        if (!user.validate()) {
            return ResponseUtil.buildFullErrorResponse("Invalid Request", "Your request is invalid, please reference to API documentation");
        }
        if (!configurationService.getBoolean("allow_register")) {
            //Check invite key
            if (user.getInviteKey() == null || user.getInviteKey().isEmpty()) {
                return ResponseUtil.buildFullErrorResponse("Invalid Request", "You must be invited to register first");
            }
        }
        try {
            AuthenticationResult ar = userService.doRegisterUser(user);
            if (ar.isSuccess()) {
                TreeMap<String, String> map = new TreeMap<>();
                map.put("success", "true");
                map.put("message", ar.getMessage());
                map.put("token", authenticationTokenService.generateTokenForUser(ar.getUserId()));
                authLogger.info("SUCCESS;{};{};{}", ar.getUserId(), request.getRemoteAddr(), ar.getMessage());
                return new ResponseEntity<>(map, HttpStatus.OK);
            }
        } catch (SQLException e) {
            TreeMap<String, String> map = new TreeMap<>();
            map.put("success", "false");
            map.put("message", e.getMessage());
            return new ResponseEntity<>(map, HttpStatus.OK);
        }
        return ResponseUtil.buildNotImplementedResponse();
    }

    @RequestMapping(path = "changepassword", method = RequestMethod.POST)
    public ResponseEntity changePassword(
            HttpServletRequest request,
            @RequestBody PasswordReset pwdReset
    ) {
        if (!authenticationService.isAuthenticated()) {
            //User is not authenticated
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!pwdReset.verify()) {
            return ResponseUtil.buildFullErrorResponse("Invalid Request", "Your request is invalid, please reference to API documentation");
        }
        int userId = authenticationService.getUserId();
        if(userPermissionService.hasPermission("FORBID_PWD_CHG")) {
            return ResponseUtil.buildForbiddenResponse();
        }
        userService.changePassword(userId, pwdReset.getOldPassword(), pwdReset.getNewPassword());
        return ResponseEntity.ok(Map.of("result","success"));
    }

}
