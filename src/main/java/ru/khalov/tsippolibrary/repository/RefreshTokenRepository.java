package ru.khalov.tsippolibrary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.khalov.tsippolibrary.entity.RefreshToken;
import ru.khalov.tsippolibrary.entity.User;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteAllByUser(User user);
}
