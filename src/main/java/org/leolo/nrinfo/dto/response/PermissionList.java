package org.leolo.nrinfo.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.TreeSet;

@Getter
public class PermissionList {

    @Setter
    private String status = "OK";
    private final TreeSet<Permission> permissions = new TreeSet<>();

}
