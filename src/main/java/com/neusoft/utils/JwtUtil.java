package com.neusoft.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT 工具类
 * 修复：
 * 1. 密钥从配置文件注入，不再硬编码
 * 2. 使用 Keys.hmacShaKeyFor() 替代已弃用的明文 String 签名方式
 * 3. 密钥长度校验（HMAC-SHA512 需要至少 64 字节）
 * 4. 增加刷新token方法
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretStr;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secretStr.getBytes(StandardCharsets.UTF_8);
        // 长度不足时自动补全（保证兼容旧密钥）
        if (keyBytes.length < 64) {
            byte[] padded = new byte[64];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Token
     *
     * @param userId 用户ID
     */
    public String generateToken(Integer userId) {
        return Jwts.builder()
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从 Token 解析 userId
     */
    public Integer getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Integer.class);
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false; // token 已过期
        } catch (Exception e) {
            return false; // token 无效
        }
    }

    /**
     * 刷新 Token
     */
    public String refreshToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Integer userId = claims.get("userId", Integer.class);

        // 检查刷新token是否在有效期内
        Date issuedAt = claims.getIssuedAt();
        Date now = new Date();
        long refreshExpireTime = issuedAt.getTime() + refreshExpiration;

        if (now.getTime() > refreshExpireTime) {
            throw new ExpiredJwtException(null, claims, "刷新token已过期");
        }

        // 生成新的token
        return generateToken(userId);
    }
}