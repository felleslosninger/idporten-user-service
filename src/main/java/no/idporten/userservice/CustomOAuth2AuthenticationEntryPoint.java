package no.idporten.userservice;

import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static no.idporten.userservice.ApplicationExceptionHandler.AUTHENTICATION_HEADER;
import static no.idporten.userservice.ApplicationExceptionHandler.computeWWWAuthenticateHeaderValue;

@Slf4j
public class CustomOAuth2AuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {

        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String error = "access_denied";
        String errorDescription = "Insufficient authentication details";
        String wwwAuthenticate = null;
        Map<String, String> parameters = new LinkedHashMap<>();

        if (e instanceof OAuth2AuthenticationException oAuth2AuthenticationException) {
            OAuth2Error oAuth2Error = oAuth2AuthenticationException.getError();
            error = oAuth2Error.getErrorCode();
            parameters.put("error", error);
            if (StringUtils.hasText(oAuth2Error.getDescription())) {
                errorDescription = oAuth2Error.getDescription();
                parameters.put("error_description", errorDescription);
            }
            if (StringUtils.hasText(oAuth2Error.getUri())) {
                parameters.put("error_uri", oAuth2Error.getUri());
            }
            if (oAuth2Error instanceof BearerTokenError bearerTokenError) {
                if (StringUtils.hasText(bearerTokenError.getScope())) {
                    parameters.put("scope", bearerTokenError.getScope());
                }
                status = ((BearerTokenError) oAuth2Error).getHttpStatus();
            }
        } else if (e instanceof InsufficientAuthenticationException authenticationException) {
            // No authentication is provided for either /login or /admin.
            if (request.getRequestURI().contains("login")) {
                wwwAuthenticate = "Basic realm=\"Realm\"";
            }
        }
        JSONObject message = new JSONObject();
        message.put("error", error);
        message.put("error_description", errorDescription);

        if (null == wwwAuthenticate) wwwAuthenticate = computeWWWAuthenticateHeaderValue(parameters);

        response.addHeader(AUTHENTICATION_HEADER, wwwAuthenticate);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(message.toJSONString());
        log.info(String.format("{%s}, http status: %s, %s header: {%s}. Error message: %s", e.getMessage(), status.value(), AUTHENTICATION_HEADER, wwwAuthenticate, message));
    }

}
