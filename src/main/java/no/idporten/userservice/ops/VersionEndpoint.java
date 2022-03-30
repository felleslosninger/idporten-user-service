package no.idporten.userservice.ops;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "version", enableByDefault = false)
public class VersionEndpoint {

    @Value("${project.version}")
    private String projectVersion;

    @ReadOperation
    public Map<String, Object> version() {
        Map<String, Object> versionMap = new HashMap<>();
        versionMap.put("version", projectVersion);
        return versionMap;
    }
}