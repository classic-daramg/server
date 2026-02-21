package com.daramg.server.common.util;

import com.daramg.server.common.dto.PageRequestDto;
import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.common.exception.CommonErrorStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

@Component
public class PagingUtils {

    private static final String CURSOR_DELIMITER = "_";

    public <T> List<T> applyCursorPagination(
            JPAQuery<T> query,
            PageRequestDto request,
            DateTimePath<LocalDateTime> createdAtPath,
            NumberPath<Long> idPath
    ) {
        int querySize = request.getValidatedSize() + 1;

        Cursor cursor = decodeCursor(request.getCursor());
        return query
                .where(getPaginationConditions(cursor, createdAtPath, idPath))
                .orderBy(createdAtPath.desc(), idPath.desc())
                .limit(querySize)
                .fetch();
    }

    public <T, D> PageResponseDto<D> createPageResponse(
            List<T> content,
            int size,
            Function<T, D> dtoMapper,
            Function<T, LocalDateTime> createdAtExtractor,
            Function<T, Long> idExtractor
    ) {
        boolean hasNext = content.size() > size;
        List<D> dtoList = content.stream()
                .limit(size)
                .map(dtoMapper)
                .toList();

        String nextCursor = null;
        if (hasNext) {
            T lastItem = content.get(size - 1);
            nextCursor = encodeCursor(
                    createdAtExtractor.apply(lastItem),
                    idExtractor.apply(lastItem)
            );
        }

        return new PageResponseDto<>(dtoList, nextCursor, hasNext);
    }

    public <T> List<T> applyCursorPaginationToList(
            List<T> content,
            PageRequestDto request,
            Function<T, LocalDateTime> createdAtExtractor,
            Function<T, Long> idExtractor
    ) {
        Cursor cursor = decodeCursor(request.getCursor());
        int size = request.getValidatedSize() + 1;

        List<T> filtered = content.stream()
                .filter(item -> {
                    if (cursor == null) {
                        return true;
                    }
                    LocalDateTime createdAt = createdAtExtractor.apply(item);
                    Long id = idExtractor.apply(item);
                    return createdAt.isBefore(cursor.createdAt())
                            || (createdAt.equals(cursor.createdAt()) && id < cursor.id());
                })
                .limit(size)
                .toList();

        return filtered;
    }

    private BooleanExpression getPaginationConditions(
            Cursor cursor,
            DateTimePath<LocalDateTime> createdAtPath,
            NumberPath<Long> idPath
    ) {
        if (cursor == null) {
            return null;
        }

        return createdAtPath.lt(cursor.createdAt())
                .or(createdAtPath.eq(cursor.createdAt())
                        .and(idPath.lt(cursor.id())));
    }

    public String encodeCursor(LocalDateTime createdAt, Long id) {
        if (createdAt == null || id == null) return null;
        String cursorStr = createdAt.toString() + CURSOR_DELIMITER + id;
        return Base64.getEncoder().encodeToString(cursorStr.getBytes());
    }

    private Cursor decodeCursor(String cursorString) {
        if (cursorString == null || cursorString.isEmpty()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(cursorString));
            String[] parts = decoded.split(CURSOR_DELIMITER);
            return new Cursor(LocalDateTime.parse(parts[0]), Long.parseLong(parts[1]));
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException |
                 DateTimeParseException e) {
            throw new BusinessException(CommonErrorStatus.INVALID_CURSOR);
        }
    }

    private record Cursor(LocalDateTime createdAt, Long id) {}
}
