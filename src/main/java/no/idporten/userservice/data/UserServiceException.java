package no.idporten.userservice.data;

import no.idporten.userservice.api.ApiException;
import org.springframework.http.HttpStatus;

public class UserServiceException extends ApiException {

    public UserServiceException(String error, String errorDescription, HttpStatus httpStatus) {
        super(error, errorDescription, httpStatus);
    }

    public static UserServiceException duplicateUser() {
        return new UserServiceException("invalid_request", "User already exists.", HttpStatus.CONFLICT);
    }

    public static UserServiceException duplicateUser(String message) {
        return new UserServiceException("invalid_request", message, HttpStatus.CONFLICT);
    }

    public static UserServiceException userNotFound() {
        return new UserServiceException("invalid_request", "User not found.", HttpStatus.NOT_FOUND);
    }

    public static UserServiceException userNotFound(String message) {
        return new UserServiceException("invalid_request", message, HttpStatus.NOT_FOUND);
    }

    public static UserServiceException invalidUserData(String message) {
        return new UserServiceException("invalid_request", message, HttpStatus.BAD_REQUEST);
    }

}
