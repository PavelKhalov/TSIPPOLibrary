package ru.khalov.tsippolibrary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.khalov.tsippolibrary.dto.UserDto;
import ru.khalov.tsippolibrary.dto.request.UserUpdateRequest;
import ru.khalov.tsippolibrary.entity.User;
import ru.khalov.tsippolibrary.repository.UserRepository;
import ru.khalov.tsippolibrary.util.Role;
import ru.khalov.tsippolibrary.util.expeption.UsernameTakenException;
import ru.khalov.tsippolibrary.util.mapper.UserMapper;

import java.util.Set;

/**
 * Управление пользователями — ТЗ 3.1.4, 3.1.5, диаграммы ВИ-9, ВИ-11, ВИ-12, ВИ-13, ВИ-15.
 * - Профиль: свой для всех
 * - Список/просмотр — ADMIN/OWNER
 * - Удаление — ADMIN/OWNER, но ADMIN не может удалять ADMIN/OWNER
 * - Роли — только OWNER (ВИ-13)
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto getProfile(User currentUser) {
        return userMapper.toDto(currentUser);
    }

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public Page<UserDto> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public UserDto getById(Long id) {
        User u = userRepository.findById(id).orElseThrow(() ->
                new ru.khalov.tsippolibrary.util.expeption.UserNotFoundException(id));
        return userMapper.toDto(u);
    }

    /** Пользователь редактирует свой профиль; ADMIN/OWNER могут редактировать любой */
    @Transactional
    public UserDto updateProfile(Long targetId, UserUpdateRequest req, User currentUser) {
        boolean isOwner = currentUser.getRoles().contains(Role.OWNER);
        boolean isSelf = currentUser.getId().equals(targetId);
        boolean isAdmin = currentUser.getRoles().contains(Role.ADMIN);

        if (!isSelf && !isAdmin && !isOwner) {
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        }

        User target = userRepository.findById(targetId).orElseThrow(() ->
                new ru.khalov.tsippolibrary.util.expeption.UserNotFoundException(targetId));

        // ADMIN не может редактировать ADMIN/OWNER (ТЗ 3.1.4, 5.2)
        if (isAdmin && !isOwner && !isSelf) {
            if (target.getRoles().contains(Role.ADMIN) || target.getRoles().contains(Role.OWNER)) {
                throw new org.springframework.security.access.AccessDeniedException("Admin cannot edit admin/owner");
            }
        }

        if (req.username() != null && !req.username().isBlank()) {
            target.setUsername(req.username());
        }
        if (req.email() != null && !req.email().isBlank()) {
            if (!req.email().equals(target.getEmail()) && userRepository.existsByEmail(req.email())) {
                throw new UsernameTakenException(req.email());
            }
            target.setEmail(req.email());
        }
        User saved = userRepository.save(target);
        return userMapper.toDto(saved);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    public void deleteUser(Long id, User currentUser) {
        User target = userRepository.findById(id).orElseThrow(() ->
                new ru.khalov.tsippolibrary.util.expeption.UserNotFoundException(id));

        if (target.getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Cannot delete yourself");
        }
        // ADMIN не может удалять ADMIN/OWNER
        if (currentUser.getRoles().contains(Role.ADMIN) && !currentUser.getRoles().contains(Role.OWNER)) {
            if (target.getRoles().contains(Role.ADMIN) || target.getRoles().contains(Role.OWNER)) {
                throw new org.springframework.security.access.AccessDeniedException("Admin cannot delete admin/owner");
            }
        }
        // OWNER в системе один — нельзя удалить последнего OWNER (исправлено: count без findAll)
        if (target.getRoles().contains(Role.OWNER)) {
            long ownerCount = userRepository.countByRolesContaining(Role.OWNER);
            if (ownerCount <= 1) {
                throw new IllegalStateException("Cannot delete the last OWNER");
            }
        }
        userRepository.delete(target);
    }

    /** Только OWNER — ВИ-13 */
    @Transactional
    @PreAuthorize("hasRole('OWNER')")
    public UserDto updateRole(Long userId, Role role) {
        User target = userRepository.findById(userId).orElseThrow(() ->
                new ru.khalov.tsippolibrary.util.expeption.UserNotFoundException(userId));

        if (role == Role.OWNER) {
            throw new IllegalArgumentException("Cannot assign OWNER via API");
        }
        // Снимаем/ставим ADMIN — исправлено: Set.of immutable → mutable HashSet
        if (role == Role.ADMIN) {
            target.setRoles(new java.util.HashSet<>(Set.of(Role.USER, Role.ADMIN)));
        } else if (role == Role.USER) {
            if (target.getRoles().contains(Role.OWNER)) {
                throw new IllegalArgumentException("Cannot remove OWNER role via this endpoint");
            }
            // снять ADMIN: мутабельная копия
            java.util.Set<Role> roles = new java.util.HashSet<>(target.getRoles());
            roles.remove(Role.ADMIN);
            if (roles.isEmpty()) roles.add(Role.USER);
            target.setRoles(roles);
        }
        User saved = userRepository.save(target);
        return userMapper.toDto(saved);
    }
}
