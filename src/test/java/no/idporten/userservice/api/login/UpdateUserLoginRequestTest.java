package no.idporten.userservice.api.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("When validating a request for update login")
public class UpdateUserLoginRequestTest {

    Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("then an empty eID name is rejected")
    void testEmptyEidNameRequest() {
        UpdateUserLoginRequest request = new UpdateUserLoginRequest();
        Set<ConstraintViolation<UpdateUserLoginRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertEquals(1, errors.size()),
                () -> assertEquals("eID name must have a value", errors.iterator().next().getMessage())
        );
    }

    @Test
    @DisplayName("then a too long eID name is rejected")
    void testTooLongEidNameRequest() {
        UpdateUserLoginRequest request = new UpdateUserLoginRequest();
        request.setEidName("x".repeat(256));
        Set<ConstraintViolation<UpdateUserLoginRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertEquals(1, errors.size()),
                () -> assertEquals("eID name too long", errors.iterator().next().getMessage())
        );
    }



    @Test
    @DisplayName("then an eID name cannot start with a whitespace")
    void testCannotStartWithWhitespace() {
        UpdateUserLoginRequest request = new UpdateUserLoginRequest();
        request.setEidName(" FooD");
        Set<ConstraintViolation<UpdateUserLoginRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertEquals(1, errors.size()),
                () -> assertEquals("Invalid format for eID name", errors.iterator().next().getMessage())
        );
    }

    @Test
    @DisplayName("then an eID name cannot end with a whitespace")
    void testCannotEndWithWhitespace() {
        UpdateUserLoginRequest request = new UpdateUserLoginRequest();
        request.setEidName("FooID ");
        Set<ConstraintViolation<UpdateUserLoginRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertEquals(1, errors.size()),
                () -> assertEquals("Invalid format for eID name", errors.iterator().next().getMessage())
        );
    }

    @Test
    @DisplayName("then an eID name can start with a character and end with a character or number and can contain whitespace in the middle")
    void testCanContainWhitespace() {
        UpdateUserLoginRequest request = new UpdateUserLoginRequest();
        request.setEidName("FooID Nettbrett9");
        Set<ConstraintViolation<UpdateUserLoginRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }

    @Test
    @DisplayName("then an eID name can contain æøå")
    void testCanContainNorwegianCharacters() {
        UpdateUserLoginRequest request = new UpdateUserLoginRequest();
        request.setEidName("Åååpenbart En_Kul-Eid Øy Æææææ");
        Set<ConstraintViolation<UpdateUserLoginRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertTrue(errors.isEmpty())
        );
    }


    @Test
    @DisplayName("then an eID name cannot start with a number")
    void testCannotStartWithNumber() {
        UpdateUserLoginRequest request = new UpdateUserLoginRequest();
        request.setEidName("1Number-_ one1eid1");
        Set<ConstraintViolation<UpdateUserLoginRequest>> errors = validator.validate(request);
        assertAll(
                () -> assertNotNull(errors),
                () -> assertEquals(1, errors.size()),
                () -> assertEquals("Invalid format for eID name", errors.iterator().next().getMessage())
        );
    }









}
