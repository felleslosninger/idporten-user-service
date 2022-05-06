package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.lang.reflect.Field;

@Slf4j
@ControllerAdvice
public class ExceptionHelper {

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserExists(UserExistsException e) {
        log.error("UserExistsException: " + e.getMessage(), e);
        ErrorResponse error = ErrorResponse.builder().error("user_exits").errorDescription(e.getMessage()).build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.error("IllegalArgumentException: " + e.getMessage(), e);
        ErrorResponse error = ErrorResponse.builder().error("invalid_input").errorDescription(e.getMessage()).build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(MethodArgumentNotValidException e) {
        String errorDescription = null;
        if (!e.getBindingResult().getAllErrors().isEmpty() && e.getBindingResult().getFieldError() != null && e.getTarget() != null) {
            FieldError fieldError = e.getBindingResult().getFieldError();
            Field field = ReflectionUtils.findField(e.getTarget().getClass(), fieldError.getField());
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

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Exception: ", ex);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.builder()
                        .error("unknown_error")
                        .errorDescription(ex.getMessage())
                        .build());
    }
}
