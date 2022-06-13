package no.idporten.userservice.api.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Pattern;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateStatusRequest {

    @Length(max = 50, message = "closed code too long")
    @Pattern(regexp = "^([a-zA-Z])+([a-zA-Z0-9\\-_])*([a-zA-Z0-9])+$", message = "Invalid format for closed code")
    @JsonProperty("closed_code")
    private String closedCode;

}
