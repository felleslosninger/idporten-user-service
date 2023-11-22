package no.idporten.userservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@WebFilter(urlPatterns = "/login/*")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@Validated
public class TokenAuthenticationFilter extends HttpFilter {

    public static final String API_KEY_NAME = "api-key";

    @Value("${spring.security.api-key}")
    @NotBlank(message = "api-key must not be blank")
    private String apiKey;

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // get api-key from request header

        if (request.getRequestURI().contains("login")) {

            String apiKeyRecived = request.getHeader(API_KEY_NAME);
            if (apiKey == null || !apiKey.equals(apiKeyRecived)) {
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

}
