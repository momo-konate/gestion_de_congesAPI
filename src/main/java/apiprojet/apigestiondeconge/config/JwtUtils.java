package apiprojet.apigestiondeconge.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    private SecretKey getSigningKey() {
        // Clé secrète de signature (minimum 256 bits / 32 caractères pour HS256)
        String jwtSecret = "MaCleSecreteHyperSecuriseePourNotreProjetGestionDeConge2026!";
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Générer un Token JWT
    public String generateToken(String email, String role) {
        // 24 heures
        int jwtExpirationMs = 86400000;
        return Jwts.builder().subject(email)
                .claim("role", role).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extraire l'email (Subject) du token
    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload() //   Utiliser getPayload() avec la nouvelle API JJWT
                .getSubject();
    }

    // Valider le token JWT
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Extraire le Rôle du token
    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}