package com.evidencevault.config;

import com.evidencevault.security.CustomUserDetailsService;
import com.evidencevault.security.LoginRateLimitFilter;
import com.evidencevault.security.LoginSecurityService;
import com.evidencevault.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final AuditService audit;
    private final LoginSecurityService loginSecurity;
    private final LoginRateLimitFilter rateLimitFilter;
    private final boolean hsts;

    public SecurityConfig(AuditService audit, LoginSecurityService loginSecurity,
                          LoginRateLimitFilter rateLimitFilter,
                          @Value("${app.security.hsts:false}") boolean hsts){
        this.audit=audit; this.loginSecurity=loginSecurity; this.rateLimitFilter=rateLimitFilter; this.hsts=hsts;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,CustomUserDetailsService userDetailsService) throws Exception {
        http
            .userDetailsService(userDetailsService)
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/error", "/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers("/cases/*/archive", "/cases/*/restore").hasRole("ADMIN")
                .requestMatchers("/audit/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request,response,authentication) -> {
                    loginSecurity.recordSuccess(authentication.getName());
                    audit.log(authentication.getName(),"LOGIN_SUCCESS","AUTH",authentication.getName(),"SUCCESS",clientIp(request),"Successful login");
                    response.sendRedirect("/dashboard");
                })
                .failureHandler((request,response,exception) -> {
                    String username=request.getParameter("username");
                    username=username==null?"unknown":username.trim();
                    loginSecurity.recordFailure(username);
                    audit.log(username,"LOGIN_FAILURE","AUTH",username,"FAILURE",clientIp(request),"Authentication failed");
                    response.sendRedirect("/login?error");
                })
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation(fix -> fix.migrateSession())
                .maximumSessions(3)
            )
            .headers(headers -> {
                headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com data:; img-src 'self' data:; object-src 'none'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'"));
                headers.frameOptions(frame -> frame.deny());
                headers.referrerPolicy(ref -> ref.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER));
                if (hsts) headers.httpStrictTransportSecurity(h -> h.includeSubDomains(true).maxAgeInSeconds(31536000));
                else headers.httpStrictTransportSecurity(h -> h.disable());
            })
            .logout(logout -> logout
                .addLogoutHandler((request, response, authentication) -> {
                    if(authentication != null) audit.log(authentication.getName(),"LOGOUT","AUTH",authentication.getName(),"SUCCESS",clientIp(request),"User logged out");
                })
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(exception -> exception.accessDeniedPage("/403"));
        return http.build();
    }

    private static String clientIp(HttpServletRequest request){return request.getRemoteAddr();}
}
