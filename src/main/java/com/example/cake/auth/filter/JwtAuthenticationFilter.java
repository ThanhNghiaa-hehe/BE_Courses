package com.example.cake.auth.filter;

import com.example.cake.auth.model.UserPrincipal;
import com.example.cake.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path =request.getServletPath();
        if(path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;

        // 1. Lấy token từ header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 2. Nếu có token thì kiểm tra hợp lệ
        if (token != null && jwtService.isValid(token)) {
            Claims claims = jwtService.extractAllClaims(token);
            String userId = claims.get("id", String.class);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            // 3. Tạo UserPrincipal
            UserPrincipal userPrincipal = new UserPrincipal(
                    userId,
                    email,
                    null,  // không cần password
                    role,
                    true   // giả định active = true
            );

            // 4. Tạo Authentication và set vào SecurityContext
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal,
                    null,
                    userPrincipal.getAuthorities()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 5. Cho request đi tiếp
        filterChain.doFilter(request, response);
    }
}
