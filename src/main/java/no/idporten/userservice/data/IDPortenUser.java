package no.idporten.userservice.data;

import lombok.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IDPortenUser {

    private UUID id;

    private String pid;

    private Instant created;

    private Instant lastUpdated;

    private boolean active;

    private String closedCode;

    private Instant closedCodeLastUpdated;

    @Singular
    private List<String> helpDeskCaseReferences = Collections.EMPTY_LIST;

    @Singular
    private List<EID> eids;

    public EID getEIDLastLogin() {
        long latest = 0L;
        EID latestEid = null;
        for (EID e : eids) {
            if (e.getLastLogin().toEpochMilli() > latest) {
                latestEid = e;
                latest = latestEid.getLastLogin().toEpochMilli();
            }
        }
        // Can be null
        return latestEid;
    }

    public IDPortenUser(UserEntity u) {
        this.id = u.getUuid();
        this.pid = u.getPersonIdentifier();
        this.created = Instant.ofEpochMilli(u.getUserCreatedAtEpochMs());
        this.lastUpdated = Instant.ofEpochMilli(u.getUserLastUpdatedAtEpochMs());
        this.active = u.isActive();
        if (u.getClosedCode() != null) {
            this.closedCode = u.getClosedCode();
            this.closedCodeLastUpdated = Instant.ofEpochMilli(u.getClosedCodeUpdatedAtEpochMs());
        }
        if (u.getHelpDeskCaseReferences() != null && u.getHelpDeskCaseReferences().length() > 0 && u.getHelpDeskCaseReferences().contains(",")) {
            this.helpDeskCaseReferences = Arrays.asList(u.getHelpDeskCaseReferences().split(","));
        }

        if (u.getEIDs() != null && !u.getEIDs().isEmpty()) {
            this.eids = u.getEIDs().stream().map(EID::new).toList();
        }
    }


}
