package com.daramg.server.notice.presentation;

import com.daramg.server.notice.application.NoticeService;
import com.daramg.server.notice.dto.NoticeCreateDto;
import com.daramg.server.notice.dto.NoticeUpdateDto;
import com.daramg.server.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RestController
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping("/notice")
    @ResponseStatus(HttpStatus.CREATED)
    public void createNotice(@Valid @RequestBody NoticeCreateDto request, User user) {
        noticeService.create(request, user);
    }

    @PatchMapping("/notice/{noticeId}")
    @ResponseStatus(HttpStatus.OK)
    public void updateNotice(@PathVariable Long noticeId,
                             @Valid @RequestBody NoticeUpdateDto updateRequest, User user) {
        noticeService.updateNotice(noticeId, updateRequest, user);
    }

    @DeleteMapping("/notice/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotice(@PathVariable Long noticeId, User user) {
        noticeService.delete(noticeId, user);
    }
}
