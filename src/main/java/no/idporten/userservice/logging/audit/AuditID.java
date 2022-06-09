package no.idporten.userservice.logging.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import no.idporten.logging.audit.AuditIdentifier;

@Getter
@AllArgsConstructor
public enum AuditID {
    UNKNOWN("UNKNOWN"),
    LOGIN_USER_CREATED("LOGIN-USER-CREATED"),
    LOGIN_USER_LOGGEDIN("LOGIN-USER-LOGGEDIN"),
    LOGIN_USER_SEARCHED("LOGIN-USER-SEARCHED"),
    ADMIN_USER_SEARCHED("ADMIN-USER-SEARCHED"),
    ADMIN_USER_READ("ADMIN-USER-READ"),
    ADMIN_USER_UPDATE("ADMIN-USER-UPDATED"),
    ADMIN_USER_STATUS_UPDATED("ADMIN-USER-STATUS-UPDATED");

    private static final String AUDIT_ID_FORMAT = "IDPORTEN-USER-SERVICE-%s";

    private final String auditName;

    AuditIdentifier auditIdentifier() {
        return () -> String.format(AUDIT_ID_FORMAT, auditName);
    }

}
