package no.idporten.userservice.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity(name = "user")
@Table(name = "user")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue
    private UUID uuid;

    @Column(name = "person_identifier")
    private String personIdentifier;

    @Column(name = "user_created_ms")
    private long userCreatedAtEpochMs;

    @Column(name = "user_last_updated_ms")
    private long userLastUpdatedAtEpochMs;

    @Column(name = "active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "close_code")
    private String closeCode;

    @Column(name = "close_code_updated_ms")
    private long closeCodeUpdatedAtEpochMs;

    @PrePersist
    public void onPrePersist() {
        userCreatedAtEpochMs = Instant.now().toEpochMilli();
        userLastUpdatedAtEpochMs = getUserCreatedAtEpochMs();
    }

    @PreUpdate
    public void onPreUpdate() {
        userLastUpdatedAtEpochMs = Instant.now().toEpochMilli();
    }

}
