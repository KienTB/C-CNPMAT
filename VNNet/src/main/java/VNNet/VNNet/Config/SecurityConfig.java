    package VNNet.VNNet.Config;

    import VNNet.VNNet.Service.CustomUserDetailsService;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
    import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.CorsConfigurationSource;
    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
    import org.springframework.web.servlet.config.annotation.CorsRegistry;
    import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

    import java.util.Arrays;

    @Configuration
    @EnableWebSecurity
    public class SecurityConfig {
        private final JwtAuthenticationFilter jwtAuthFilter;
        private final CustomUserDetailsService customUserDetailsService;

        public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtAuthenticationFilter jwtAuthFilter) {
            this.customUserDetailsService = customUserDetailsService;
            this.jwtAuthFilter = jwtAuthFilter;
        }
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .formLogin(formLogin -> formLogin.disable())
                    .csrf(csrf -> csrf
                            .ignoringRequestMatchers("/api/user/login", "/api/get/notifications", "/api/user/profile")
                            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    )
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/user/login", "api/get/notifications", "api/user/profile").permitAll()

                            .requestMatchers("/api/admin/**").hasAuthority("admin")
                            .requestMatchers("/api/teacher/**").hasAuthority("teacher")
                            .requestMatchers("/api/parent/**").hasAuthority("parent")

                            .anyRequest().authenticated()
                    )
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    )
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);;
            return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedOrigins(Arrays.asList(
                    "http://10.0.108.250:8080",
                    "http://localhost:8080"
            ));
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
            configuration.setAllowCredentials(true);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", configuration);
            return source;
        }

        @Bean
        public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
            AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
            auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
            return auth.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
