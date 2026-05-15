package org.main.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.main.models.Usuario;
import org.main.models.JwtSigningKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;

@Service
public class JwtService {

    private final JwtKeyRotationService jwtKeyRotationService;
    private final ObjectMapper objectMapper;

    @Value("${jwt.access-token-ttl-seconds}")
    private long jwtTtl;

    public JwtService(JwtKeyRotationService jwtKeyRotationService, ObjectMapper objectMapper) {
        this.jwtKeyRotationService = jwtKeyRotationService;
        this.objectMapper = objectMapper;
    }

    private SecretKey signingKeyFor(JwtSigningKey key) {
        byte[] secretBytes = Base64.getDecoder().decode(key.getSecretBase64().trim());
        return Keys.hmacShaKeyFor(secretBytes);
    }

    private String keyIdHeader(JwtSigningKey key) {
        return String.valueOf(key.getKeyVersion());
    }

    private Integer extractKeyVersionFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            JsonNode header = objectMapper.readTree(headerJson);
            JsonNode kidNode = header.get("kid");
            if (kidNode == null || kidNode.isNull()) {
                return null;
            }

            String kid = kidNode.asText();
            if (kid == null || kid.isBlank()) {
                return null;
            }

            return Integer.valueOf(kid.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private Claims parseWithKey(String token, JwtSigningKey key) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKeyFor(key))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // --------------------------
    // Gera token JWT a partir de claims
    // --------------------------
    public String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtTtl * 1000);
        JwtSigningKey activeKey = jwtKeyRotationService.getActiveKey();

        // Se tiver uid, esse é o identificador oficial
        String subject;
        if (claims.containsKey("uid")) {
            subject = String.valueOf(claims.get("uid"));
            claims.put("sub", subject); // garante consistência
        } else {
            subject = String.valueOf(claims.get("sub"));
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
            .setHeaderParam("kid", keyIdHeader(activeKey))
            .signWith(signingKeyFor(activeKey), SignatureAlgorithm.HS256)
                .compact();
    }

    // --------------------------
    // Extrai claims do token
    // --------------------------
    public Claims extractAllClaims(String token) {
        Integer keyVersion = extractKeyVersionFromToken(token);
        if (keyVersion != null) {
            Optional<JwtSigningKey> resolved = jwtKeyRotationService.findByVersion(keyVersion);
            if (resolved.isPresent()) {
                return parseWithKey(token, resolved.get());
            }
        }

        for (JwtSigningKey key : jwtKeyRotationService.findUsableKeys()) {
            try {
                return parseWithKey(token, key);
            } catch (Exception ignored) {
                // tenta a próxima chave válida
            }
        }

        throw new IllegalArgumentException("Token inválido ou assinado com uma chave expirada.");
    }

    // --------------------------
    // Valida token
    // --------------------------
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long getTokenRemainingSeconds(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        if (expiration == null) {
            return -1L;
        }

        long remainingMillis = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0L, remainingMillis / 1000L);
    }

    // --------------------------
    // Claims padrão a partir do Authentication (Google, Local)
    // --------------------------
    public static Map<String, Object> defaultClaims(Authentication authentication, Usuario usuarioLocal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", String.valueOf(usuarioLocal.getIdUsuario()));
        claims.put("uid", usuarioLocal.getIdUsuario());
        claims.put("roles", authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList()));

        if (authentication.getPrincipal() instanceof OAuth2User oauthUser) {
            claims.put("name", oauthUser.getAttribute("name"));
            claims.put("email", oauthUser.getAttribute("email"));
            claims.put("provider", ((OAuth2AuthenticationToken) authentication)
                    .getAuthorizedClientRegistrationId());
        } else if (authentication.getPrincipal() instanceof User user) {
            claims.put("name", user.getUsername());
            claims.put("email", usuarioLocal.getEmail());
            claims.put("provider", "local");
        }

        return claims;
    }

    // --------------------------
    // Claims padrão a partir do Map (Facebook ou Google)
    // --------------------------
    public static Map<String, Object> defaultClaims(Map<String, Object> profile, Usuario usuarioLocal) {
    	Map<String, Object> claims = new HashMap<>();
    	claims.put("sub", String.valueOf(usuarioLocal.getIdUsuario()));
    	claims.put("uid", usuarioLocal.getIdUsuario());
    	claims.put("roles", List.of("ROLE_CONSUMIDOR"));
    	claims.put("name", profile.get("name"));
    	claims.put("email", profile.get("email"));
    	claims.put("provider", profile.getOrDefault("provider", "facebook"));

    	return claims;
    }

    public static Map<String, Object> defaultClaimsFromUsuario(Usuario usuarioLocal, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", String.valueOf(usuarioLocal.getIdUsuario()));
        claims.put("uid", usuarioLocal.getIdUsuario());
        claims.put("roles", roles);
        claims.put("name", usuarioLocal.getNomeLogin());
        claims.put("email", usuarioLocal.getEmail());
        claims.put("provider", usuarioLocal.getOauthProvider() != null ? usuarioLocal.getOauthProvider() : "local");
        return claims;
    }

    // --------------------------
    // Extrair subject direto
    // --------------------------
    public String getUsernameFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    // --------------------------
    // Extrair claim genérica
    // --------------------------
    public <T> T extractClaim(String token, String claimName, Class<T> clazz) {
        Claims claims = extractAllClaims(token);
        return clazz.cast(claims.get(claimName));
    }

    // --------------------------
    // Extrair UID (sempre Long)
    // --------------------------
    public Long extractUserId(String token) {
        Object uid = extractClaim(token, "uid", Object.class);
        if (uid == null) return null;
        if (uid instanceof Integer) return ((Integer) uid).longValue();
        if (uid instanceof Long)    return (Long) uid;
        if (uid instanceof String)  return Long.parseLong((String) uid);
        return null;
    }
}
