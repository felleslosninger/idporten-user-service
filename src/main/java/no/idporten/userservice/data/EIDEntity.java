package no.idporten.userservice.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "eid")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EIDEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "last_login_ms")
    private long lastLoginAtEpochMs;

    @ManyToOne
    @JoinColumn(name = "user_uuid")
    private UserEntity user;

    @PrePersist
    public void onPrePersist() {
        lastLoginAtEpochMs = Instant.now().toEpochMilli();
    }

    @PreUpdate
    public void onPreUpdate() {
        lastLoginAtEpochMs = Instant.now().toEpochMilli();
    }

}