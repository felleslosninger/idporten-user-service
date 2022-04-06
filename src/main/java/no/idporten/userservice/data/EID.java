package no.idporten.userservice.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EID {

    private String name;

    private Instant lastLogin;

    public EID(EIDEntity eidEntity) {
        this.name = eidEntity.getName();
        this.lastLogin = Instant.ofEpochMilli(eidEntity.getLastLoginAtEpochMs());
    }

    public EIDEntity toEntity() {
        EIDEntity.EIDEntityBuilder builder = EIDEntity.builder();
        builder.name(getName());
        return builder.build();
    }
}
