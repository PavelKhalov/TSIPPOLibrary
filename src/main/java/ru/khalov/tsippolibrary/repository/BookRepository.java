package ru.khalov.tsippolibrary.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.khalov.tsippolibrary.entity.Book;
import ru.khalov.tsippolibrary.util.Genre;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByTitle(String title);

    void deleteByTitle(String title);

    // Пессимистическая блокировка для защиты от гонки при бронировании последнего экземпляра (ТЗ 3.1.3)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);

    // Фильтрация каталога: поиск по названию, автору, жанру, наличию (ТЗ 3.1.2)
    @Query("""
            select b from Book b
            where (:title is null or lower(b.title) like lower(concat('%', :title, '%')))
              and (:author is null or lower(b.author) like lower(concat('%', :author, '%')))
              and (:genre is null or b.genre = :genre)
              and (:availableOnly = false or b.availableCopies > 0)
            """)
    Page<Book> search(@Param("title") String title,
                      @Param("author") String author,
                      @Param("genre") Genre genre,
                      @Param("availableOnly") boolean availableOnly,
                      Pageable pageable);
}
