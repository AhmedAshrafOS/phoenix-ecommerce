package com.vodafone.ecommerce.security.filter;

import com.vodafone.ecommerce.security.service.CustomUserDetailsService;
import com.vodafone.ecommerce.security.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = jwtService.resolveToken(request);

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (jwtService.validateToken(accessToken)) {
            Claims claims = jwtService.getClaims(accessToken);
            Long userId = jwtService.getUserIdFromToken(claims);

            String role = jwtService.getRole(claims);
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

            UserDetails userDetails = userDetailsService.loadUserById(userId);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, "", authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
