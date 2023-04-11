package no.idporten.userservice.api.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When validation a request for attribute updates")
public class UpdateAttributesRequestTest {

    Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("then an empty request is accepted")
    void testEmptyRequest() {
        UpdateAttributesRequest request = new UpdateAttributesRequest();
        Set<ConstraintViolation<UpdateAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

    @Test
    @DisplayName("then an empty list of help desk references is accepted")
    void testEmptyReferences() {
        UpdateAttributesRequest request = new UpdateAttributesRequest();
        request.setHelpDeskReferences(Collections.emptyList());
        Set<ConstraintViolation<UpdateAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

    @Test
    @DisplayName("then a list containing valid help desk reference (format e-journal 7 digits and new format TopDesk 1-10 digits) are accepted")
    void testValidReferenceInList() {
        UpdateAttributesRequest request = new UpdateAttributesRequest();
        request.setHelpDeskReferences(List.of("9876543", "1234567", "1234567890", "1"));
        Set<ConstraintViolation<UpdateAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

    @Test
    @DisplayName("then a list containing an invalid help desk reference is rejected")
    void testInvalidReferenceInList() {
        UpdateAttributesRequest request = new UpdateAttributesRequest();
        request.setHelpDeskReferences(List.of("foo", "1234567"));
        Set<ConstraintViolation<UpdateAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertEquals(1, errors.size()),
                () -> assertEquals("Invalid help desk reference", errors.iterator().next().getMessage())
        );
    }

    @Test
    @DisplayName("then a list containing an empty help desk reference is rejected")
    void testEmptyReferenceInList() {
        UpdateAttributesRequest request = new UpdateAttributesRequest();
        request.setHelpDeskReferences(List.of("", "1234567"));
        Set<ConstraintViolation<UpdateAttributesRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertEquals(1, errors.size()),
                () -> assertEquals("Invalid help desk reference", errors.iterator().next().getMessage())
        );
    }


}
