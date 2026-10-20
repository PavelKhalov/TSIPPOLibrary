package ru.khalov.tsippolibrary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.khalov.tsippolibrary.util.Role;

import java.time.Instant;
import java.util.HashSet;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Пользователь системы — сущность User.
 * Согласно ТЗ п. 3.3.2 и 3.1.1: имя, уникальный email, пароль (хеш), роли.
 * Реализует UserDetails для интеграции со Spring Security + JWT.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Имя пользователя (отображаемое имя)
    @Column(name = "username", nullable = false)
    private String username;

    // Email — уникален, используется для входа (ТЗ 3.1.1)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    // Роли: USER, ADMIN, OWNER (ТЗ 3.5.2). По умолчанию USER
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return password;
    }

    // Spring Security использует username как principal, но у нас вход по email.
    // Поэтому getUsername() возвращает email (см. JwtService, UserDetailsService).
    @Override
    public String getUsername() {
        return email;
    }

    /** Отображаемое имя (для DTO). */
    public String getDisplayName() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
