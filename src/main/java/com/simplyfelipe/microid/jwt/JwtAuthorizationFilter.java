package com.simplyfelipe.microid.jwt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplyfelipe.microid.entity.Role;
import com.simplyfelipe.microid.entity.RoleName;
import com.simplyfelipe.microid.response.ServiceResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final String ROLES_KEY = "roles";
    private static final String EMPTY_CREDENTIALS = "";
    private static final String SESSION_EXPIRED_MSG = "The current session has expired, please login again";

    private final JwtUtil jwtUtil;
    private final ObjectMapper mapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String accessToken = jwtUtil.resolveToken(request);
            if (ObjectUtils.isEmpty(accessToken)) {
                filterChain.doFilter(request, response);
                return;
            }

            Claims claims = jwtUtil.resolveClaims(request);

            if (jwtUtil.validateClaims(claims)) {
                String email = claims.getSubject();

                List<Role> roleList = Optional.of(claims)
                                .map(optClaims -> optClaims.get(ROLES_KEY))
                                .map(roles -> mapper.convertValue(roles, new TypeReference<List<String>>() {}))
                                .map(Collection::stream)
                                .orElse(Stream.of(RoleName.USER.value))
                                .map(RoleName::byName)
                                .map(Role::new)
                                .toList();

                Authentication authentication = new UsernamePasswordAuthenticationToken(email, EMPTY_CREDENTIALS, roleList);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            processErrorResponse(response, HttpStatus.UNAUTHORIZED, SESSION_EXPIRED_MSG);
            return;
        } catch (Exception e) {
            processErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void processErrorResponse(HttpServletResponse response, HttpStatus status, String errorMessage) throws IOException {
        ServiceResponse<Object> errorResponse = ServiceResponse.builder()
                .code(status.value())
                .status(status.name())
                .message(errorMessage)
                .build();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), errorResponse);
    }
}