package com.daramg.server.composer.dto;

import com.daramg.server.common.dto.PageResponseDto;
import com.daramg.server.post.dto.PostResponseDto;

public record ComposerWithPostsResponseDto(
        ComposerResponseDto composer,
        PageResponseDto<PostResponseDto> posts
) {
}
