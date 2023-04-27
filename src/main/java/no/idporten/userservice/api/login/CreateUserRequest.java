package no.idporten.userservice.api.login;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.idporten.validators.identifier.PersonIdentifier;

import jakarta.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateUserRequest {

    @PersonIdentifier(message = "Invalid person identifier.")
    @NotEmpty(message = "Must hava a value.")
    @JsonProperty("person_identifier")
    private String personIdentifier;


}
