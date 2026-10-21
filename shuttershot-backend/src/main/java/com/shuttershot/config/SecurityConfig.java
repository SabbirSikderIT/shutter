package com.shuttershot.config;

import com.shuttershot.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/otp/**", "/uploads/**", "/api/chatbot/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ai-chat/messages", "/api/ai-chat/transcribe").permitAll()
                        .requestMatchers("/api/ai-chat/**").authenticated()
                        .requestMatchers("/api/blue-badge/payment/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/photographers/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/photographers/**", "/api/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/poses", "/api/music", "/api/photoshoot-categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**", "/api/orders/delivery-rules").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                        .requestMatchers("/api/shop/payment/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/bookings", "/api/bookings/*/confirm-otp",
                                "/api/bookings/*/deposit/initiate", "/api/bookings/*/verification-method",
                                "/api/bookings/verify-qr/*", "/api/reviews")
                        .permitAll()
                        .requestMatchers("/api/bookings/payment/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/bookings/mine").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/bookings/*").permitAll()
                        // 🌟 নতুন লাইন: /api/ ছাড়া পাথগুলোর জন্য
                        .requestMatchers(HttpMethod.GET, "/photographers/**", "/orders/delivery-rules").permitAll()
                        .requestMatchers("/api/admin/staff", "/api/admin/staff/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "MODERATOR")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}