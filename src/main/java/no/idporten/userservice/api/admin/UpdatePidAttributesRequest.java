package no.idporten.userservice.api.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;
import no.idporten.validators.identifier.PersonIdentifier;
import org.hibernate.validator.constraints.Length;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePidAttributesRequest extends UpdateAttributesRequest {

    @PersonIdentifier(message = "Invalid person identifier")
    @JsonProperty("person_identifier")
    private String personIdentifier;

    @Length(max = 50, message = "closed code too long")
    @Pattern(regexp = "^([a-zA-Z])+([a-zA-Z0-9\\-_])*([a-zA-Z0-9])+|$", message = "Invalid format for closed code")
    @JsonProperty("closed_code")
    private String closedCode;

}
