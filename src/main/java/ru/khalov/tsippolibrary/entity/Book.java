package ru.khalov.tsippolibrary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OptimisticLock;
import ru.khalov.tsippolibrary.util.Genre;

import java.time.Instant;

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

    @Column(name = "title", unique = true, nullable = false)
    private String title;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "genre", nullable = false)
    @CollectionTable(name = "genres", joinColumns = @JoinColumn(name = "genre_id", referencedColumnName = "id"))
    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Column(name = "year_of_release", nullable = false)
    private int yearOfRelease;

    @Column(name = "is_reserved")
    private boolean isReserved = false;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private User owner;

    @Column(name = "created_at")
    private Instant createdAt;

    @Version
    @OptimisticLock(excluded = true)
    private Long version;

    @PrePersist
    public void prePersist(){
        this.createdAt = Instant.now();
    }
}
