package org.leolo.nrinfo.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;

@ToString
@Getter
@Setter
public class UserRegister {

    private String username;
    private String password;

    private String inviteKey;

    private HashMap<String,String> attributes;

    public boolean validate() {
        //TODO: Implement
        return username != null && password != null;
    }

}
