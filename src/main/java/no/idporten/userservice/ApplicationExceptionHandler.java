package no.idporten.userservice;

import lombok.extern.slf4j.Slf4j;
import no.idporten.userservice.api.ErrorResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletResponse;


/**
 * Top level exception handler for application.  Handles generic error situations.
 */
@Slf4j
@ControllerAdvice
@Order(100)
public class ApplicationExceptionHandler {

    //spring security 403
    @ExceptionHandler({ AccessDeniedException.class })
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request, HttpServletResponse response) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .error("invalid_scope") // might be something else as well???
                        .errorDescription(ex.getMessage())
                        .build());
    }

    //spring security 401
/*    @ExceptionHandler({ AuthenticationException.class })
    public ResponseEntity<Object> handleAccessDeniedException(AuthenticationException ex, WebRequest request) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .error("invalid_token")
                        .errorDescription(ex.getMessage())
                        .build());
    }*/

    // Spring 405
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.builder()
                        .error("invalid_request")
                        .errorDescription("Unsupported HTTP method").build());
    }

    // Spring 404
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .error("invalid_request")
                        .errorDescription("Requested resource not found")
                        .build());
    }

    // Spring-exception som gir HTTP-feil
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
        return errorResponseEntity(e.getStatus(), errorMessageForHttpStatus(e.getStatus()), e.getReason());
    }

    // Last resort
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Failed to process request", e);
        return errorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "server_error", "Failed to process request. See server logs for details.");
    }

    protected String errorMessageForHttpStatus(HttpStatus httpStatus) {
        if (httpStatus.is4xxClientError()) {
            return "invalid_request";
        }
        return "server_error";
    }

    private ResponseEntity<ErrorResponse> errorResponseEntity(HttpStatus httpStatus, String error, String errorDescription) {
        return errorResponseEntity(httpStatus, ErrorResponse.builder().error(error).errorDescription(errorDescription).build());
    }

    private ResponseEntity<ErrorResponse> errorResponseEntity(HttpStatus httpStatus, ErrorResponse errorResponse) {
        return ResponseEntity
                .status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }

}
