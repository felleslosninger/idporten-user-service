package no.idporten.userservice.config;

import lombok.RequiredArgsConstructor;
import no.idporten.userservice.CustomOAuth2AuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final WebSecurityProperties webSecurityProperties;

    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    @Value("${spring.security.oauth2.resource.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        List<String> openEndpoints = webSecurityProperties.getGetAllowed();

        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .addFilterBefore(tokenAuthenticationFilter, BearerTokenAuthenticationFilter.class)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeRequests((authorize) -> authorize
                        .requestMatchers("/login/**").hasRole("USER")
                        .requestMatchers("/admin/**").hasAnyAuthority("SCOPE_idporteninternal:user.read", "SCOPE_idporteninternal:user.write")
                        .requestMatchers(openEndpoints.stream().toArray(String[]::new)).permitAll()
                        .anyRequest().authenticated()
                )
                // will trigger if Bearer token is supplied in Authentication header regardless of what is path is specified in requestMatchers above
                .oauth2ResourceServer((oauth2RS) -> oauth2RS
                        .authenticationEntryPoint(new CustomOAuth2AuthenticationEntryPoint())
                        .jwt((jwt) -> jwt.decoder(jwtDecoder()))
                );

        return http.build();
    }


    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }
}
