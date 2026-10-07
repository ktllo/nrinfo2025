package org.leolo.nrinfo.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAttribute {
    private String attributeName;
    private String defaultValue;
    private String groupValue;
    private String userValue;

    public String getActualValue() {
        if (userValue != null)
            return userValue;
        if (groupValue != null)
            return groupValue;
        return defaultValue;
    }
}
