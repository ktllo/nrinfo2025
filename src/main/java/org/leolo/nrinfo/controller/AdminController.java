package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.model.User;
import org.leolo.nrinfo.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdminController {

    @Autowired private APIAuthenticationService apiAuthenticationService;
    @Autowired private UserPermissionService userPermissionService;
    @Autowired private ConfigurationService configurationService;
    @Autowired private TiplocService tiplocService;

    private Logger logger = LoggerFactory.getLogger(AdminController.class);
    @Autowired
    private UserService userService;

    @RequestMapping("clear/cache")
    public ResponseEntity clearConfigCache() {
        if (!apiAuthenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!userPermissionService.hasPermission(Constants.Permission.PURGE_CACHE)) {
            return ResponseUtil.buildForbiddenResponse();
        }
        configurationService.clearCache();
        tiplocService.clearCache();
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "OK"));
    }

    @RequestMapping("invite/generate")
    public ResponseEntity generateInviteKey(
            @RequestParam(name = "count", required = false, defaultValue = "1") String count
    ) {
        logger.debug("generateInviteKey - count: {}", count);
        if (!apiAuthenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!userPermissionService.hasPermission(Constants.Permission.GENERATE_INVITE_KEYS)) {
            return ResponseUtil.buildForbiddenResponse();
        }
        try {
            int keyCount = Integer.parseInt(count);
            if (keyCount <= 0) {
                return ResponseUtil.buildBadRequestResponse("count must be a positive integer");
            }
            if (keyCount > configurationService.getInt("max_invite_key", 10)) {
                return ResponseUtil.buildBadRequestResponse("count is too big");
            }
            List<String> generatedKeys = userService.generateInviteKeys(keyCount);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "success", "generated_keys", generatedKeys));
        } catch (NumberFormatException e) {
            return ResponseUtil.buildBadRequestResponse("count must be a positive integer");
        }
    }

    @RequestMapping("/admin/users")
    public ResponseEntity getUsers(@RequestParam("q") String q) {
        if (!apiAuthenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!userPermissionService.hasPermission(Constants.Permission.VIEW_OTHER_USERS)) {
            return ResponseUtil.buildForbiddenResponse();
        }
        List<User> users = userService.searchUsers(q);
        List<org.leolo.nrinfo.dto.response.User> displayUsers = new ArrayList<>();
        SimpleDateFormat dfFullDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (User user : users) {
            org.leolo.nrinfo.dto.response.User dto = new org.leolo.nrinfo.dto.response.User();
            dto.setUserId(user.getUserId());
            dto.setUsername(user.getUsername());
            if (user.getLastLoginDate() != null) {
                dto.setLastLoginDate(dfFullDate.format(user.getLastLoginDate()));
            }
            if (user.getLastPasswordDate() != null) {
                dto.setPasswordDate(dfFullDate.format(user.getLastPasswordDate()));
            }
            displayUsers.add(dto);
        }
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "success", "users", displayUsers));
    }

    @RequestMapping("/admin/users/{uid}")
    public ResponseEntity getUsersDetails(@PathVariable("uid") String strUid) {
        if (!apiAuthenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!userPermissionService.hasPermission(Constants.Permission.VIEW_OTHER_USERS)) {
            return ResponseUtil.buildForbiddenResponse();
        }
        logger.debug("getUsersDetails - uid: {}", strUid);
        int uid;
        try {
            uid = Integer.parseInt(strUid);
        } catch (NumberFormatException e) {
            return ResponseUtil.buildBadRequestResponse("uid must be a positive integer");
        }
        try {
            User user = userService.getUserById(uid);
            if (user == null) {
                return ResponseUtil.buildNotFoundResponse();
            }
            org.leolo.nrinfo.dto.response.User dto = new org.leolo.nrinfo.dto.response.User();
            dto.setUserId(user.getUserId());
            dto.setUsername(user.getUsername());
            SimpleDateFormat dfFullDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (user.getLastLoginDate() != null) {
                dto.setLastLoginDate(dfFullDate.format(user.getLastLoginDate()));
            }
            if (user.getLastPasswordDate() != null) {
                dto.setPasswordDate(dfFullDate.format(user.getLastPasswordDate()));
            }
            dto.setPermission(userService.getUserPermissions(uid).stream().toList());
            dto.setRole(userService.getUserRole(uid).stream().toList());
            dto.setPreference(userService.getUserPreference(uid));
            dto.setAttribute(userService.getUserAttribute(uid));
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("status", "success", "user", dto));
        } catch (Exception e) {
            logger.error("Error while getting user details - {}", e.getMessage(), e);
            return ResponseUtil.buildInternalServerErrorResponseForDatabaseError();
        }
    }


}
