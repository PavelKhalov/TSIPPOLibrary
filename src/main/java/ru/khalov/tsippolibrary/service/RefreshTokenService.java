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

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt-refresh-expiration}")
    private Long refreshExpiration;

    @Transactional
    public RefreshToken create(User user){
        refreshTokenRepository.deleteAllByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiredAt(Instant.now().plusMillis(refreshExpiration));

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verify(String refreshToken){
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken).orElseThrow(() ->
                new RefreshTokenException("Refresh token not found"));

        if(token.isExpired() || token.isRevoked()){
            refreshTokenRepository.delete(token);
            throw new RefreshTokenException("Refresh token is expired");
        }
        return token;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revoke(String refreshToken){
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token ->{
            token.setRevoked(true);
            refreshTokenRepository.save(token);}
        );
    }
}
