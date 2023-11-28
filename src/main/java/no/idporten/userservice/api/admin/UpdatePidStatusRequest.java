package no.idporten.userservice.api.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.idporten.validators.identifier.PersonIdentifier;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePidStatusRequest extends UpdateStatusRequest {

    @PersonIdentifier(message = "Invalid person identifier")
    @JsonProperty("person_identifier")
    private String personIdentifier;

}
