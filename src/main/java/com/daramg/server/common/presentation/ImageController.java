package com.daramg.server.common.presentation;

import com.daramg.server.common.application.S3ImageService;
import com.daramg.server.common.dto.ImageUploadResponseDto;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.common.exception.ImageErrorStatus;
import com.daramg.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {

    private final S3ImageService s3ImageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImageUploadResponseDto uploadImage(
            @RequestPart("images") List<MultipartFile> images,
            User user
    ) {
        log.info("이미지 업로드 요청 - User ID: {}, 이미지 개수: {}", user.getId(), images != null ? images.size() : 0);
        
        // 이미지가 비어있는지 체크
        if (images == null || images.isEmpty()) {
            throw new BusinessException(ImageErrorStatus.EMPTY_FILE);
        }
        
        List<String> imageUrls = s3ImageService.uploadImages(images);
        return new ImageUploadResponseDto(imageUrls);
    }
}
