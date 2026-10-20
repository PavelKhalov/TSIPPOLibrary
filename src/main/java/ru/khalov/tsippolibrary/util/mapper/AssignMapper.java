package ru.khalov.tsippolibrary.util.mapper;

import org.springframework.stereotype.Component;
import ru.khalov.tsippolibrary.dto.response.AssignResponse;
import ru.khalov.tsippolibrary.entity.Assign;

@Component
public class AssignMapper {

    public AssignResponse toDto(Assign a) {
        return new AssignResponse(
                a.getId(),
                a.getUser().getId(),
                a.getUser().getDisplayName(),
                a.getBook().getId(),
                a.getBook().getTitle(),
                a.getStatus(),
                a.getCreatedAt(),
                a.getReturnedAt()
        );
    }
}
