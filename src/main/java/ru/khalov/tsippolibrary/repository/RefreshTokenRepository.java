package ru.khalov.tsippolibrary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.khalov.tsippolibrary.entity.RefreshToken;
import ru.khalov.tsippolibrary.entity.User;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Исправлено: поле называется tokenHash, а не token (ТЗ 3.5.3 — храним хеш)
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Для обратной совместимости алиас (если где-то используется старое имя)
    default Optional<RefreshToken> findByToken(String tokenHash) {
        return findByTokenHash(tokenHash);
    }

    void deleteAllByUser(User user);

    // Поиск всех токенов пользователя (для ротации)
    java.util.List<RefreshToken> findAllByUser(User user);
}
