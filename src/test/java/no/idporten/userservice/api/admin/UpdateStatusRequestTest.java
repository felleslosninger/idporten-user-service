package no.idporten.userservice.api.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When validation a request for status update")
public class UpdateStatusRequestTest {

    Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("then an empty request is accepted")
    void testEmptyRequest() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        Set<ConstraintViolation<UpdateStatusRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

    @Test
    @DisplayName("then closed codes can contains characters, -_ and numbers")
    void testValidClosedCodes() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setClosedCode("AClosed-_code1");
        Set<ConstraintViolation<UpdateStatusRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

    @DisplayName("then invalid closed codes are rejected")
    @ValueSource(strings = {" foo", "bar ", "æøyh", "SPERRA!", "%¤&/¤/%/(%/", ";drop"})
    @ParameterizedTest
    void testInvalidClosedCodes(String closedCode) {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setClosedCode(closedCode);
        Set<ConstraintViolation<UpdateStatusRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertEquals(1, errors.size())
        );
    }

    @DisplayName("then too long closed codes are rejected")
    @Test
    void testTooLongClosedCode() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setClosedCode("c".repeat(51));
        Set<ConstraintViolation<UpdateStatusRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertEquals(1, errors.size())
        );
    }

    @DisplayName("then known closed codes from LDAP import are valid")
    @ValueSource(strings = {
            "CHOSEN",
            "CLOSED_DATAWASH",
            "DEAD",
            "DISAPPEARED",
            "EXPIRED",
            "EXPIRED_TEST",
            "NEW_IDENTITY",
            "Selvvalgt",
            "Sperret",
            "UNINFORMED"})
    @ParameterizedTest
    void testValidLdapClosdedCodes(String closedCode) {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setClosedCode(closedCode);
        Set<ConstraintViolation<UpdateStatusRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

}
