package no.idporten.userservice.api.admin;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import no.idporten.validators.identifier.PersonIdentifierValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

        PersonIdentifierValidator.setSyntheticPersonIdentifiersAllowed(true);
        PersonIdentifierValidator.setRealPersonIdentifiersAllowed(false);
    }

    @Test
    @DisplayName("then an empty request is not accepted")
    void testEmptyRequest() {
        UpdatePidAttributesRequest request = new UpdatePidAttributesRequest();
        Set<ConstraintViolation<UpdatePidAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertFalse(errors.isEmpty())
        );
    }

    @Test
    @DisplayName("then a valid synthetic pid is accepted")
    void testValidPid() {
        UpdatePidAttributesRequest request = new UpdatePidAttributesRequest();
        request.setPersonIdentifier("04929074640");
        Set<ConstraintViolation<UpdatePidAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

    @Test
    @DisplayName("then an invalid pid is not accepted")
    void testInvalidPid() {
        UpdatePidAttributesRequest request = new UpdatePidAttributesRequest();
        request.setPersonIdentifier("22222255555");
        Set<ConstraintViolation<UpdatePidAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertFalse(errors.isEmpty())
        );
    }

    @Test
    @DisplayName("then an empty closed code is accepted")
    void testEmptyClosedCode() {
        UpdatePidAttributesRequest request = new UpdatePidAttributesRequest();
        request.setPersonIdentifier("14914598118");
        request.setClosedCode("");
        Set<ConstraintViolation<UpdatePidAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

    @Test
    @DisplayName("then closed codes can contains characters, -_ and numbers")
    void testValidClosedCodes() {
        UpdatePidAttributesRequest request = new UpdatePidAttributesRequest();
        request.setPersonIdentifier("14914598118");
        request.setClosedCode("AClosed-_code1");
        Set<ConstraintViolation<UpdatePidAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

    @DisplayName("then invalid closed codes are rejected")
    @ValueSource(strings = {" foo", "bar ", "æøyh", "SPERRA!", "%¤&/¤/%/(%/", ";drop"})
    @ParameterizedTest
    void testInvalidClosedCodes(String closedCode) {
        UpdatePidAttributesRequest request = new UpdatePidAttributesRequest();
        request.setPersonIdentifier("14914598118");
        request.setClosedCode(closedCode);
        Set<ConstraintViolation<UpdatePidAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertEquals(1, errors.size())
        );
    }

    @DisplayName("then too long closed codes are rejected")
    @Test
    void testTooLongClosedCode() {
        UpdatePidAttributesRequest request = new UpdatePidAttributesRequest();
        request.setPersonIdentifier("14914598118");
        request.setClosedCode("c".repeat(51));
        Set<ConstraintViolation<UpdatePidAttributesRequest>> errors = validator.validate(request);
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
        UpdatePidAttributesRequest request = new UpdatePidAttributesRequest();
        request.setPersonIdentifier("14914598118");
        request.setClosedCode(closedCode);
        Set<ConstraintViolation<UpdatePidAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

}
