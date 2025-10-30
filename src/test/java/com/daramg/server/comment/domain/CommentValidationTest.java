package com.daramg.server.comment.domain;

import com.daramg.server.post.dto.CommentCreateDto;
import com.daramg.server.post.dto.CommentReplyCreateDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CommentValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Nested
    @DisplayName("CommentCreateDto 검증 테스트")
    class CommentCreateDtoValidationTest {

        @Test
        @DisplayName("내용이 빈 값이면 검증에 실패한다")
        void content_NotBlank_실패() {
            // given
            CommentCreateDto dto = new CommentCreateDto("");

            // when
            Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("댓글 내용은 비어있을 수 없습니다.");
        }

        @Test
        @DisplayName("내용이 500자를 초과하면 검증에 실패한다")
        void content_Size_초과_실패() {
            // given
            String longContent = "a".repeat(501);
            CommentCreateDto dto = new CommentCreateDto(longContent);

            // when
            Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("댓글은 500자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("내용이 500자 정확히 입력되면 검증에 성공한다")
        void content_500자_정확히_성공() {
            // given
            String exactContent = "a".repeat(500);
            CommentCreateDto dto = new CommentCreateDto(exactContent);

            // when
            Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("내용이 올바르게 입력되면 검증에 성공한다")
        void content_정상_성공() {
            // given
            CommentCreateDto dto = new CommentCreateDto("정상 댓글 내용");

            // when
            Set<ConstraintViolation<CommentCreateDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("CommentReplyCreateDto 검증 테스트")
    class CommentReplyCreateDtoValidationTest {

        @Test
        @DisplayName("내용이 빈 값이면 검증에 실패한다")
        void reply_content_NotBlank_실패() {
            // given
            CommentReplyCreateDto dto = new CommentReplyCreateDto("");

            // when
            Set<ConstraintViolation<CommentReplyCreateDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("대댓글 내용은 비어있을 수 없습니다.");
        }

        @Test
        @DisplayName("내용이 500자를 초과하면 검증에 실패한다")
        void reply_content_Size_초과_실패() {
            // given
            String longContent = "a".repeat(501);
            CommentReplyCreateDto dto = new CommentReplyCreateDto(longContent);

            // when
            Set<ConstraintViolation<CommentReplyCreateDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("대댓글은 500자를 초과할 수 없습니다.");
        }

        @Test
        @DisplayName("내용이 500자 정확히 입력되면 검증에 성공한다")
        void reply_content_500자_정확히_성공() {
            // given
            String exactContent = "a".repeat(500);
            CommentReplyCreateDto dto = new CommentReplyCreateDto(exactContent);

            // when
            Set<ConstraintViolation<CommentReplyCreateDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("내용이 올바르게 입력되면 검증에 성공한다")
        void reply_content_정상_성공() {
            // given
            CommentReplyCreateDto dto = new CommentReplyCreateDto("정상 대댓글 내용");

            // when
            Set<ConstraintViolation<CommentReplyCreateDto>> violations = validator.validate(dto);

            // then
            assertThat(violations).isEmpty();
        }
    }
}


