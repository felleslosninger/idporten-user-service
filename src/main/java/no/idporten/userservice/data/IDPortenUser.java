package no.idporten.userservice.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
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

    private String closeCode;

    private boolean active;

    private Instant closeCodeLastUpdated;

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
    }

    public UserEntity toEntity() {
        UserEntity.UserEntityBuilder builder = UserEntity.builder();
        builder.personIdentifier(this.getPid()).uuid(this.getId()).active(this.isActive());
        if (getCloseCode() != null) {
            builder.closeCode(this.getCloseCode());
            builder.closeCodeUpdatedAtEpochMs(Instant.now().toEpochMilli());
        }
        return builder.build();
    }
}
