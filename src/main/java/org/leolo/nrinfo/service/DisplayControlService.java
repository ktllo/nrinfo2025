package org.leolo.nrinfo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("displayControlService")
public class DisplayControlService {

    @Autowired private UserPermissionService userPermissionService;

    public boolean hasPermission(String permission) {
        return userPermissionService.hasPermission(permission);
    }

}
