package no.idporten.userservice.api.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.idporten.validators.identifier.PersonIdentifier;

import javax.validation.constraints.NotEmpty;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeIdentifierRequest {

    @NotEmpty(message = "Must hava a value.")
    @PersonIdentifier(message = "Invalid person identifier.")
    @JsonProperty("old_person_identifier")
    private String oldPersonIdentifier;

    @NotEmpty(message = "Must hava a value.")
    @PersonIdentifier(message = "Invalid person identifier.")
    @JsonProperty("new_person_identifier")
    private String newPersonIdentifier;

}
