package no.idporten.userservice.api.admin;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateAttributesRequest {

    @JsonIgnore
    private Map<String, Object> _attributes = new HashMap<>();

    @JsonAnySetter
    public void setAttribute(String name, Object value) {
        _attributes.put(name, value);
    }

    @JsonIgnore
    public Object getAttribute(String name) {
        Objects.requireNonNull(name);
        return _attributes.get(name);
    }

    @JsonIgnore
    public String getStringAttribute(String name) {
        return (String) getAttribute(name);
    }

}
