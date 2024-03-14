package no.idporten.userservice;

import io.micrometer.core.instrument.Counter;
import jakarta.servlet.http.HttpServletResponse;
import no.idporten.userservice.api.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.transaction.*;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.security.Principal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;


/**
 * Top level exception handler for application.  Handles generic error situations.
 */
@ControllerAdvice
@Order(100)
public class ApplicationExceptionHandler {
    protected final static String AUTHENTICATION_HEADER = "WWW-Authenticate";

    private final Logger log = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

    @Qualifier("databaseExceptionCounter")
    private final Counter databaseExceptionCounter;

    public ApplicationExceptionHandler(Counter databaseExceptionCounter) {
        this.databaseExceptionCounter = databaseExceptionCounter;
    }

    public static String computeWWWAuthenticateHeaderValue(Map<String, String> parameters) {
        StringJoiner wwwAuthenticate = new StringJoiner(", ", "Bearer ", "");
        if (!parameters.isEmpty()) {
            parameters.forEach((k, v) -> wwwAuthenticate.add(k + "=\"" + v + "\""));
        }
        return wwwAuthenticate.toString();
    }

    // Never triggers this one
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleError(WebRequest request, BadCredentialsException e) {

        String wwwAuthenticate = computeWWWAuthenticateHeaderValue(Collections.emptyMap());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .header(AUTHENTICATION_HEADER, wwwAuthenticate)
                .body(ErrorResponse.builder()
                        .error("access_denied")
                        .errorDescription(e.getMessage())
                        .build());
    }

    //spring security 403
    @ExceptionHandler({ AccessDeniedException.class })
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e, WebRequest request, HttpServletResponse response) {
        Principal userPrincipal = request.getUserPrincipal();
        String error = "access_denied";
        if (userPrincipal instanceof AbstractOAuth2TokenAuthenticationToken) {
            Map<String, String> parameters = new LinkedHashMap<>();
            String errorMessage = "The request requires higher privileges than provided by the access token.";
            error = "insufficient_scope";
            parameters.put("error", error);
            parameters.put("error_description", errorMessage);
            parameters.put("error_uri", "https://tools.ietf.org/html/rfc6750#section-3.1");
            String wwwAuthenticate = computeWWWAuthenticateHeaderValue(parameters);


            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .header(AUTHENTICATION_HEADER, wwwAuthenticate)
                    .body(ErrorResponse.builder()
                            .error(error)
                            .errorDescription(errorMessage)
                            .build());

        }
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                        .error(error)
                        .errorDescription(e.getMessage())
                        .build());
    }

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
        return errorResponseEntity(e.getStatusCode(), errorMessageForHttpStatus(e.getStatusCode()), e.getReason());
    }

    // Database-exception
    // Subclasses of TransactionException: CannotCreateTransactionException (if db is down), HeuristicCompletionException, TransactionSystemException, TransactionTimedOutException, TransactionUsageException, UnexpectedRollbackException
    @ExceptionHandler({CannotCreateTransactionException.class, TransactionSystemException.class, TransactionTimedOutException.class, UnexpectedRollbackException.class})
    public ResponseEntity<ErrorResponse> handleTransactionException(TransactionException e) {
        log.error("TransactionException from database", e);
        databaseExceptionCounter.increment();
        return errorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, errorMessageForHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR), "Failed to process request. See server logs for details.");
    }



    // Last resort
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Failed to process request", e);
        return errorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "server_error", "Failed to process request. See server logs for details.");
    }

    protected String errorMessageForHttpStatus(HttpStatusCode httpStatus) {
        if (httpStatus.is4xxClientError()) {
            return "invalid_request";
        }
        return "server_error";
    }

    private ResponseEntity<ErrorResponse> errorResponseEntity(HttpStatusCode httpStatus, String error, String errorDescription) {
        return errorResponseEntity(httpStatus, ErrorResponse.builder().error(error).errorDescription(errorDescription).build());
    }

    private ResponseEntity<ErrorResponse> errorResponseEntity(HttpStatusCode httpStatus, ErrorResponse errorResponse) {
        return ResponseEntity
                .status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }

}
