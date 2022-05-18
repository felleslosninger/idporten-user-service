package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatus {

    @JsonProperty("closed_code")
    private String closedCode;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonProperty("closed_date")
    private ZonedDateTime closedDate;

}
