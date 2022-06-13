package no.idporten.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final WebSecurityProperties webSecurityProperties;

    @Value("${spring.security.user.name}")
    private String basicUsername;

    @Value("${spring.security.user.password}")
    private String basicPassword;

    @Value("${spring.security.oauth2.resource.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] openEndpoints = webSecurityProperties.getGetAllowed().toArray(String[]::new);


        http
                .csrf().disable()
                .headers().frameOptions().sameOrigin()
                .and()
                .authorizeHttpRequests((authorize) -> authorize
                        .antMatchers("/login/**").hasAnyRole("USER")
                        .antMatchers("/admin/**").hasAnyAuthority("SCOPE_idporteninternal:user.read", "SCOPE_idporteninternal:user.write")
                        .antMatchers(openEndpoints).permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer().jwt()
                .and()
                .authenticationEntryPoint(new CustomOAuth2AuthenticationEntryPoint())
                .and()
                .httpBasic(withDefaults())
                .formLogin(withDefaults());

        return http.build();
    }


    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.withUsername(basicUsername)
                .password(passwordEncoder().encode(basicPassword))
                .authorities("ROLE_USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri).build();
    }
}
