package ru.khalov.tsippolibrary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.khalov.tsippolibrary.dto.response.LibraryStatsResponse;
import ru.khalov.tsippolibrary.repository.AssignRepository;
import ru.khalov.tsippolibrary.repository.BookRepository;
import ru.khalov.tsippolibrary.repository.UserRepository;
import ru.khalov.tsippolibrary.util.AssignStatus;

import java.util.List;

/** Статистика — только OWNER (ТЗ 3.1.5) */
@Service
@RequiredArgsConstructor
public class LibraryStatsService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final AssignRepository assignRepository;

    public LibraryStatsResponse getStats() {
        long totalBooks = bookRepository.count();
        long totalUsers = userRepository.count();
        long activeAssigns = assignRepository.countByStatus(AssignStatus.ACTIVE);

        // Исправлено: один JPQL запрос вместо findAll().stream() (ТЗ 3.4.4 ≤500мс, не грузим всю таблицу)
        List<LibraryStatsResponse.TopBook> top = assignRepository.topBooksRaw(PageRequest.of(0, 5)).stream()
                .map(row -> new LibraryStatsResponse.TopBook((Long) row[0], (String) row[1], (Long) row[2]))
                .toList();

        return new LibraryStatsResponse(totalBooks, totalUsers, activeAssigns, top);
    }
}
