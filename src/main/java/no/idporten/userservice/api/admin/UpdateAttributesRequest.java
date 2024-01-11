package no.idporten.userservice.api.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Pattern;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateAttributesRequest {

    @JsonProperty("help_desk_references")
    private List<@Pattern(regexp = "([\\d\\s]{1,15})", message = "Invalid help desk reference, only digits and space allowed") String> helpDeskReferences;

}
