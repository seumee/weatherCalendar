package com.wcback.wcback.config;


import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {
    @Value("${jwt.password}")
    private String secretKey;
    // 토큰 생성 메서드
    public String createToken(String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + Duration.ofDays(1).toMillis()); // 만료기간  = 1일
        return Jwts.builder()
                .setHeaderParam(Header.TYPE,Header.JWT_TYPE)
                .setIssuer("WC") // 토큰 발급자
                .setIssuedAt(now) // 발급시간
                .setExpiration(expiration) // 만료기간
                .setSubject(subject) // 토큰 제목
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secretKey.getBytes())) // 알고리즘
                .compact();
    }

    // 토큰 유효성 검사
    public Claims parseJwtToken(String token) {
        String userToken = BearerRemove(token);
        return Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(secretKey.getBytes()))
                .parseClaimsJws(userToken)
                .getBody();
    }

    public String getPayload(String token) {
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));
        return payload.split("sub")[1]
                .replaceAll("\"","")
                .replaceAll(":","")
                .replaceAll("}","");
    }


    private static String BearerRemove(String token) {
        return token.substring("Bearer ".length());
    }

}
