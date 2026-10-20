package ru.khalov.tsippolibrary.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.khalov.tsippolibrary.dto.response.LibraryStatsResponse;
import ru.khalov.tsippolibrary.service.LibraryStatsService;

/**
 * Управление библиотекой — только OWNER (ТЗ 3.1.5)
 * GET /api/owner/stats — сводная статистика
 */
@Tag(name = "Owner")
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerController {

    private final LibraryStatsService statsService;

    @GetMapping("/stats")
    public ResponseEntity<LibraryStatsResponse> stats() {
        return ResponseEntity.ok(statsService.getStats());
    }
}
