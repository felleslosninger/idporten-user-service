package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatus {

    @JsonProperty("closed_code")
    private String closedCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = JsonDateFormat.ISO_INSTANT_PATTERN, timezone = JsonDateFormat.TIME_ZONE_UTC)
    @JsonProperty("closed_date")
    private Instant closedDate;

}
