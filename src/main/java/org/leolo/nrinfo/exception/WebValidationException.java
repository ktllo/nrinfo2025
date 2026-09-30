package org.leolo.nrinfo.exception;

import lombok.Getter;

@Getter
public class WebValidationException extends Exception {

    private String source;
    public WebValidationException(String message) {
        super(message);
    }

    public WebValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebValidationException(String message, String source) {
        super(message);
        this.source = source;
    }

    public WebValidationException(String message, Throwable cause, String source) {
        super(message, cause);
        this.source = source;
    }
}
