package org.leolo.nrinfo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class User {
    private int userId;
    private String username;
    private String passwordDate;
    private String lastLoginDate;

    @JsonInclude(JsonInclude.Include.NON_EMPTY) private List<String> permission;
    @JsonInclude(JsonInclude.Include.NON_EMPTY) private List<String> role;

    @JsonInclude(JsonInclude.Include.NON_EMPTY) private Map<String, String> preference;
    @JsonInclude(JsonInclude.Include.NON_EMPTY) private Map<String, UserAttribute> attribute;


}
