package ru.khalov.tsippolibrary.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.khalov.tsippolibrary.entity.Assign;
import ru.khalov.tsippolibrary.util.AssignStatus;

@Repository
public interface AssignRepository extends JpaRepository<Assign, Long> {

    // Активные бронирования пользователя (ТЗ 3.1.3: просмотр своих бронирований, лимит 5)
    long countByUserIdAndStatus(Long userId, AssignStatus status);

    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, AssignStatus status);

    Page<Assign> findByUserId(Long userId, Pageable pageable);

    // Для администратора: все бронирования с фильтрами по пользователю/книге/статусу (ТЗ 3.1.3, ВИ-4)
    @Query("""
            select a from Assign a
            where (:userId is null or a.user.id = :userId)
              and (:bookId is null or a.book.id = :bookId)
              and (:status is null or a.status = :status)
            """)
    Page<Assign> searchAll(@Param("userId") Long userId,
                           @Param("bookId") Long bookId,
                           @Param("status") AssignStatus status,
                           Pageable pageable);

    // Статистика (ТЗ 3.1.5)
    long countByStatus(AssignStatus status);

    // Исправлено: быстрая проверка наличия активных бронирований книги (для удаления книги ТЗ 3.1.2)
    boolean existsByBookIdAndStatus(Long bookId, AssignStatus status);

    long countByBookIdAndStatus(Long bookId, AssignStatus status);

    // Топ книг по бронированиям (ТЗ 3.1.5, ВИ-15) — один JPQL запрос, без загрузки всей таблицы в память
    @Query("""
            select a.book.id as bookId, a.book.title as title, count(a) as cnt
            from Assign a group by a.book.id, a.book.title order by count(a) desc
            """)
    java.util.List<Object[]> topBooksRaw(org.springframework.data.domain.Pageable pageable);
}
