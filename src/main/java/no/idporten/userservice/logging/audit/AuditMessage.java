package no.idporten.userservice.logging.audit;

import java.lang.annotation.*;

import static no.idporten.userservice.logging.audit.AuditID.UNKNOWN;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditMessage {
    AuditID value() default UNKNOWN;
}
