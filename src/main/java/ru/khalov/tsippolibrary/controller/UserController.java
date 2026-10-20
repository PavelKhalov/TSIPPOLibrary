package ru.khalov.tsippolibrary.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.khalov.tsippolibrary.dto.UserDto;
import ru.khalov.tsippolibrary.dto.request.RoleUpdateRequest;
import ru.khalov.tsippolibrary.dto.request.UserUpdateRequest;
import ru.khalov.tsippolibrary.entity.User;
import ru.khalov.tsippolibrary.service.UserService;

/**
 * Пользователи — ТЗ 3.2.7, ВИ-9, ВИ-11, ВИ-12, ВИ-13, ВИ-15
 * GET   /api/porfile              — свой профиль (опечатка из ТЗ сохранена как алиас, плюс /api/profile)
 * GET   /api/users                — список (ADMIN/OWNER)
 * GET   /api/users/{id}           — по id (ADMIN/OWNER)
 * PATCH /api/users/{id}           — редактировать профиль
 * DELETE /api/users/{id}          — удалить (ADMIN/OWNER)
 * PATCH /api/users/{id}/role      — назначить/снять ADMIN (только OWNER)
 */
@Tag(name = "Users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ВИ-9: свой профиль — поддержка обоих путей (опечатка в ТЗ: /api/porfile)
    @GetMapping({"/api/profile", "/api/porfile"})
    public ResponseEntity<UserDto> profile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getProfile(user));
    }

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @GetMapping("/api/users")
    public ResponseEntity<Page<UserDto>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.listUsers(pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @GetMapping("/api/users/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // Редактирование — свой или ADMIN/OWNER чужой
    @PatchMapping("/api/users/{id}")
    public ResponseEntity<UserDto> update(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.updateProfile(id, request, currentUser));
    }

    // Также PATCH /api/profile для удобства (редактировать себя)
    @PatchMapping({"/api/profile", "/api/porfile"})
    public ResponseEntity<UserDto> updateSelf(
            @RequestBody @Valid UserUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.updateProfile(currentUser.getId(), request, currentUser));
    }

    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        userService.deleteUser(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // ВИ-13: только OWNER
    @PreAuthorize("hasRole('OWNER')")
    @PatchMapping("/api/users/{id}/role")
    public ResponseEntity<UserDto> updateRole(
            @PathVariable Long id,
            @RequestBody @Valid RoleUpdateRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request.role()));
    }
}
