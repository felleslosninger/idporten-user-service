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
public class CreateUserRequest {

    @PersonIdentifier(message = "Invalid person identifier")
    @NotEmpty(message = "pid cannot be empty")
    @JsonProperty("pid")
    private String pid;

    @JsonProperty("closed_code")
    private String closedCode;

}
