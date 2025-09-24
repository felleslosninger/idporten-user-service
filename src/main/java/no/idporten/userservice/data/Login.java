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
    private Long id;

    private String eidName;

    private Instant lastLogin;

    private Instant firstLogin;

    public Login(LoginEntity loginEntity) {
        this.id = loginEntity.getId();
        this.eidName = loginEntity.getEidName();
        this.firstLogin = Instant.ofEpochMilli(loginEntity.getFirstLoginAtEpochMs());
        this.lastLogin = Instant.ofEpochMilli(loginEntity.getLastLoginAtEpochMs());
    }

    public LoginEntity toEntity(UserEntity user) {
        LoginEntity loginEntity = new LoginEntity();
        loginEntity.setId(id);
        loginEntity.setEidName(eidName);
        loginEntity.setFirstLoginAtEpochMs(firstLogin.toEpochMilli());
        loginEntity.setLastLoginAtEpochMs(lastLogin.toEpochMilli());
        loginEntity.setUser(user);
        return loginEntity;
    }
}
