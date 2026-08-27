package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchParameter {

    private int type;
    private Object value;

    public SearchParameter(int type, Object value) {
        this.type = type;
        this.value = value;
    }

}
