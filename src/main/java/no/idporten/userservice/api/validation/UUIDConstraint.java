package no.idporten.userservice.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UUIDConstraint implements ConstraintValidator<UUID, String> {

    public void initialize(UUID constraintAnnotation) {
    }

    public boolean isValid(String name, ConstraintValidatorContext constraintContext) {
        try {
            java.util.UUID.fromString(name);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}