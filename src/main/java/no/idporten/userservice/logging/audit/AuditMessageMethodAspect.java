package no.idporten.userservice.logging.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.logging.audit.AuditEntry;
import no.idporten.logging.audit.AuditLogger;
import no.idporten.userservice.logging.TokenMasker;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditMessageMethodAspect {

    private final AuditLogger auditLogger;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<HttpServletRequest> requestObjectProvider;

    @Around("@annotation(AuditMessage)")
    public Object auditLog(ProceedingJoinPoint pjp) throws Throwable {
        var method = ((MethodSignature) pjp.getSignature()).getMethod();
        Object body = pjp.proceed();

        var auditMessage = method.getAnnotation(AuditMessage.class);
        HttpServletRequest request = requestObjectProvider.getObject();
        String authorization = request.getHeader("Authorization");
        String accessToken = null;
        String username = null;
        if (authorization != null && authorization.startsWith("Bearer")) {
            accessToken = TokenMasker.maskToken(authorization.replaceFirst("Bearer", "").trim());
        } else if (authorization != null && authorization.startsWith("Basic")) {
            String userAndPasswordEncoded = authorization.replaceFirst("Basic", "").trim();
            String usernameAndPassword = new String(Base64.getDecoder().decode(userAndPasswordEncoded));
            if (usernameAndPassword.contains(":")) {
                username = usernameAndPassword.substring(0, usernameAndPassword.indexOf(":"));
            }
        }

        Object responseBody = body;
        //set to response body if succcess.
        if (responseBody instanceof ResponseEntity) {
            responseBody = ((ResponseEntity<?>) responseBody).getBody();
            if (!((ResponseEntity<?>) body).getStatusCode().is2xxSuccessful()) {
                return body;
            }
        }

        //add path variables and request body if present
        List<String> resourceId = new ArrayList<>();
        List<Object> requestBody = new ArrayList<>();
        for (var i = 0; i < method.getParameters().length; i++) {
            if (method.getParameters()[i].isAnnotationPresent(PathVariable.class)) {
                Object arg = pjp.getArgs()[i];
                if (arg instanceof String) {
                    resourceId.add((String) arg);
                } else {
                    resourceId.add(arg.toString());
                }
            }
            if (method.getParameters()[i].isAnnotationPresent(RequestBody.class)) {
                Object arg = pjp.getArgs()[i];
                requestBody.add(arg);
            }
        }
        auditLogger.log(AuditEntry.builder()
                .auditId(auditMessage.value().auditIdentifier())
                .logNullAttributes(false)
                .attribute("access_token", accessToken)
                .attribute("username", username)
                .attribute("resource", resourceId.isEmpty() ? null : StringUtils.collectionToDelimitedString(resourceId, "::"))
                .attribute("request_body", requestBody.isEmpty() ? null : StringUtils.collectionToDelimitedString(requestBody, ","))
                .attribute("response_body", Optional.ofNullable(responseBody).map(value -> {
                    try {
                        return objectMapper.writeValueAsString(value);
                    } catch (JsonProcessingException e) {
                        log.warn("Error parsing value for auditlogging. Please look into it.", e);
                        return value;
                    }
                }).orElse(null))
                .build());

        return body;
    }
}
