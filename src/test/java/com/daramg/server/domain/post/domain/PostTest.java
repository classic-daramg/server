package com.daramg.server.domain.post.domain;

import com.daramg.server.domain.post.dto.PostCreateDto;
import com.daramg.server.domain.post.dto.PostUpdateDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Collections.emptyList;

class PostTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("제목이 빈 값이면 검증에 실패한다")
    void title_NotBlank_실패() {
        // given
        PostCreateDto.CreateFree dto = new PostCreateDto.CreateFree(
                "", // 빈 제목
                "내용입니다",
                PostStatus.PUBLISHED,
                emptyList(),
                null,
                emptyList()
        );

        // when
        Set<ConstraintViolation<PostCreateDto.CreateFree>> violations = validator.validate(dto);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("제목은 필수입니다");
    }

    @Test
    @DisplayName("제목이 15자를 초과하면 검증에 실패한다")
    void title_Size_초과_실패() {
        // given
        String longTitle = "a".repeat(16);

        PostCreateDto.CreateFree dto = new PostCreateDto.CreateFree(
                longTitle,
                "내용입니다",
                PostStatus.PUBLISHED,
                emptyList(),
                null,
                emptyList()
        );

        // when
        Set<ConstraintViolation<PostCreateDto.CreateFree>> violations = validator.validate(dto);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("제목은 15자를 초과할 수 없습니다");
    }

    @Test
    @DisplayName("내용이 빈 값이면 검증에 실패한다")
    void content_NotBlank_실패() {
        // given
        PostCreateDto.CreateFree dto = new PostCreateDto.CreateFree(
                "제목입니다",
                "", // 빈 내용
                PostStatus.PUBLISHED,
                emptyList(),
                null,
                emptyList()
        );

        // when
        Set<ConstraintViolation<PostCreateDto.CreateFree>> violations = validator.validate(dto);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("내용은 5자 이상 3000자 이하로 입력해주세요");
    }

    @Test
    @DisplayName("내용이 5자 미만이면 검증에 실패한다")
    void content_Size_미만_실패() {
        // given
        PostCreateDto.CreateFree dto = new PostCreateDto.CreateFree(
                "제목입니다",
                "내용",
                PostStatus.PUBLISHED,
                emptyList(),
                null,
                emptyList()
        );

        // when
        Set<ConstraintViolation<PostCreateDto.CreateFree>> violations = validator.validate(dto);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("내용은 5자 이상 3000자 이하로 입력해주세요");
    }

    @Test
    @DisplayName("내용이 3000자를 초과하면 검증에 실패한다")
    void content_Size_초과_실패() {
        // given
        String longContent = "a".repeat(3001); // 3001자 내용
        PostCreateDto.CreateFree dto = new PostCreateDto.CreateFree(
                "제목입니다",
                longContent,
                PostStatus.PUBLISHED,
                emptyList(),
                null,
                emptyList()
        );

        // when
        Set<ConstraintViolation<PostCreateDto.CreateFree>> violations = validator.validate(dto);

        // then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("내용은 5자 이상 3000자 이하로 입력해주세요");
    }

    @Test
    @DisplayName("모든 필수 값이 올바르게 입력되면 검증에 성공한다")
    void 모든_필수값_검증_성공() {
        // given
        PostCreateDto.CreateFree dto = new PostCreateDto.CreateFree(
                "제목입니다",
                "내용입니다",
                PostStatus.PUBLISHED,
                emptyList(),
                null,
                emptyList()
        );

        // when
        Set<ConstraintViolation<PostCreateDto.CreateFree>> violations = validator.validate(dto);

        // then
        assertThat(violations).isEmpty();
    }

    @Nested
    @DisplayName("PostUpdateDto 검증 테스트")
    class PostUpdateDtoValidationTest {

        @Test
        @DisplayName("제목이 15자를 초과하면 검증에 실패한다")
        void title_Size_초과_실패() {
            // given
            String longTitle = "a".repeat(16);

            PostUpdateDto.UpdateFree dto = new PostUpdateDto.UpdateFree(
                    longTitle,
                    "내용입니다",
                    PostStatus.PUBLISHED,
                    emptyList(),
                    null,
                    emptyList()
            );

            // when
            Set<ConstraintViolation<PostUpdateDto.UpdateFree>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("제목은 15자를 초과할 수 없습니다");
        }

        @Test
        @DisplayName("내용이 3000자를 초과하면 검증에 실패한다")
        void content_Size_초과_실패() {
            // given
            String longContent = "a".repeat(3001); // 3001자 내용
            PostUpdateDto.UpdateFree dto = new PostUpdateDto.UpdateFree(
                    "제목입니다",
                    longContent,
                    PostStatus.PUBLISHED,
                    emptyList(),
                    null,
                    emptyList()
            );

            // when
            Set<ConstraintViolation<PostUpdateDto.UpdateFree>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("내용은 5자 이상 3000자 이하로 입력해주세요");
        }

        @Test
        @DisplayName("제목과 내용이 null이면 검증에 성공한다")
        void title_content_null_성공() {
            // given
            PostUpdateDto.UpdateFree dto = new PostUpdateDto.UpdateFree(
                    null, // null 제목
                    null, // null 내용
                    PostStatus.PUBLISHED,
                    emptyList(),
                    null,
                    emptyList()
            );

            // when
            Set<ConstraintViolation<PostUpdateDto.UpdateFree>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("제목과 내용이 올바른 범위 내에서 입력되면 검증에 성공한다")
        void 모든_값_검증_성공() {
            // given
            PostUpdateDto.UpdateFree dto = new PostUpdateDto.UpdateFree(
                    "제목입니다",
                    "내용입니다",
                    PostStatus.PUBLISHED,
                    emptyList(),
                    null,
                    emptyList()
            );

            // when
            Set<ConstraintViolation<PostUpdateDto.UpdateFree>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("제목이 15자 정확히 입력되면 검증에 성공한다")
        void title_15자_정확히_성공() {
            // given
            String exactTitle = "a".repeat(15); // 정확히 15자

            PostUpdateDto.UpdateFree dto = new PostUpdateDto.UpdateFree(
                    exactTitle,
                    "내용입니다",
                    PostStatus.PUBLISHED,
                    emptyList(),
                    null,
                    emptyList()
            );

            // when
            Set<ConstraintViolation<PostUpdateDto.UpdateFree>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("내용이 3000자 정확히 입력되면 검증에 성공한다")
        void content_3000자_정확히_성공() {
            // given
            String exactContent = "a".repeat(3000); // 정확히 3000자
            PostUpdateDto.UpdateFree dto = new PostUpdateDto.UpdateFree(
                    "제목입니다",
                    exactContent,
                    PostStatus.PUBLISHED,
                    emptyList(),
                    null,
                    emptyList()
            );

            // when
            Set<ConstraintViolation<PostUpdateDto.UpdateFree>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }
    }
}
