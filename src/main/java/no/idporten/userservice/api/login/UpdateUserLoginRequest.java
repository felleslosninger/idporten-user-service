package no.idporten.userservice.api.login;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserLoginRequest {

    @NotEmpty(message = "Must hava a value.")
    @JsonProperty("eid_name")
    private String eidName;

}
