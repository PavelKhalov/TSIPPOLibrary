package ru.khalov.tsippolibrary.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.khalov.tsippolibrary.util.AssignStatus;

import java.time.Instant;

/**
 * Бронирование — связь пользователь ↔ книга (ТЗ 3.1.3).
 * При бронировании availableCopies книги уменьшается на 1, при возврате увеличивается.
 * Ограничения: нельзя повторно бронировать ту же книгу, лимит активных бронирований = 5.
 * Исправлено: убран uniqueConstraints на (user_id,book_id,status) — он запрещал повторное
 * бронирование той же книги после возврата (второе RETURNED падало с 500). Проверка
 * повторного ACTIVE делается в сервисе (ТЗ 3.1.3), БД-ограничение только на ACTIVE — через частичный индекс.
 */
@Entity
@Table(name = "assigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AssignStatus status = AssignStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "returned_at")
    private Instant returnedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
