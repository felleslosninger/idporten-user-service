package no.idporten.userservice.logging.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import no.idporten.logging.audit.AuditIdentifier;

@Getter
@AllArgsConstructor
public enum AuditID {
    UNKNOWN("UNKNOWN", 0),
    LOGIN_USER_CREATED("LOGIN-USER-CREATED", 1),
    LOGIN_USER_LOGGEDIN("LOGIN-USER-LOGGEDIN", 2),
    LOGIN_USER_SEARCHED("LOGIN-USER-SEARCHED", 3),
    ADMIN_USER_SEARCHED("ADMIN-USER-SEARCHED", 4),
    ADMIN_USER_READ("ADMIN-USER-READ", 5),
    ADMIN_USER_UPDATE("ADMIN-USER-UPDATED", 6),
    ADMIN_USER_STATUS_UPDATED("ADMIN-USER-STATUS-UPDATED", 7);

    private static final String AUDIT_ID_FORMAT = "IDPORTEN-USER-SERVICE-%s";

    private final String auditName;
    private final int id;

    AuditIdentifier auditIdentifier() {
        return () -> String.format(AUDIT_ID_FORMAT, auditName);
    }

}
