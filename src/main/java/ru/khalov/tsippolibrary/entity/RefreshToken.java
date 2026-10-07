package ru.khalov.tsippolibrary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.collections4.functors.InstantiateTransformer;

import java.time.Instant;
import java.util.IdentityHashMap;

@Table(name = "refresh_token")
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

    @Column(name = "token", nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "expired_at", nullable = false)
    private Instant expiredAt;

    // Призниак отозванности
    private boolean revoked = false;

    public boolean isExpired(){
        return Instant.now().isAfter(expiredAt);
    }
}
