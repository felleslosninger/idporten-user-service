package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.idporten.userservice.data.EID;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserResponse {

    private String id;

    private String pid;

    @JsonProperty("last_updated")
    private Instant lastUpdated;

    @JsonProperty("closed_code")
    private String closedCode;

    @JsonProperty("closed_code_last_updated")
    private Instant closedCodeLastUpdated;

    private List<EIDResponse> eids;
}
