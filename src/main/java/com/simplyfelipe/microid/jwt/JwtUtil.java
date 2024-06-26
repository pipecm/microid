package com.simplyfelipe.microid.jwt;

import com.simplyfelipe.microid.entity.Role;
import com.simplyfelipe.microid.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
public class JwtUtil {
    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_EXPIRED = "expired";
    private static final String KEY_INVALID = "invalid";
    private static final String KEY_ROLES = "roles";

    private final JwtUtilParameters jwtUtilParameters;
    private final JwtParser jwtParser;

    public String createToken(User user) {
        Claims claims = Jwts.claims()
                .subject(user.getEmail())
                .add(KEY_EMAIL, user.getEmail())
                .add(KEY_ROLES, user.getRoles().stream().map(Role::getRoleName).toList())
                .build();

        Date tokenCreateTime = new Date();
        Date tokenValidity = new Date(tokenCreateTime.getTime() + TimeUnit.MINUTES.toMillis(jwtUtilParameters.getExpiration()));

        return Jwts.builder()
                .claims(claims)
                .expiration(tokenValidity)
                .signWith(Keys.hmacShaKeyFor(jwtUtilParameters.getSecretKey().getBytes()))
                .compact();
    }

    private Claims parseJwtClaims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }

    public Date getExpiration(String token) {
        return parseJwtClaims(token).getExpiration();
    }

    public Claims resolveClaims(HttpServletRequest req) {
        try {
            String token = resolveToken(req);
            if (!ObjectUtils.isEmpty(token)) {
                return parseJwtClaims(token);
            }
            return new DefaultClaims(Collections.emptyMap());
        } catch (ExpiredJwtException ex) {
            req.setAttribute(KEY_EXPIRED, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            req.setAttribute(KEY_INVALID, ex.getMessage());
            throw ex;
        }
    }

    public String resolveToken(HttpServletRequest httpServletRequest) {
        return Optional.ofNullable(httpServletRequest)
                .map(request -> request.getHeader(TOKEN_HEADER))
                .filter(token -> token.startsWith(TOKEN_PREFIX))
                .map(token -> token.substring(TOKEN_PREFIX.length()))
                .orElse(null);
    }

    public boolean validateClaims(Claims claims) {
        return Optional.ofNullable(claims)
                .map(Claims::getExpiration)
                .map(expiration -> expiration.after(new Date()))
                .orElse(false);
    }
}
