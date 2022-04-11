package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateEidRequest {

    @JsonProperty("eid_name")
    private String eIdName;
}
