package ru.khalov.tsippolibrary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Refresh-токен: хранится в БД в виде SHA-256 хеша (ТЗ 3.5.3), срок 30 дней.
 * Клиенту отдаётся сырой UUID, в БД — его хеш.
 * Исправлено: убран конфликтующий @Transient getToken/setToken — используем tokenHash.
 */
@Table(name = "refresh_tokens")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // Хеш токена (не сам токен) — требование безопасности ТЗ 3.5.3
    @Column(name = "token_hash", nullable = false, unique = true, length = 512)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "expired_at", nullable = false)
    private Instant expiredAt;

    // Признак отозванности — для инвалидации при logout/refresh
    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private boolean revoked = false;

    public boolean isExpired() {
        return Instant.now().isAfter(expiredAt);
    }

    /** Алиас для отладки: вернуть hash (сырой токен клиенту не храним). */
    public String getToken() {
        return tokenHash;
    }
}
