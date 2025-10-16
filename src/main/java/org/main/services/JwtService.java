package org.main.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.main.models.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-ttl-seconds}")
    private long jwtTtl;

    // --------------------------
    // Gera token JWT a partir de claims
    // --------------------------
    public String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtTtl * 1000);

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
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    // --------------------------
    // Extrai claims do token
    // --------------------------
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
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
            claims.put("fotoPerfil", oauthUser.getAttribute("picture"));
            claims.put("fotoCapa", oauthUser.getAttribute("cover")); // caso exista
        } else if (authentication.getPrincipal() instanceof User user) {
            claims.put("name", user.getUsername());
            claims.put("email", usuarioLocal.getEmail());
            claims.put("provider", "local");
            claims.put("fotoPerfil", usuarioLocal.getImagemPerfil());
            claims.put("fotoCapa", usuarioLocal.getImagemCapa());
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

     // Extrai foto de perfil
    	Object pictureObj = profile.get("picture");
    	if (pictureObj instanceof Map pictureMap) {
    		Object dataObj = pictureMap.get("data");
    		if (dataObj instanceof Map dataMap) {
    			claims.put("fotoPerfil", dataMap.get("url"));
    		}
    	} else if (pictureObj instanceof String url) {
    		claims.put("fotoPerfil", url);
    	}

     // Extrai foto de capa
    	Object coverObj = profile.get("cover");
    	if (coverObj instanceof Map coverMap) {
    		claims.put("fotoCapa", coverMap.get("source"));
    	} else if (coverObj instanceof String url) {
    		claims.put("fotoCapa", url);
    	}

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