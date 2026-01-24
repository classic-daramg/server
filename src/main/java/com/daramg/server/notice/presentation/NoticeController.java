package com.daramg.server.notice.presentation;

import com.daramg.server.notice.application.NoticeService;
import com.daramg.server.notice.dto.NoticeCreateDto;
import com.daramg.server.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}
