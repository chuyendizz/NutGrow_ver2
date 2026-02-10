// package com.demo.nutgrow.config;

// import com.demo.nutgrow.service.UserService;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.ApplicationListener;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
// import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
// import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
// import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
// import org.springframework.security.oauth2.core.user.OAuth2User;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
// import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
// import org.thymeleaf.spring6.SpringTemplateEngine;
// import org.thymeleaf.templateresolver.ITemplateResolver;

// import java.io.IOException;
// import java.util.HashSet;
// import java.util.Set;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {
//     @Autowired
//     private UserService userService;
//     @Autowired
//     private CustomOAuth2UserService oauthUserService;

//     @Bean
//     public DaoAuthenticationProvider authenticationProvider() {
//         DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
//         authenticationProvider.setUserDetailsService(userService);
//         authenticationProvider.setPasswordEncoder(passwordEncoder());
//         return authenticationProvider;
//     }

//     @Bean
//     public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//         return authConfig.getAuthenticationManager();
//     }

//     @Bean
//     public BCryptPasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }

//     @Bean
//     ApplicationListener<AuthenticationSuccessEvent> successEvent() {
//         return event -> {
//             System.out.println("Success Login " + event.getAuthentication().getClass().getSimpleName() + " - "
//                     + event.getAuthentication().getName());
//         };
//     }

//     @Bean
//     ApplicationListener<AuthenticationFailureBadCredentialsEvent> failureEvent() {
//         return event -> {
//             System.err.println("Bad Credentials Login " + event.getAuthentication().getClass().getSimpleName() + " - "
//                     + event.getAuthentication().getName());
//         };
//     }

//     @Bean
//     public SpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
//         SpringTemplateEngine engine = new SpringTemplateEngine();
//         engine.setTemplateResolver(templateResolver);
//         engine.addDialect(new SpringSecurityDialect());
//         return engine;
//     }

//     @Bean
//     public WebSecurityCustomizer webSecurityCustomizer() {
//         return (web) -> web.ignoring().requestMatchers(
//                 "/css/**",
//                 "/js/**",
//                 "/images/**",
//                 "/assets/**",
//                 "/assets_admin/**",
//                 "/fonts/**",
//                 "/lib/**");
//     }

//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http.csrf().disable()
//                 .formLogin(f -> {
//                     try {
//                         f.loginPage("/login")
//                                 .usernameParameter("email")
//                                 .passwordParameter("password")
//                                 .defaultSuccessUrl("/", true)
//                                 .failureHandler(new CustomAuthenticationFailureHandler())
//                                 .and()
//                                 .oauth2Login()
//                                 .loginPage("/login")
//                                 .userInfoEndpoint()
//                                 .userService(oauthUserService)
//                                 .and()
//                                 .successHandler(new AuthenticationSuccessHandler() {
//                                     @Override
//                                     public void onAuthenticationSuccess(HttpServletRequest request,
//                                             HttpServletResponse response,
//                                             Authentication authentication) throws IOException, ServletException {
//                                         System.out.println("AuthenticationSuccessHandler invoked");
//                                         System.out.println("Authentication name: " + authentication.getName());
//                                         Object principal = authentication.getPrincipal();

//                                         Boolean active = true;

//                                         if (principal instanceof OAuth2User) {
//                                             OAuth2User oauthUser = (OAuth2User) principal;
//                                             String fullName = oauthUser.getAttribute("name");
//                                             String email = oauthUser.getAttribute("email");
//                                             active = userService.processOAuthPostLogin(email, fullName);

//                                             Set<GrantedAuthority> authorities = extractAuthoritiesFromOAuth2User(
//                                                     oauthUser);

//                                             Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
//                                                     authentication.getPrincipal(),
//                                                     authentication.getCredentials(),
//                                                     authorities);

//                                             SecurityContextHolder.getContext().setAuthentication(newAuthentication);

//                                         }
//                                         if (active) {
//                                             response.sendRedirect("/index");
//                                         } else {
//                                             response.sendRedirect("/loginInactive");
//                                         }
//                                     }

//                                     private Set<GrantedAuthority> extractAuthoritiesFromOAuth2User(
//                                             OAuth2User oauthUser) {
//                                         Set<GrantedAuthority> authorities = new HashSet<>();
//                                         authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
//                                         return authorities;
//                                     }

//                                     private Set<GrantedAuthority> extractAuthoritiesFromDefaultOidcUser(
//                                             DefaultOidcUser oidcUser) {
//                                         Set<GrantedAuthority> authorities = new HashSet<>();
//                                         authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
//                                         return authorities;
//                                     }

//                                 });
//                     } catch (Exception e) {
//                         e.printStackTrace();
//                         throw new RuntimeException(e);
//                     }
//                 })

