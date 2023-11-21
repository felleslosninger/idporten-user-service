package no.idporten.userservice.logging.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.idporten.logging.audit.AuditEntry;
import no.idporten.logging.audit.AuditLogger;
import no.idporten.logging.audit.masking.JwtMasker;
import no.idporten.userservice.api.UserResource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static no.idporten.userservice.config.TokenAuthenticationFilter.API_KEY_NAME;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditMessageMethodAspect {

    @Qualifier("auditLogger")
    private final AuditLogger auditLogger;
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
            accessToken = new JwtMasker().mask(authorization.replaceFirst("Bearer", "").trim());
        } else if (authorization != null && authorization.startsWith("Basic")) { // TODO: remove this when login is updated with api-key
            String userAndPasswordEncoded = authorization.replaceFirst("Basic", "").trim();
            String usernameAndPassword = new String(Base64.getDecoder().decode(userAndPasswordEncoded));
            if (usernameAndPassword.contains(":")) {
                username = usernameAndPassword.substring(0, usernameAndPassword.indexOf(":"));
            }
        } else if(request.getHeader(API_KEY_NAME) != null){
            username = API_KEY_NAME;
        }


        String responseReturnMessage = null;
        if (body instanceof ResponseEntity responseEntity) {
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                return body;
            }
            Object responseEntityBody = responseEntity.getBody();
            if(responseEntityBody instanceof UserResource userResource){
                responseReturnMessage = String.format("1 user, user-id: %s", userResource.getId());
            }else if(responseEntityBody instanceof List<?>){
                List<UserResource> users = (List<UserResource>) responseEntityBody;
                List<String> ids = users.stream().map(UserResource::getId).toList();
                responseReturnMessage = String.format("%s user, user-id: %s", users.size(),  ids);
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
                .attribute("response_body", responseReturnMessage)
                .build());

        return body;
    }

}
