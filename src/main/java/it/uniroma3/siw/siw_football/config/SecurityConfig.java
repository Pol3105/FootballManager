package it.uniroma3.siw.siw_football.config;

import it.uniroma3.siw.siw_football.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests((requests) -> requests
                // Req 4.2: comentario requiere auth
                .requestMatchers("/match/*/comment").authenticated()
                // Req 4.3: solo ADMIN puede acceder a rutas de gestión
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Req 4.1: funciones públicas de lectura
                .requestMatchers(
                    "/", "/login", "/register",
                    "/css/**", "/images/**",
                    "/tournament/**", "/team/**",
                    "/match/**",
                    "/players", "/referees", "/teams"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}
