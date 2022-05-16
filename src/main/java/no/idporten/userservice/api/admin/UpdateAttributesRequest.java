package no.idporten.userservice.api.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Validated
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateAttributesRequest {

    @JsonProperty("help_desk_references")
    private List<@NotEmpty(message = "A help desk reference cannot be empty") String> helpDeskReferences;

}