//                 .authorizeHttpRequests(at -> at
//                         .requestMatchers("/", "/home", "/appointment/**", "/login/**", "/loginError/**",
//                                 "/login-google", "/register", "/save", "re-send", "/image/**", "/js/**", "/css/**",
//                                 "/images/**",
//                                 "/lib/**", "/style/**", "/slider/**", "/assets/**",
//                                 "/blog/**", "/api/**", "recover", "send-otp-recover", "otp-check", "confirm-otp",
//                                 "send-otp-recover", "confirm-otp-recover", "/sendRequest/**",
//                                 "/api/**", "/pricing/**",
//                                 "save-new-password", "detail", "change-password", "save-change-password", "/users/**",
//                                 "/forgot/**", "/forgotPass/**", "/otp-check-pass/**", "/confirm-otp-pass/**",
//                                 "/resend-otp-pass/**", "/resend-otp-register/**", "/change-pass/**",
//                                 "/analyze/**", "/vendor/**",
//                                 "/assets/**", "/assets_admin/**", "/about/**", "/service/**", "/index", "/index/**",
//                                 "/fonts/**", "/404")
//                         .permitAll()
//                         .requestMatchers("/changePass/**", "/change-password/**").hasAnyRole("ADMIN", "USER")
//                         .requestMatchers("/auth/send-otp", "/auth/verify-otp", "/auth/resend-otp", "/profile/**",
//                                 "/change-password/**")
//                         .hasAnyRole("ADMIN", "USER")
//                         .requestMatchers("/admin/**").hasAnyRole("ADMIN")
//                         .requestMatchers("/user/**").hasAnyRole("USER")
//                         .anyRequest().authenticated());
//         return http.build();
//     }
// }

package com.demo.nutgrow.config;

import com.demo.nutgrow.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private UserService userService;
    @Autowired
    private CustomOAuth2UserService oauthUserService;
    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    ApplicationListener<AuthenticationSuccessEvent> successEvent() {
        return event -> {
            System.out.println("Success Login " + event.getAuthentication().getClass().getSimpleName() + " - "
                    + event.getAuthentication().getName());
        };
    }

    @Bean
    ApplicationListener<AuthenticationFailureBadCredentialsEvent> failureEvent() {
        return event -> {
            System.err.println("Bad Credentials Login " + event.getAuthentication().getClass().getSimpleName() + " - "
                    + event.getAuthentication().getName());
        };
    }

    @Bean
    public SpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver);
        engine.addDialect(new SpringSecurityDialect());
        return engine;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/css/**",
                "/js/**",
                "/images/**",
                "/assets/**",
                "/assets_admin/**",
                "/fonts/**",
                "/lib/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .formLogin(f -> {
                    try {
                        f.loginPage("/login")
                                .usernameParameter("email")
                                .passwordParameter("password")
                                // === SUCCESS HANDLER CHO FORM LOGIN ===
                                .successHandler(customAuthenticationSuccessHandler)
                                .failureHandler(new CustomAuthenticationFailureHandler())
                                .and()
                                .oauth2Login()
                                .loginPage("/login")
                                .userInfoEndpoint()
                                .userService(oauthUserService)
                                .and()
                                .successHandler(new AuthenticationSuccessHandler() {
                                    @Override
                                    public void onAuthenticationSuccess(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Authentication authentication) throws IOException, ServletException {
                                        System.out.println("AuthenticationSuccessHandler invoked");
                                        System.out.println("Authentication name: " + authentication.getName());
                                        Object principal = authentication.getPrincipal();

                                        Boolean active = true;

                                        if (principal instanceof OAuth2User) {
                                            OAuth2User oauthUser = (OAuth2User) principal;
                                            String fullName = oauthUser.getAttribute("name");
                                            String email = oauthUser.getAttribute("email");
                                            // Xử lý logic lưu user Google
                                            active = userService.processOAuthPostLogin(email, fullName);

                                            Set<GrantedAuthority> authorities = extractAuthoritiesFromOAuth2User(
                                                    oauthUser);

                                            Authentication newAuthentication = new UsernamePasswordAuthenticationToken(
                                                    authentication.getPrincipal(),
                                                    authentication.getCredentials(),
                                                    authorities);

                                            SecurityContextHolder.getContext().setAuthentication(newAuthentication);
                                        }

                                        if (active) {
                                            response.sendRedirect("/dashboard");
                                        } else {
                                            response.sendRedirect("/loginInactive");
                                        }
                                    }

                                    private Set<GrantedAuthority> extractAuthoritiesFromOAuth2User(
                                            OAuth2User oauthUser) {
                                        Set<GrantedAuthority> authorities = new HashSet<>();
                                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                                        return authorities;
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                })

                .authorizeHttpRequests(at -> at
                        // Cho phép truy cập công khai vào Landing Page và tài nguyên
                        .requestMatchers("/", "/home", "/index", "/appointment/**", "/login/**", "/loginError/**",
                                "/login-google", "/register", "/save", "re-send", "/image/**", "/js/**", "/css/**",
                                "/images/**",
                                "/lib/**", "/style/**", "/slider/**", "/assets/**",
                                "/blog/**", "/api/**", "recover", "send-otp-recover", "otp-check", "confirm-otp",
                                "send-otp-recover", "confirm-otp-recover", "/sendRequest/**",
                                "/api/**", "/pricing/**",
                                "save-new-password", "detail", "change-password", "save-change-password", "/users/**",
                                "/forgot/**", "/forgotPass/**", "/otp-check-pass/**", "/confirm-otp-pass/**",
                                "/resend-otp-pass/**", "/resend-otp-register/**", "/change-pass/**",
                                "/analyze/**", "/vendor/**",
                                "/assets/**", "/assets_admin/**", "/about/**", "/service/**", "/index/**",
                                "/fonts/**", "/404",
                                "/error")
                        .permitAll()

                        // Các quyền Admin/User cũ giữ nguyên
                        .requestMatchers("/changePass/**", "/change-password/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/auth/send-otp", "/auth/verify-otp", "/auth/resend-otp", "/profile/**",
                                "/change-password/**")
                        .hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER")

                        // QUAN TRỌNG: Mọi request khác (bao gồm /dashboard) bắt buộc phải đăng nhập
                        .anyRequest().authenticated());
        return http.build();
    }
}