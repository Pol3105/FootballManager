package it.uniroma3.siw.siw_football.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                // 1. Decimos qué rutas son PÚBLICAS (Cualquiera puede entrar)
                .requestMatchers("/", "/css/**", "/images/**","/tournament/**","/team/**").permitAll() 
                
                // 2. Decimos que CUALQUIER OTRA ruta requerirá estar logueado
                .anyRequest().authenticated() 
            )
            .formLogin((form) -> form
                .permitAll() // Permitimos que todos puedan ver la pantalla de Login
            )
            .logout((logout) -> logout.permitAll());

        return http.build();
    }
}