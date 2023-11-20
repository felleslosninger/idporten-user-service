package no.idporten.userservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;

@WebFilter(urlPatterns = "/login/*")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class TokenAuthenticationFilter extends HttpFilter {

    public static final String API_KEY_NAME = "api-key";

    @Value("${spring.security.api-key}")
    private String apiKey;

    @Value("${spring.security.user.name}") //TODO: remove this when login is updated with api-key
    private String basicUsername;

    @Value("${spring.security.user.password}") //TODO: remove this when login is updated with api-key
    private String basicPassword;

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // get api-key from request header

        if (request.getRequestURI().contains("login")) {

            String apiKeyRecived = request.getHeader(API_KEY_NAME);
            boolean isBasicAuth = isBasicAuth(request);
            if ((apiKey == null || !apiKey.equals(apiKeyRecived)) && !isBasicAuth) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                // create default user and add to context
                Collection<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("idportenUser", "Pwd", authorities);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            }
        }
        chain.doFilter(request, response);
    }

    /**
     * Check if request is using basic auth and verify it
     *
     * TODO: REMOVE when login has changed to api-key
     *
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    private boolean isBasicAuth(HttpServletRequest request) throws UnsupportedEncodingException {
        String basicauth = request.getHeader("Authorization");
        if (basicauth == null || !basicauth.startsWith("Basic ")) {
            return false;
        }
        byte[] decoded = Base64.getDecoder().decode(basicauth.substring("Basic ".length()));
        if (decoded == null) {
            return false;
        }
        String[] credentials = new String(decoded, "UTF-8").split(":");
        if (credentials == null || credentials.length != 2) {
            return false;
        }
        return basicUsername.equals(credentials[0]) && basicPassword.equals(credentials[1]);

    }
}
