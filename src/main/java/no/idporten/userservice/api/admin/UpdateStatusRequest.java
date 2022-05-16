package no.idporten.userservice.api.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateStatusRequest {

    @JsonProperty("closed_code")
    private String closedCode;

}
