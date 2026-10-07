package com.rota.config;

import com.rota.repository.UsuarioRepository;
import com.rota.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UsuarioRepository usuarioRepository;

    public SecurityConfig(@Lazy JwtAuthenticationFilter jwtAuthFilter, UsuarioRepository usuarioRepository) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.usuarioRepository = usuarioRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                // Endpoint público de Login
                .requestMatchers("/api/auth/**").permitAll()
                
                // Endpoints exclusivos del DUEÑO
                .requestMatchers(HttpMethod.POST, "/api/categorias/**", "/api/productos/**").hasRole("DUENO")
                .requestMatchers(HttpMethod.PUT, "/api/categorias/**", "/api/productos/**").hasRole("DUENO")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**", "/api/productos/**").hasRole("DUENO")
                .requestMatchers("/api/comercios/**").hasRole("DUENO")
                
                // Endpoints compartidos con REPOSITOR
                .requestMatchers("/api/lotes/**", "/api/operaciones/**", "/api/riesgo/**").hasAnyRole("DUENO", "REPOSITOR")
                .requestMatchers(HttpMethod.GET, "/api/categorias/**", "/api/productos/**").hasAnyRole("DUENO", "REPOSITOR")

                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // CONFIGURACIÓN PROFESIONAL: Forzar respuesta HTTP 401 para solicitudes no autenticadas
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + username));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}