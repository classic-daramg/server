package com.daramg.server.common.application;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.common.exception.ImageErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageService {

    private static final java.util.Set<String> ALLOWED_CONTENT_TYPES = java.util.Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String S3_URL_FORMAT = "https://%s.s3.%s.amazonaws.com/%s";

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.region.static:ap-northeast-2}")
    private String region;

    public List<String> uploadImages(List<MultipartFile> images) {
        return images.stream()
                .map(this::uploadImage)
                .toList();
    }

    public String uploadImage(MultipartFile file) {
        validateFile(file);

        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String key = "images/" + fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            try (InputStream inputStream = file.getInputStream()) {
                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
            }

            String imageUrl = String.format(S3_URL_FORMAT, bucketName, region, key);
            log.info("Image uploaded successfully: {}", imageUrl);
            return imageUrl;

        } catch (S3Exception e) {
            log.error("Failed to upload image to S3", e);
            throw new BusinessException(ImageErrorStatus.FILE_UPLOAD_FAILED);
        } catch (IOException e) {
            log.error("Failed to read file input stream", e);
            throw new BusinessException(ImageErrorStatus.FILE_UPLOAD_FAILED);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ImageErrorStatus.EMPTY_FILE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ImageErrorStatus.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ImageErrorStatus.INVALID_FILE_TYPE);
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }
}
