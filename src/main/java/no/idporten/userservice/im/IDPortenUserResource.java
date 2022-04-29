package no.idporten.userservice.im;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.idporten.im.api.UserLogin;
import no.idporten.im.api.UserResource;
import no.idporten.im.api.UserStatus;

import java.time.ZonedDateTime;
import java.util.List;

@NoArgsConstructor
@Data
public class IDPortenUserResource extends UserResource {

    public IDPortenUserResource(boolean active, String id, String personIdentifier, UserStatus userStatus, List<UserLogin> userLogins, ZonedDateTime created, ZonedDateTime lastModified, List<String> helpDeskReferences) {
        super(active, id, personIdentifier, userStatus, userLogins, created, lastModified);
        this.helpDeskReferences = helpDeskReferences;
    }

    @JsonProperty("help_desk_references")
    private List<String> helpDeskReferences;

}
