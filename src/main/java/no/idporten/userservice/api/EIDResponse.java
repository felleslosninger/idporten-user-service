package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EIDResponse {

    private String name;

    @JsonProperty("last_login")
    private Instant lastLogin;

    @JsonProperty("first_login")
    private Instant firstLogin;
}
