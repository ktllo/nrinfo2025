package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Configuration {

    private String configurationName;
    private String configurationGroup;
    private String configurationValue;
    private String dataType;
    private int maxCacheTime;
    private String updatedBy;
    private Date updatedDate;

    public Configuration() {
    }

    public Configuration(String configurationName, String configurationGroup, String configurationValue, String dataType, int maxCacheTime, String updatedBy, Date updatedDate) {
        this.configurationName = configurationName;
        this.configurationGroup = configurationGroup;
        this.configurationValue = configurationValue;
        this.dataType = dataType;
        this.maxCacheTime = maxCacheTime;
        this.updatedBy = updatedBy;
        this.updatedDate = updatedDate;
    }

    public Configuration(String configurationName, String configurationGroup, String configurationValue, String dataType, int maxCacheTime) {
        this.configurationName = configurationName;
        this.configurationGroup = configurationGroup;
        this.configurationValue = configurationValue;
        this.dataType = dataType;
        this.maxCacheTime = maxCacheTime;
    }
}
