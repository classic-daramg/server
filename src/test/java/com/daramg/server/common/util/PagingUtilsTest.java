package com.daramg.server.common.util;

import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PagingUtilsTest {
    private PagingUtils pagingUtils;

    @BeforeEach
    void setUp() {
        pagingUtils = new PagingUtils();
    }

    private record TestEntity(Long id, String name, LocalDateTime createdAt) {}
    private record TestDto(Long id, String name) {}

    @Nested
    @DisplayName("createPageResponse (응답 DTO 생성) 테스트")
    class CreatePageResponseTest {

        @Test
        @DisplayName("N+1개 조회 성공 시 (다음 페이지 있음)")
        void createPageResponse_HasNext_True() {
            // given
            int requestSize = 10;

            List<TestEntity> content = LongStream.range(1, 12) // 11개
                    .mapToObj(i -> new TestEntity(11 - i, "Test " + i, LocalDateTime.now().minusDays(i)))
                    .toList();

            // when
            PageResponseDto<TestDto> response = pagingUtils.createPageResponse(
                    content,
                    requestSize,
                    (entity) -> new TestDto(entity.id, entity.name),
                    TestEntity::createdAt,
                    TestEntity::id
            );

            // then
            assertThat(response.getHasNext()).isTrue();
            assertThat(response.getContent()).hasSize(10); // 10개로 잘려서 반환됨
            assertThat(response.getContent().getFirst().id()).isEqualTo(10L); // 1번째 아이템 (ID=10)
            assertThat(response.getNextCursor()).isNotNull();

            // 다음 커서는 10번째(index 9) 아이템 기준으로 생성
            TestEntity lastItem = content.get(9); // 10번째 아이템
            String expectedCursor = pagingUtils.encodeCursor(lastItem.createdAt, lastItem.id);
            assertThat(response.getNextCursor()).isEqualTo(expectedCursor);
        }

        @Test
        @DisplayName("N개 이하 조회 시 (다음 페이지 없음)")
        void createPageResponse_HasNext_False() {
            // given
            int requestSize = 10;
            List<TestEntity> content = LongStream.range(1, 11) // 10개
                    .mapToObj(i -> new TestEntity(10 - i, "Test " + i, LocalDateTime.now().minusDays(i)))
                    .toList();

            // when
            PageResponseDto<TestDto> response = pagingUtils.createPageResponse(
                    content,
                    requestSize,
                    (entity) -> new TestDto(entity.id, entity.name),
                    TestEntity::createdAt,
                    TestEntity::id
            );

            // then
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getContent()).hasSize(10);
            assertThat(response.getNextCursor()).isNull(); // 다음 커서가 없음
        }

        @Test
        @DisplayName("조회 결과가 비어있을 때")
        void createPageResponse_Empty() {
            // given
            int requestSize = 10;
            List<TestEntity> content = new ArrayList<>();

            // when
            PageResponseDto<TestDto> response = pagingUtils.createPageResponse(
                    content,
                    requestSize,
                    (entity) -> new TestDto(entity.id, entity.name),
                    TestEntity::createdAt,
                    TestEntity::id
            );

            // then
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getNextCursor()).isNull();
        }
    }

    @Nested
    @DisplayName("커서 인코딩/디코딩 테스트")
    class CursorEncodingTest {

        @Test
        @DisplayName("커서 인코딩 및 디코딩 라운드트립")
        void cursor_EncodeDecode_Roundtrip() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Long id = 123L;

            // when
            String encodedCursor = pagingUtils.encodeCursor(now, id);

            Object decodedRecord = ReflectionTestUtils.invokeMethod(
                    pagingUtils, "decodeCursor", encodedCursor
            );

            // then
            assertThat(encodedCursor).isNotNull().isNotEqualTo(now.toString() + "_" + id); // Base64 인코딩
            assertThat(decodedRecord).isNotNull();

            LocalDateTime decodedTime = (LocalDateTime) ReflectionTestUtils.getField(decodedRecord, "createdAt");
            Long decodedId = (Long) ReflectionTestUtils.getField(decodedRecord, "id");

            assertThat(decodedTime).isEqualTo(now);
            assertThat(decodedId).isEqualTo(id);
        }

        @Test
        @DisplayName("null 커서 디코딩 시 null 반환")
        void decodeCursor_NullInput_ReturnsNull() {
            // when
            Object decodedRecord = ReflectionTestUtils.invokeMethod(
                    pagingUtils, "decodeCursor", (String) null
            );
            // then
            assertThat(decodedRecord).isNull();
        }

        @Test
        @DisplayName("유효하지 않은(Malformed) 커서 디코딩 시 BusinessException 발생")
        void decodeCursor_InvalidFormat_ThrowsException() {
            // given
            String invalidCursor = "not-base-64-string"; // Base64가 아니거나 포맷이 깨진 문자열

            // when & then
            assertThatThrownBy(() -> {
                ReflectionTestUtils.invokeMethod(pagingUtils, "decodeCursor", invalidCursor);
            }).isInstanceOf(BusinessException.class)
                    .hasMessage("유효하지 않은 커서 포맷입니다.");
        }
    }
}
