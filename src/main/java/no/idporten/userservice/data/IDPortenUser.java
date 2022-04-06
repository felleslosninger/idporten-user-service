package no.idporten.userservice.data;

import lombok.*;

import java.time.Instant;
import java.util.*;

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
    }

    public UserEntity toEntity() {
        UserEntity.UserEntityBuilder builder = UserEntity.builder();
        builder.personIdentifier(this.getPid()).uuid(this.getId()).active(this.isActive());
        if (getCloseCode() != null) {
            builder.closeCode(this.getCloseCode());
            builder.closeCodeUpdatedAtEpochMs(Instant.now().toEpochMilli());
        }
        if (getHelpDeskCaseReferences()!=null && !getHelpDeskCaseReferences().isEmpty()) {
            builder.helpDeskCaseReferences(String.join(",", getHelpDeskCaseReferences()));
        }
        return builder.build();
    }
}
