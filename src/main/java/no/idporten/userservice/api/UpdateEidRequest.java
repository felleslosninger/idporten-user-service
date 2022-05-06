package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateEidRequest {

    @JsonProperty("eid_name")
    @NotBlank(message = "eid_name cannot be null")
    private String eIdName;
}
