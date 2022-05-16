package no.idporten.userservice.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLogin {

    private String eid;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonProperty("first_login")
    private ZonedDateTime firstLogin;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonProperty("last_login")
    private ZonedDateTime lastLogin;

}


