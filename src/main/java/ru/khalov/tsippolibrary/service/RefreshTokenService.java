package ru.khalov.tsippolibrary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.khalov.tsippolibrary.entity.RefreshToken;
import ru.khalov.tsippolibrary.entity.User;
import ru.khalov.tsippolibrary.repository.RefreshTokenRepository;
import ru.khalov.tsippolibrary.util.expeption.RefreshTokenException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Сервис refresh-токенов (ТЗ 3.5.3):
 * - храним SHA-256 хеш в БД
 * - клиенту отдаём сырой UUID
 * - проверяем по хешу
 */
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt-refresh-expiration}")
    private Long refreshExpiration;

    private static String sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Создаёт токен для пользователя, старый удаляется (ротация).
     * Возвращает сырой токен для отправки клиенту.
     */
    @Transactional
    public String createRawToken(User user) {
        refreshTokenRepository.deleteAllByUser(user);
        String raw = UUID.randomUUID().toString();
        String hash = sha256(raw);
        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(hash)
                .expiredAt(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();
        refreshTokenRepository.save(entity);
        return raw;
    }

    /** Для совместимости: возвращает сущность-обёртку с сырым токеном (raw отдаётся клиенту, в БД лежит hash) */
    @Transactional
    public RefreshToken create(User user) {
        String raw = createRawToken(user);
        // ищем только что сохранённый hash
        String hash = sha256(raw);
        RefreshToken saved = refreshTokenRepository.findByTokenHash(hash).orElseThrow();
        // Вернём detached объект где tokenHash = raw (для AuthResponse клиенту нужен raw)
        return RefreshToken.builder()
                .id(saved.getId())
                .user(user)
                .tokenHash(raw) // тут raw — клиенту (не хэш)
                .expiredAt(saved.getExpiredAt())
                .revoked(false)
                .build();
    }

    @Transactional
    public RefreshToken verify(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        // Исправлено: поле tokenHash, метод findByTokenHash (ТЗ 3.5.3)
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash).orElseThrow(() ->
                new RefreshTokenException("Refresh token not found"));
        if (token.isExpired() || token.isRevoked()) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenException("Refresh token is expired or revoked");
        }
        return token;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revoke(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
}
