package org.leolo.nrinfo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Permission {
    private int id;
    private String name;
    private String description;

    public org.leolo.nrinfo.dto.response.Permission convertToDTO() {
        return new org.leolo.nrinfo.dto.response.Permission(name, description);
    }
}
