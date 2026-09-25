package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserPreference {
    private String preferenceName;
    private String displayName;
    private int displayOrder;
    private String dataType;
    private String validationClass;
    private String defaultValue;
    private String userValue;
    private List<UserPreferenceOption> options = new ArrayList<UserPreferenceOption>();


    public String getActualValue() {
        return userValue == null ? defaultValue : userValue;
    }
}
