package no.idporten.userservice.api.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Pattern;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateAttributesRequest {

    @JsonProperty("help_desk_references")
    private List<@Pattern(regexp = "(\\d{1,10})", message = "Invalid help desk reference") String> helpDeskReferences;

}
