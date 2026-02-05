package com.daramg.server.notice.application;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.common.exception.NotFoundException;
import com.daramg.server.notice.domain.Notice;
import com.daramg.server.notice.dto.NoticeCreateDto;
import com.daramg.server.notice.dto.NoticeUpdateDto;
import com.daramg.server.notice.repository.NoticeRepository;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import com.daramg.server.user.domain.User;
import com.daramg.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class NoticeServiceTest extends ServiceTestSupport {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("admin@test.com", "password", "관리자", LocalDate.now(), "profile", "관리자닉네임", "bio", null);
        userRepository.save(user);
    }

    @Nested
    @DisplayName("공지사항 생성")
    class CreateNotice {

        @Test
        void 공지사항을_정상적으로_생성한다() {
            // given
            NoticeCreateDto request = new NoticeCreateDto(
                    "공지사항 제목",
                    "공지사항 내용입니다.",
                    List.of("https://example.com/image.jpg")
            );

            // when
            noticeService.create(request, user);

            // then
            List<Notice> all = noticeRepository.findAll();
            assertThat(all).hasSize(1);
            Notice saved = all.getFirst();
            assertThat(saved.getTitle()).isEqualTo("공지사항 제목");
            assertThat(saved.getContent()).isEqualTo("공지사항 내용입니다.");
            assertThat(saved.getImages()).containsExactly("https://example.com/image.jpg");
            assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        }

        @Test
        void 이미지_없이_공지사항을_생성한다() {
            // given
            NoticeCreateDto request = new NoticeCreateDto(
                    "제목",
                    "내용입니다.",
                    List.of()
            );

            // when
            noticeService.create(request, user);

            // then
            List<Notice> all = noticeRepository.findAll();
            assertThat(all).hasSize(1);
            Notice saved = all.getFirst();
            assertThat(saved.getImages()).isEmpty();
        }
    }

    @Nested
    @DisplayName("공지사항 수정")
    class UpdateNotice {

        @Test
        void 공지사항을_정상적으로_수정한다() {
            // given
            NoticeCreateDto createDto = new NoticeCreateDto("원래 제목", "원래 내용입니다.", List.of());
            noticeService.create(createDto, user);
            Notice notice = noticeRepository.findAll().getFirst();

            NoticeUpdateDto updateDto = new NoticeUpdateDto("수정된 제목", "수정된 내용입니다.", List.of("https://new-image.jpg"));

            // when
            noticeService.updateNotice(notice.getId(), updateDto, user);

            // then
            Notice updated = noticeRepository.findById(notice.getId()).orElseThrow();
            assertThat(updated.getTitle()).isEqualTo("수정된 제목");
            assertThat(updated.getContent()).isEqualTo("수정된 내용입니다.");
            assertThat(updated.getImages()).containsExactly("https://new-image.jpg");
        }

        @Test
        void 제목만_수정할_수_있다() {
            // given
            NoticeCreateDto createDto = new NoticeCreateDto("원래 제목", "원래 내용입니다.", List.of());
            noticeService.create(createDto, user);
            Notice notice = noticeRepository.findAll().getFirst();

            NoticeUpdateDto updateDto = new NoticeUpdateDto("수정된 제목", null, null);

            // when
            noticeService.updateNotice(notice.getId(), updateDto, user);

            // then
            Notice updated = noticeRepository.findById(notice.getId()).orElseThrow();
            assertThat(updated.getTitle()).isEqualTo("수정된 제목");
            assertThat(updated.getContent()).isEqualTo("원래 내용입니다.");
        }

        @Test
        void 존재하지_않는_공지사항은_수정할_수_없다() {
            // given
            Long nonExistentId = 9999L;
            NoticeUpdateDto updateDto = new NoticeUpdateDto("제목", null, null);

            // when & then
            assertThatThrownBy(() -> noticeService.updateNotice(nonExistentId, updateDto, user))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void 작성자가_아니면_공지사항을_수정할_수_없다() {
            // given
            NoticeCreateDto createDto = new NoticeCreateDto("제목", "내용입니다.", List.of());
            noticeService.create(createDto, user);
            Notice notice = noticeRepository.findAll().getFirst();

            User anotherUser = new User("another@test.com", "password", "다른사용자", LocalDate.now(), "profile", "다른닉네임", "bio", null);
            userRepository.save(anotherUser);

            NoticeUpdateDto updateDto = new NoticeUpdateDto("수정 시도", null, null);

            // when & then
            assertThatThrownBy(() -> noticeService.updateNotice(notice.getId(), updateDto, anotherUser))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("공지사항의 작성자가 일치하지 않습니다.");
        }

        @Test
        void 수정_사항이_없으면_예외가_발생한다() {
            // given
            NoticeCreateDto createDto = new NoticeCreateDto("제목", "내용입니다.", List.of());
            noticeService.create(createDto, user);
            Notice notice = noticeRepository.findAll().getFirst();

            NoticeUpdateDto updateDto = new NoticeUpdateDto(null, null, null);

            // when & then
            assertThatThrownBy(() -> noticeService.updateNotice(notice.getId(), updateDto, user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("수정 사항이 없습니다.");
        }
    }

    @Nested
    @DisplayName("공지사항 삭제")
    class DeleteNotice {

        @Test
        void 공지사항을_정상적으로_삭제한다() {
            // given
            NoticeCreateDto createDto = new NoticeCreateDto("삭제할 공지", "삭제될 내용입니다.", List.of());
            noticeService.create(createDto, user);
            Notice notice = noticeRepository.findAll().getFirst();

            // when
            noticeService.delete(notice.getId(), user);

            // then
            Notice deleted = noticeRepository.findById(notice.getId()).orElse(null);
            // SQLRestriction으로 인해 soft deleted 공지는 조회되지 않음
            assertThat(deleted).isNull();
        }

        @Test
        @Transactional
        void 삭제된_공지사항은_isDeleted가_true이다() {
            // given
            NoticeCreateDto createDto = new NoticeCreateDto("삭제할 공지", "삭제될 내용입니다.", List.of());
            noticeService.create(createDto, user);
            Notice notice = noticeRepository.findAll().getFirst();
            Long noticeId = notice.getId();

            // when
            noticeService.delete(noticeId, user);

            // then - native query로 확인
            Boolean isDeleted = (Boolean) ReflectionTestUtils.getField(notice, "isDeleted");
            assertThat(isDeleted).isTrue();
        }

        @Test
        void 존재하지_않는_공지사항은_삭제할_수_없다() {
            // given
            Long nonExistentId = 9999L;

            // when & then
            assertThatThrownBy(() -> noticeService.delete(nonExistentId, user))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void 작성자가_아니면_공지사항을_삭제할_수_없다() {
            // given
            NoticeCreateDto createDto = new NoticeCreateDto("삭제할 공지", "삭제될 내용입니다.", List.of());
            noticeService.create(createDto, user);
            Notice notice = noticeRepository.findAll().getFirst();

            User anotherUser = new User("another@test.com", "password", "다른사용자", LocalDate.now(), "profile", "다른닉네임", "bio", null);
            userRepository.save(anotherUser);

            // when & then
            assertThatThrownBy(() -> noticeService.delete(notice.getId(), anotherUser))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("공지사항의 작성자가 일치하지 않습니다.");
        }
    }
}
