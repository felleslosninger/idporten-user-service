package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.lang.reflect.Field;

@Slf4j
@ControllerAdvice("no.idporten.userservice.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ErrorResponse.builder()
                        .error(e.getError())
                        .errorDescription(e.getErrorDescription())
                        .build());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(BindException e) {
        String errorDescription = null;
        if (!e.getBindingResult().getAllErrors().isEmpty() && e.getBindingResult().getFieldError() != null && e.getTarget() != null) {
            FieldError fieldError = e.getBindingResult().getFieldError();
            String fieldName = fieldError.getField();
            if (fieldName.indexOf('[') > 0) {
                fieldName = fieldName.substring(0, fieldName.indexOf('['));
            }
            Field field = ReflectionUtils.findField(e.getTarget().getClass(), fieldName);
            if (field != null) {
                JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                if (jsonProperty != null) {
                    errorDescription = "Invalid attribute %s: %s".formatted(jsonProperty.value(), fieldError.getDefaultMessage());
                } else {
                    errorDescription = "Invalid attribute %s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage());
                }
            }
        }
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder()
                        .error("invalid_request")
                        .errorDescription(errorDescription)
                        .build());
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
