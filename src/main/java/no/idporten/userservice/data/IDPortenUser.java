package no.idporten.userservice.data;

import lombok.*;
import org.springframework.util.StringUtils;

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
    private List<String> helpDeskCaseReferences = Collections.EMPTY_LIST;

    @Singular
    private List<Login> logins;

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
            this.helpDeskCaseReferences = Arrays.asList(u.getHelpDeskCaseReferences().split(",")).stream().map(s -> s.trim()).toList();
        }

        if (u.getEIDs() != null && !u.getEIDs().isEmpty()) {
            this.logins = u.getEIDs().stream().map(Login::new).toList();
        }
        if(u.getPreviousUser() != null){
            this.previousUser = new IDPortenUser(u.getPreviousUser());
        }
    }


}
