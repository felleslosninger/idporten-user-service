package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest {

    @NotEmpty(message = "pid cannot be empty")
    @JsonProperty("pid")
    private String pid;

    @JsonProperty("closed_code")
    private String closedCode;


}
