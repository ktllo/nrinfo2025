package org.leolo.nrinfo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Permission implements Comparable<Permission> {
    private String name;
    private String description;


    @Override
    public int compareTo(Permission o) {
        return name.compareTo(o.name);
    }
}
