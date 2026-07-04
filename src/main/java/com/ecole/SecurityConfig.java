package com.ecole;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.ecole.service.AuthSuccessHandler;
import com.ecole.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthSuccessHandler authSuccessHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
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
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index", "/login", "/css/**", "/js/**").permitAll()
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/api/actualites", "/api/actualites/").permitAll()
                .requestMatchers("/api/actualites/*").permitAll()
                .requestMatchers("/api/notifications/**").authenticated()
                .requestMatchers("/directeur/**").hasRole("DIRECTEUR")
                .requestMatchers("/secretariat/**").hasRole("SECRETARIAT")
                .requestMatchers("/professeur/**").hasRole("PROFESSEUR")
                .requestMatchers("/etudiant/**").hasRole("ETUDIANT")
                .requestMatchers("/parent/**").hasRole("PARENT")
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/actualites", "/api/actualites/", "/api/notifications/**")
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(authSuccessHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            );

        return http.build();
    }
}