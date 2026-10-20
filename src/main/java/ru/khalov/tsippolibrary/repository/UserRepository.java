package ru.khalov.tsippolibrary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.khalov.tsippolibrary.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Вход по email (ТЗ 3.1.1, 3.2), но сохраняем поиск по username для совместимости
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // Для проверки OWNER без загрузки всех пользователей (ТЗ 3.1.5)
    @org.springframework.data.jpa.repository.Query("select count(u) > 0 from User u join u.roles r where r = :role")
    boolean existsByRolesContaining(@org.springframework.data.repository.query.Param("role") ru.khalov.tsippolibrary.util.Role role);

    @org.springframework.data.jpa.repository.Query("select count(u) from User u join u.roles r where r = :role")
    long countByRolesContaining(@org.springframework.data.repository.query.Param("role") ru.khalov.tsippolibrary.util.Role role);
}
