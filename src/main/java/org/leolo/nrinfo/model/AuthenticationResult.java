package org.leolo.nrinfo.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthenticationResult {

    private boolean success;
    private String message;
    private int userId;
    private String username;

    public AuthenticationResult(boolean success, int userId, String username) {
        this.success = success;
        this.userId = userId;
        this.username = username;
    }

    public AuthenticationResult(boolean success, String message, int userId, String username) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.username = username;
    }

    public AuthenticationResult() {
    }

    public void appendMessage(String message) {
        this.message = this.message.concat(message);
    }
}
