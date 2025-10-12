package org.leolo.nrinfo.controller;

import org.leolo.nrinfo.service.APIAuthenticationService;
import org.leolo.nrinfo.service.ConfigurationService;
import org.leolo.nrinfo.service.UserPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdminController {

    @Autowired private APIAuthenticationService apiAuthenticationService;
    @Autowired private UserPermissionService userPermissionService;
    @Autowired private ConfigurationService configurationService;

    @RequestMapping("clear/cache")
    public ResponseEntity clearConfigCache() {
        if (!apiAuthenticationService.isAuthenticated()) {
            return ResponseUtil.buildUnauthorizedResponse();
        }
        if (!userPermissionService.hasPermission("PURGE_CONF_CACHE")) {
            return ResponseUtil.buildForbiddenResponse();
        }
        configurationService.clearCache();
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "OK"));
    }

}
