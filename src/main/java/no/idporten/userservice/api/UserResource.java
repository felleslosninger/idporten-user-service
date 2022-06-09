package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResource {

    private boolean active;

    private String id;

    @JsonProperty("person_identifier")
    private String personIdentifier;

    @JsonProperty("help_desk_references")
    private List<String> helpDeskReferences;

    @JsonProperty("status")
    private UserStatus userStatus;

    @JsonProperty("logins")
    private List<UserLogin> userLogins = new ArrayList<>();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = JsonDateFormat.ISO_INSTANT_PATTERN, timezone = JsonDateFormat.TIME_ZONE_UTC)
    @JsonProperty("created")
    private Instant created;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = JsonDateFormat.ISO_INSTANT_PATTERN, timezone = JsonDateFormat.TIME_ZONE_UTC)
    @JsonProperty("last_modified")
    private Instant lastModified;

}
