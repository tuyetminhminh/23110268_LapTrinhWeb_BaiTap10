package vn.org.com.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;

    // Dùng interface cho thống nhất với nơi tiêm (UserServiceImpl)
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder());
        return p;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public pages
                .requestMatchers("/", "/home", "/login", "/register", "/forgot-password").permitAll()
                // Static assets (tùy dự án bạn đang dùng /image/ hay /images/)
                .requestMatchers("/css/**", "/js/**", "/images/**", "/image/**").permitAll()
                // GraphQL/GraphiQL (mở khi demo; muốn khóa thì chuyển thành hasRole)
                .requestMatchers("/graphql", "/graphiql/**").hasAnyRole("ADMIN")
                // Role-based areas
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                // Everything else must be authenticated
                .anyRequest().authenticated()
            )
            // Nếu bạn gọi POST /graphql bằng fetch, bỏ CSRF cho endpoint này
            .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/graphql")))
            .formLogin(form -> form
                .loginPage("/login")              // GET /login
                .loginProcessingUrl("/login")     // <-- ĐỔI về /login để khớp th:action="@{/login}"
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(loginSuccessHandler)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .httpBasic(Customizer.withDefaults()); // tiện debug API

        http.authenticationProvider(authProvider());
        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
