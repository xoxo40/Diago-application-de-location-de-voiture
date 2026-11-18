package com.diago.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    /** Chaîne 1 : API REST (/api/**) */
    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/cars", "/api/cars/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cars").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/cars/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/cars/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /** Chaîne 2 : ESPACE ADMIN (/admin/**) — sa propre connexion, réservé au rôle ADMIN */
    @Bean
    @Order(2)
    public SecurityFilterChain adminChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login").permitAll()
                        .anyRequest().hasRole("ADMIN"))
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .usernameParameter("identifier")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/admin", true)
                        .failureUrl("/admin/login?error")
                        .permitAll())
                .logout(l -> l.logoutUrl("/admin/logout").logoutSuccessUrl("/admin/login").permitAll());
        return http.build();
    }

    /** Chaîne 3 : SITE CLIENT (tout le reste) */
    /** Chaîne 3 : ESPACE CHAUFFEUR (/driver/**) */
    @Bean
    @Order(3)
    public SecurityFilterChain driverChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/driver/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/driver/login").permitAll()
                        .anyRequest().hasRole("DRIVER"))
                .formLogin(form -> form
                        .loginPage("/driver/login")
                        .loginProcessingUrl("/driver/login")
                        .usernameParameter("identifier")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/driver", true)
                        .failureUrl("/driver/login?error")
                        .permitAll())
                .logout(l -> l.logoutUrl("/driver/logout").logoutSuccessUrl("/driver/login").permitAll());
        return http.build();
    }
    /** Chaîne 4 : SITE CLIENT (tout le reste) */
    @Bean
    @Order(4)
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/forgot-password",
                                "/css/**", "/js/**", "/img/**","/uploads/**", "/error",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("identifier")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/").permitAll());
        return http.build();
    }
}