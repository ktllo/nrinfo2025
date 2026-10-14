package org.leolo.nrinfo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OperationNotPermittedException extends RuntimeException {

    private final HttpStatus status;
    public OperationNotPermittedException(HttpStatus status,String message) {
        super(message);
        this.status = status;
    }

    public OperationNotPermittedException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public OperationNotPermittedException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public OperationNotPermittedException(Throwable cause) {
        super(cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public OperationNotPermittedException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
