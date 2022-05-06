package no.idporten.userservice.api;

import lombok.Getter;

@Getter
public class UserExistsException extends RuntimeException {
    private String message;
    private Throwable throwable;

    public UserExistsException() {
        super();
    }

    public UserExistsException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
