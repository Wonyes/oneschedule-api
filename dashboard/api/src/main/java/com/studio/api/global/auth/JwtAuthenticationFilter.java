package com.studio.api.global.auth;

import com.studio.api.global.config.CookieProperties;

import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter  {

    private final AuthTokenProvider authTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
            ) throws ServletException, IOException
    {
        String token = extractCookie(request, CookieProperties.ACCESS);

        if (token != null) {
            try {
                AuthToken authToken = authTokenProvider.convertAuthToken(token);

                if (authToken.isTokenValid()) {
                    Long memberNo = authToken.getMemberNo();
                    var authentication = new UsernamePasswordAuthenticationToken(
                            memberNo, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (CustomException e) {
                // 만료/위조 구분을 EntryPoint에 넘긴다. 여기서 401을 내지는 않는다.
                request.setAttribute(JsonAuthErrorHandler.AUTH_ERROR_ATTRIBUTE, e.getErrorCode());
                SecurityContextHolder.clearContext();
            } catch (JwtException e) {
                request.setAttribute(JsonAuthErrorHandler.AUTH_ERROR_ATTRIBUTE, ErrorCode.JWT_ERROR_TOKEN);
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

}
