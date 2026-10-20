package ru.khalov.tsippolibrary.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import ru.khalov.tsippolibrary.util.Genre;

import java.time.Instant;

/**
 * Книга — основная сущность каталога (ТЗ 3.3.2, 3.1.2).
 * Хранит общее и доступное количество экземпляров.
 * Доступное количество уменьшается при бронировании и увеличивается при возврате.
 */
@Entity
@Table(name = "books")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author", nullable = false)
    private String author;

    // Жанр — один на книгу (ТЗ 3.1.2)
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    private Genre genre;

    @Column(name = "year_of_release", nullable = false)
    private int yearOfRelease;

    // Общее количество экземпляров
    @Column(name = "total_copies", nullable = false)
    @Min(1)
    @Builder.Default
    private int totalCopies = 1;

    // Доступно для бронирования (0..totalCopies)
    @Column(name = "available_copies", nullable = false)
    @Builder.Default
    private int availableCopies = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Убран @Version - используется пессимистическая блокировка findByIdForUpdate (ТЗ 3.1.3), смешение ломало старт
    // @Version оставлен только если нужна оптимистика, но сейчас гонка решается PESSIMISTIC_WRITE

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        // Если available не задан — синхронизируем с total
        if (this.availableCopies == 0 && this.totalCopies > 0) {
            this.availableCopies = this.totalCopies;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
