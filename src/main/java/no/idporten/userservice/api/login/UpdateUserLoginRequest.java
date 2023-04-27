package no.idporten.userservice.api.login;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserLoginRequest {

    @NotEmpty(message = "eID name must have a value")
    @Length(max = 255, message = "eID name too long")
    @Pattern(regexp = "^([a-zA-ZæøåÆØÅ])+([a-zA-ZæøåÆØÅ0-9 \\-_])*([a-zA-ZæøåÆØÅ0-9])+$", message = "Invalid format for eID name")
    @JsonProperty("eid_name")
    private String eidName;

}
