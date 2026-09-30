package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.Constants;
import org.leolo.nrinfo.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
