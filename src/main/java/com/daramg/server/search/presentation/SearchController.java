package com.daramg.server.search.presentation;

import com.daramg.server.search.application.SearchService;
import com.daramg.server.search.dto.SearchResponseDto;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public SearchResponseDto search(
            @RequestParam String keyword,
            @AuthenticationPrincipal User user
    ) {
        return searchService.search(keyword, user);
    }
}
