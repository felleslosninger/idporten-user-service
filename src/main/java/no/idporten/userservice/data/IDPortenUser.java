package no.idporten.userservice.data;

import lombok.*;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class IDPortenUser {

    private UUID id;

    @EqualsAndHashCode.Include
    private String pid;

    private Instant created;

    private Instant lastUpdated;

    private boolean active;

    private String closedCode;

    private Instant closedCodeLastUpdated;

    private IDPortenUser previousUser;

    @Singular
    private List<String> helpDeskCaseReferences = Collections.emptyList();

    @Singular
    private List<Login> logins =  Collections.emptyList();

    private Boolean dirty = false;

    public Login getLastLogin() {
        long latest = 0L;
        Login latestLogin = null;
        for (Login e : logins) {
            if (e.getLastLogin().toEpochMilli() > latest) {
                latestLogin = e;
                latest = latestLogin.getLastLogin().toEpochMilli();
            }
        }
        // Can be null
        return latestLogin;
    }

    public void setStatus(String closedCode) {
        if (closedCode == null) {
            this.setActive(true);
            this.setClosedCode(null);
            this.setClosedCodeLastUpdated(null);
        } else {
            this.setActive(false);
            this.setClosedCode(closedCode);
            this.setClosedCodeLastUpdated(Clock.systemUTC().instant());
        }
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
        if (StringUtils.hasText(u.getHelpDeskCaseReferences())) {
            this.helpDeskCaseReferences = Arrays.stream(u.getHelpDeskCaseReferences().split(",")).map(String::trim).toList();
        }

        if (u.getLogins() != null && !u.getLogins().isEmpty()) {
            this.logins = u.getLogins().stream().map(Login::new).toList();
        }
        if(u.getPreviousUser() != null){
            this.previousUser = new IDPortenUser(u.getPreviousUser());
        }
    }


}
