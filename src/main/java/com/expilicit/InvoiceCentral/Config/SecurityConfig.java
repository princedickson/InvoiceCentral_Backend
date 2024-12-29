package com.expilicit.InvoiceCentral.Config;

import com.expilicit.InvoiceCentral.AuthProvider.JwtAuthenticationFilter;
import com.expilicit.InvoiceCentral.Entity.Permission;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final BCryptPasswordEncoder encoder;
    private final Userservice userService;
    private final JwtAuthenticationFilter authenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.
                csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // public endpoint
                        .requestMatchers(
                                "/api/v1/register/**",
                                "/api/v1/register/confirm").permitAll()
                        .requestMatchers("/api/v1/register/login").permitAll()

                        // user endpoint
                        .requestMatchers("/api/v1/user/**").hasAnyAuthority(Permission.USER_READ.name())

                        // customer endpoint
                        .requestMatchers("/api/v1/customer/read").hasAnyAuthority(Permission.CUSTOMER_READ.name())
                        .requestMatchers("/api/v1/customer/create").hasAnyAuthority(Permission.CUSTOMER_CREATE.name())
                        .requestMatchers("/api/v1/customer/update").hasAnyAuthority(Permission.CUSTOMER_UPDATE.name())
                        .requestMatchers("/api/v1/customer/delete").hasAnyAuthority(Permission.CUSTOMER_DELETE.name())

                        // admin endpoint
                        .requestMatchers("/api/v1/admin/").hasAnyRole("ADMIN", "SYS_ADMIN")

                        // sys_admin endpoint
                        .requestMatchers("/api/v1/sys_admin/").hasRole("SYS_ADMIN")

                        // swagger endpoint
                        .requestMatchers("/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**").permitAll()

                        .anyRequest()
                        .authenticated())
                .authenticationProvider(authprovider()).addFilterBefore(authenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    private DaoAuthenticationProvider authprovider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userService);
        daoAuthenticationProvider.setPasswordEncoder(encoder);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager manager(AuthenticationConfiguration authenticationConfig) throws Exception {

        return authenticationConfig.getAuthenticationManager();
    }
}
