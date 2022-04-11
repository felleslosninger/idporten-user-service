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

    private String closeCode;

    private Instant closeCodeLastUpdated;

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
        if (u.getCloseCode() != null) {
            this.closeCode = u.getCloseCode();
            this.closeCodeLastUpdated = Instant.ofEpochMilli(u.getCloseCodeUpdatedAtEpochMs());
        }
        if (u.getHelpDeskCaseReferences() != null && u.getHelpDeskCaseReferences().length() > 0 && u.getHelpDeskCaseReferences().contains(",")) {
            this.helpDeskCaseReferences = Arrays.asList(u.getHelpDeskCaseReferences().split(","));
        }

        if (u.getEIDs() != null && !u.getEIDs().isEmpty()) {
            this.eids = u.getEIDs().stream().map(EID::new).toList();
        }
    }

    public UserEntity toEntity() {
        UserEntity.UserEntityBuilder builder = UserEntity.builder();
        builder.personIdentifier(this.getPid()).uuid(this.getId()).active(this.isActive());
        if (getCloseCode() != null) {
            builder.closeCode(this.getCloseCode());
            builder.closeCodeUpdatedAtEpochMs(Instant.now().toEpochMilli());
        }
        if (!getHelpDeskCaseReferences().isEmpty()) {
            builder.helpDeskCaseReferences(String.join(",", getHelpDeskCaseReferences()));
        }
        if (getEids() != null && !getEids().isEmpty()) {
            builder.eIDs(this.getEids().stream().map(EID::toEntity).toList());
        }
        return builder.build();
    }
}
