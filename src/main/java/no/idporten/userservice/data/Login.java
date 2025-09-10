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
public class Login {

    private String eidName;

    private Instant lastLogin;

    private Instant firstLogin;

    public Login(LoginEntity loginEntity) {
        this.eidName = loginEntity.getEidName();
        this.firstLogin = Instant.ofEpochMilli(loginEntity.getFirstLoginAtEpochMs());
        this.lastLogin = Instant.ofEpochMilli(loginEntity.getLastLoginAtEpochMs());
    }
}
