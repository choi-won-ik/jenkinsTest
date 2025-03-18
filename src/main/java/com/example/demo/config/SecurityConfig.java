package com.example.demo.config;


import com.example.demo.config.filter.JwtFilter;
import com.example.demo.config.filter.LoginFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationConfiguration configuration;
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "https://www.breadbook.kro.kr"));
//        configuration.setAllowedMethod(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // @Component vs @Bean
    // @Component : 프로젝트가 실행될 때 컴포넌트 스캔을 통해서 객체를 생성해서 스프링 컨테이너에 Bean으로 등록
    //              개발자가 직접 개발한 클래스의 객체를 등록할 때 사용
    // @Bean : 컴포넌트 스캔 X, 개발자가 직접 객체를 생성해서 스프링 컨테이너에 Bean으로 등록
    //              라이브러리를 가져와서 라이브러리의 객체를 등록할 때 사용
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        // <script>alert("asd")</script>, 일반적으로 XSS
        // <script>게시글 작성 스크립트</script>, 일반적으로 CSRF
        // CSRF : 크로스 사이트 요청 변조
        http.cors(cors -> corsConfigurationSource());
        http.csrf(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.sessionManagement(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.logout(logout -> logout.logoutUrl("/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(HttpServletResponse.SC_OK);
                })
                .deleteCookies("ATOKEN"));

        http.authorizeHttpRequests(
                (auth) -> auth
                        .requestMatchers("/user/signup", "/user/login","/logout",
                                "/user/signup-oauth", "/user/auth/check", "/user/verify/**",
                                "/user/id_info", "/user/password/reset", "/user/password/find",
                                "/product/list",
                                "/error", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/chatting/rooms").permitAll()
                        .requestMatchers("/api/chatting/rooms/**").permitAll()
                        .requestMatchers("/api/chatting/room").permitAll()
                        .requestMatchers("/api/chatting/room/**").permitAll()
                        .requestMatchers("/swagger-resources/**","/cookies/user").permitAll()
//                        .requestMatchers("/feed/register").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
        );



        http.addFilterAt(new LoginFilter(new AntPathRequestMatcher("/user/login", "POST"), configuration.getAuthenticationManager()), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new JwtFilter(), UsernamePasswordAuthenticationFilter.class);



        return http.build();
    }
}
