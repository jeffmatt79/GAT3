package br.ufc.huwc.gat3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@Profile("test")  // ativa apenas no profile test
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()      // desativa CSRF (não recomendado em produção)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // libera todos os endpoints
            )
            .formLogin().disable();
        return http.build();
    }
}
