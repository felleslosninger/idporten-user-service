package no.idporten.userservice.logging.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import no.idporten.logging.audit.AuditIdentifier;

@Getter
@AllArgsConstructor
public enum AuditID {
    UNKNOWN("UNKNOWN", 0),
    LOGIN_CREATE_USER("LOGIN-CREATE-USER", 1),
    LOGIN_ADD_USER_LOGIN("LOGIN-ADD-USER-LOGIN", 2),
    LOGIN_SEARCH_USER("LOGIN-SEARCH-USER", 3),
    ADMIN_SEARCH_USER("ADMIN-SEARCH-USER", 4),
    ADMIN_GET_USER("ADMIN-GET-USER", 5),
    ADMIN_UPDATE_USER("ADMIN-UPDATE-USER", 6),
    ADMIN_UPDATE_USER_STATUS("ADMIN-UPDATE-USER-STATUS", 7);

    private static final String AUDIT_ID_FORMAT = "IDPORTEN-USER-SERVICE-%d-%s";

    private final String auditName;
    private final int id;

    AuditIdentifier auditIdentifier() {
        return () -> String.format(AUDIT_ID_FORMAT, id, auditName);
    }

}
