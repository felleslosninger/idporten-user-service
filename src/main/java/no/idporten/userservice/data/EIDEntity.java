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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "last_login_ms")
    private long lastLoginAtEpochMs;

    @Column(name = "first_login_ms")
    private long firstLoginAtEpochMs;

    @ManyToOne
    @JoinColumn(name = "user_uuid")
    private UserEntity user;

    @PrePersist
    public void onPrePersist() {
        firstLoginAtEpochMs = Instant.now().toEpochMilli();
        lastLoginAtEpochMs = getFirstLoginAtEpochMs();
    }

    @PreUpdate
    public void onPreUpdate() {
        lastLoginAtEpochMs = Instant.now().toEpochMilli();
    }

}