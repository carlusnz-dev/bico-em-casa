package br.com.bicoemcasa.api.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsProperties corsProperties;
    private final AuthenticationEntryPoint entryPoint;
    public SecurityConfig(CorsProperties corsProperties, AuthenticationEntryPoint entryPoint) {
        this.corsProperties = corsProperties;
        this.entryPoint = entryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/autenticacao/**",
                                "/api/usuario",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/health"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servico/meus").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/servico", "/api/servico/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tag").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/perfil/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/perfil/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/portfolio/meu").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/portfolio", "/api/portfolio/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(entryPoint))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults())
                )
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(RsaKeyProperties rsaKeyProperties) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of(rsaKeyProperties.getPublicKeyPath()));
        RSAPublicKey publicKey = RsaKeyConverters.x509()
                .convert(new ByteArrayInputStream(bytes));

        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder(RsaKeyProperties rsaKeyProperties) throws Exception {
        byte[] publicBytes = Files.readAllBytes(Path.of(rsaKeyProperties.getPublicKeyPath()));
        byte[] privateBytes = Files.readAllBytes(Path.of(rsaKeyProperties.getPrivateKeyPath()));

        RSAPublicKey publicKey = RsaKeyConverters.x509().convert(new ByteArrayInputStream(publicBytes));
        RSAPrivateKey privateKey = RsaKeyConverters.pkcs8().convert(new ByteArrayInputStream(privateBytes));

        JWK jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        corsConfiguration.setAllowedMethods(corsProperties.getAllowedMethods());
        corsConfiguration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        corsConfiguration.setAllowCredentials(corsProperties.isAllowCredentials());
        corsConfiguration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
