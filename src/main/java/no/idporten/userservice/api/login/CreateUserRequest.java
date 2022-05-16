package no.idporten.userservice.api.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.idporten.validators.identifier.PersonIdentifier;

import javax.validation.constraints.NotEmpty;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserRequest {

    @PersonIdentifier(message = "Invalid person identifier.")
    @NotEmpty(message = "Must hava a value.")
    @JsonProperty("person_identifier")
    private String personIdentifier;


}
