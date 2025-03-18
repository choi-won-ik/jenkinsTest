package com.example.demo.config.filter;

import com.example.demo.model.Member;
import com.example.demo.model.MemberDto;
import com.example.demo.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.time.Duration;

//@RequiredArgsConstructor
//public class LoginFilter extends UsernamePasswordAuthenticationFilter {
public class LoginFilter extends AbstractAuthenticationProcessingFilter {
//    private final AuthenticationManager authenticationManager;

    public LoginFilter(AntPathRequestMatcher antPathRequestMatcher, AuthenticationManager authenticationManager) {
        super(antPathRequestMatcher, authenticationManager);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);
        if(failed.getMessage().equals("No value present") || failed instanceof BadCredentialsException) {

        }
    }

    // 원래는 form-data 형식으로 사용자 정보를 입력받았는데
    // 우리는 JSON 형태로 입력을 받기 위해서 재정의
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("LoginFilter 실행됐다.");
        UsernamePasswordAuthenticationToken authToken;
        // 그림에서 1번 로직
//        UserDto.SignupRequest userDto =
//                new UserDto.SignupRequest(request.getParameter("username"), request.getParameter("password"));
        try {
            // 그림에서 원래 1번이었던 로직을 JSON 형태의 데이터를 처리하도록 변경
            MemberDto.SignupRequest memberDto  = new ObjectMapper().readValue(request.getInputStream(), MemberDto.SignupRequest.class);

            // 그림에서 2번 로직
            authToken =
                    new UsernamePasswordAuthenticationToken(memberDto.getUserid(), memberDto.getPassword(), null);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 그림에서 3번 로직
//        return authenticationManager.authenticate(authToken);
        return this.getAuthenticationManager().authenticate(authToken);
    }


    // 그림에서 9번 로직
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        Member member = (Member) authResult.getPrincipal();
        String jwtToken = JwtUtil.generateToken(member.getIdx(),
                member.getUserid());


//        일반적인 객체 생성 및 객체의 변수에 값을 설정하는 방법
//        ResponseCookie cookie = new ResponseCookie();
//        cookie.setPath("/");
//        cookie.setHttpOnly(true);

//        빌더 패턴으로 객체를 생성하면서 값을 설정하는 방법
        ResponseCookie cookie = ResponseCookie
                .from("ATOKEN", jwtToken)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofHours(1L))
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}