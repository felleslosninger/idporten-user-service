package no.idporten.userservice.api;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private String error;
    private String errorDescription;
    private HttpStatus httpStatus;

    public ApiException(String error, String errorDescription, HttpStatus httpStatus) {
        this.error = error;
        this.errorDescription = errorDescription;
        this.httpStatus = httpStatus;
    }

}
