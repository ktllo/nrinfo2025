package org.leolo.nrinfo.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class PasswordReset {
    private String oldPassword;
    private String newPassword;

    public boolean verify() {
        return oldPassword!= null && !oldPassword.isEmpty() && newPassword != null && !newPassword.isEmpty()
                && oldPassword.equals(newPassword);
    }
}
