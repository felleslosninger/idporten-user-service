package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.idporten.validators.identifier.PersonIdentifier;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchRequest {

    @PersonIdentifier(message = "Invalid person identifier")
    @NotBlank(message = "pid cannot be empty")
    @JsonProperty("pid")
    private String pid;


}
