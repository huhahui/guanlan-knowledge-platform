package com.guanlan.notes.demo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 演示 JWT 的签名机制 和 BCrypt 的哈希机制。
 * 与项目里的 JwtUtil / BCryptPasswordEncoder 行为一致（jjwt 0.9.1, HS512）。
 *
 * 运行方式：直接跑 main 方法。
 */
public class JwtBcryptDemo {

    // 与 application.yaml 保持一致
    private static final String SECRET = "local-example-signing-key";
    private static final long EXPIRATION_SECONDS = 2592000L; // 30 天

    public static void main(String[] args) {
        section("1. Generate a token (same as JwtUtil.generateToken)");
        String token = generateToken(1L);
        System.out.println("token = " + token);

        section("2. Decode header & payload -- anybody can read them");
        String[] parts = token.split("\\.");
        System.out.println("part count = " + parts.length);
        System.out.println("header  (decoded) = " + decode(parts[0]));
        System.out.println("payload (decoded) = " + decode(parts[1]));
        System.out.println("signature (base64) = " + parts[2]);

        section("3. Validate a normal token");
        System.out.println("validate(token) = " + validate(token));

        section("4. Tamper payload: change userId 1 -> 999, keep the old signature");
        String oldPayload = decode(parts[1]);
        String newPayload = oldPayload.replace("\"userId\":1", "\"userId\":999");
        String tampered = parts[0] + "." + encode(newPayload) + "." + parts[2];
        System.out.println("tampered payload = " + newPayload);
        System.out.println("tampered token   = " + tampered);
        System.out.println("validate(tampered) = " + validate(tampered));

        section("5. Attacker knows the secret -> can sign a valid forged token");
        String forged = generateToken(999L);
        System.out.println("forged token = " + forged);
        System.out.println("validate(forged) = " + validate(forged));
        System.out.println("userId in forged = " + getUserId(forged));

        section("6. Wrong secret -> signature check fails");
        System.out.println("validate with secret 'hacked' = " + validateWith(token, "hacked"));

        section("7. BCrypt: same password, two hashes are different");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String h1 = encoder.encode("123456");
        String h2 = encoder.encode("123456");
        System.out.println("hash1 = " + h1);
        System.out.println("hash2 = " + h2);
        System.out.println("hash1.equals(hash2) = " + h1.equals(h2));

        section("8. BCrypt: verify by re-hashing, NOT by decrypting");
        System.out.println("matches('123456', hash1) = " + encoder.matches("123456", h1));
        System.out.println("matches('654321', hash1) = " + encoder.matches("654321", h1));

        section("9. What the DB actually stores");
        System.out.println("user.password = " + h1);
        System.out.println("JWT in DB ?  = no, JWT is returned to client only");

        section("10. Brute-force the secret from a captured token");
        String[] dictionary = {
                "123456", "password", "secret", "admin", "abc123", "12345678", "qwerty"
        };
        long start = System.currentTimeMillis();
        String cracked = null;
        int tried = 0;
        for (String candidate : dictionary) {
            tried++;
            if (validateWith(token, candidate)) {
                cracked = candidate;
                break;
            }
        }
        long cost = System.currentTimeMillis() - start;
        System.out.println("captured token   = " + token);
        System.out.println("tried            = " + tried + " candidates");
        System.out.println("cracked secret   = " + cracked);
        System.out.println("cost             = " + cost + " ms");

        if (cracked != null) {
            String forged2 = forgeWith(cracked, 999L);
            System.out.println("now forge userId=999 -> validate = " + validateWith(forged2, cracked));
        }
    }

    /** 用指定 secret 伪造一个 userId 的 token（模拟攻击者） */
    private static String forgeWith(String secret, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_SECONDS * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // ---------- 与 JwtUtil 相同的逻辑 ----------

    private static String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_SECONDS * 1000))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    private static boolean validate(String token) {
        return validateWith(token, SECRET);
    }

    private static boolean validateWith(String token, String secret) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Long getUserId(String token) {
        Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
        return Long.valueOf(claims.get("userId").toString());
    }

    // ---------- Base64Url 编解码（JWT 用的就是它） ----------

    private static String decode(String base64Url) {
        return new String(Base64.getUrlDecoder().decode(base64Url), StandardCharsets.UTF_8);
    }

    private static String encode(String text) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("========== " + title + " ==========");
    }
}
