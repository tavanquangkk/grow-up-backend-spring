//package jp.trial.grow_up.config;
//
//import io.jsonwebtoken.JwtParser;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import jakarta.annotation.PostConstruct;
//import jp.trial.grow_up.domain.User;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.Resource;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.nio.charset.StandardCharsets;
//import java.security.KeyFactory;
//import java.security.PrivateKey;
//import java.security.spec.PKCS8EncodedKeySpec;
//import java.util.Base64;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//public class JwtUtil {
//    @Value("${jwt.secret}")
//    private String secretKey;
//
//    // jwt生成
//    public String generateToken(User user) {
//        long expirationTime = 1000 * 60 * 60; // one hour
//        return Jwts.builder()
//                .setSubject(user.getEmail())
//                .claim("role", user.getRole().name())
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
//                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    // refresh token 生成
//    public String generateRefreshToken(User user) {
//        long exprirationTime = 1000 * 60 * 60 * 168; // a week
//        return Jwts.builder()
//                .setSubject(user.getEmail())
//                .claim("role", user.getRole().name())
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + exprirationTime))
//                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    public boolean validateToken(String token, UserDetails userDetails) {
//        String username = extractUsername(token);
//        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
//    }
//
//    // username 取得
//    public String extractUsername(String token) {
//        JwtParser parser = Jwts.parserBuilder()
//                .setSigningKey(secretKey.getBytes()) // secretKeyをバイト配列で渡すのが一般的です
//                .build();
//        return parser.parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
//
//    private boolean isTokenExpired(String token) {
//        JwtParser parser = Jwts.parserBuilder()
//                .setSigningKey(secretKey.getBytes())
//                .build();
//        Date expiration = parser.parseClaimsJws(token)
//                .getBody()
//                .getExpiration();
//        return expiration.before(new Date());
//    }
//}
//
//

package jp.trial.grow_up.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jp.trial.grow_up.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.private-key}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key}")
    private Resource publicKeyResource;

    @Value("${jwt.issuer}")
    private String issuer;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        // --- 秘密鍵ロード (PKCS8 PEM) ---
        String privateKeyPem = new String(Files.readAllBytes(privateKeyResource.getFile().toPath()), StandardCharsets.UTF_8)
                .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        privateKey = KeyFactory.getInstance("RSA").generatePrivate(privateSpec);

        // --- 公開鍵ロード (X.509 PEM) ---
        String publicKeyPem = new String(Files.readAllBytes(publicKeyResource.getFile().toPath()), StandardCharsets.UTF_8)
                .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPem);
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
        publicKey = KeyFactory.getInstance("RSA").generatePublic(publicSpec);
    }

    // アクセストークン生成
    public String generateToken(User user) {
        long expirationTime = 1000 * 60 * 15; // 15分
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("role", user.getRole().name())
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(privateKey, SignatureAlgorithm.RS256) // RSA署名
                .compact();
    }

    // リフレッシュトークン生成
    public String generateRefreshToken(User user) {
        long expirationTime = 1000L * 60 * 60 * 24 * 7; // 1週間
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("userId", user.getId().toString())
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    // username 取得
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey) // 公開鍵で検証
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 検証
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }
}

