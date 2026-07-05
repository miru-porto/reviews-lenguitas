package com.lenguas.ratemyprof.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Páginas públicas: home, ver materias, ver reviews, buscar.
                // "/error" es el dispatch interno de Spring cuando salta una excepción:
                // si Security lo bloquea, un 404 termina redirigiendo al login.
                .requestMatchers("/", "/materias", "/materias/**", "/catedra/**", "/buscar", "/registro", "/css/**", "/error").permitAll()
                // Crear review requiere estar logueado
                .requestMatchers("/review/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/materias")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}
