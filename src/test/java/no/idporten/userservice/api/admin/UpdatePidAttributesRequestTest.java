package no.idporten.userservice.api.admin;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When validating a request for attribute updates")
public class UpdatePidAttributesRequestTest {

    Validator validator;

    @BeforeEach
    void setup() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("then an empty request is accepted")
    void testEmptyRequest() {
        UpdatePidAttributesRequest request = new UpdatePidAttributesRequest();
        Set<ConstraintViolation<UpdatePidAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

    @Test
    @DisplayName("then a valid pid is accepted")
    void testValidPid() {
        UpdatePidAttributesRequest request = new UpdatePidAttributesRequest();
        request.setPersonIdentifier("29916199102");
        Set<ConstraintViolation<UpdateAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

    @Test
    @DisplayName("then a valid closed code is accepted")
    void testValidClosedCode() {
        UpdatePidAttributesRequest request = new UpdatePidAttributesRequest();
        request.setClosedCode("dead");
        Set<ConstraintViolation<UpdateAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

}
