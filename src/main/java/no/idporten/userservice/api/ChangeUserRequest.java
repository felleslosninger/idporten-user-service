package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.idporten.validators.identifier.PersonIdentifier;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeUserRequest {

    @PersonIdentifier(message = "Invalid person identifier for current pid")
    @NotEmpty(message = "current pid cannot be empty")
    @JsonProperty("current_pid")
    private String currentPid;

    @PersonIdentifier(message = "Invalid person identifier for new pid")
    @NotEmpty(message = "New pid cannot be empty")
    @JsonProperty("new_pid")
    private String newPid;

}
