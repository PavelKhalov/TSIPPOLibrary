package ru.khalov.tsippolibrary.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.khalov.tsippolibrary.dto.request.AssignRequest;
import ru.khalov.tsippolibrary.dto.response.AssignResponse;
import ru.khalov.tsippolibrary.entity.User;
import ru.khalov.tsippolibrary.service.AssignService;
import ru.khalov.tsippolibrary.util.AssignStatus;

/**
 * Бронирования — ТЗ 3.2.7, ВИ-3, ВИ-3-alt, ВИ-4
 * POST /api/assign                  — забронировать (любой авторизованный)
 * POST /api/assigns/{id}/release   — вернуть своё бронирование
 * GET  /api/assigns                — мои бронирования
 * GET  /api/assigns/all            — все бронирования (ADMIN/OWNER) с фильтрами
 */
@Tag(name = "Assigns")
@RestController
@RequiredArgsConstructor
public class AssignController {

    private final AssignService assignService;

    // ВИ-3: бронирование
    @PostMapping("/api/assign")
    public ResponseEntity<AssignResponse> assign(
            @RequestBody @Valid AssignRequest request,
            @AuthenticationPrincipal User user) {
        AssignResponse resp = assignService.assign(request.bookId(), user);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    // ВИ-3-alt: возврат
    @PostMapping("/api/assigns/{id}/release")
    public ResponseEntity<AssignResponse> release(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(assignService.release(id, user));
    }

    // Свои бронирования
    @GetMapping("/api/assigns")
    public ResponseEntity<Page<AssignResponse>> myAssigns(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(assignService.myAssigns(user.getId(), pageable));
    }

    // ВИ-4: все бронирования — только ADMIN/OWNER
    @PreAuthorize("hasAnyRole('ADMIN','OWNER')")
    @GetMapping("/api/assigns/all")
    public ResponseEntity<Page<AssignResponse>> allAssigns(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) AssignStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(assignService.allAssigns(userId, bookId, status, pageable));
    }
}
