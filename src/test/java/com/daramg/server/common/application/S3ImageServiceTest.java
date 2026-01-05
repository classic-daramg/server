package com.daramg.server.common.application;

import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.common.exception.ImageErrorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3ImageService 테스트")
public class S3ImageServiceTest  {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3ImageService s3ImageService;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String REGION = "ap-northeast-2";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3ImageService, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(s3ImageService, "region", REGION);
    }

    @Nested
    @DisplayName("이미지 업로드 성공")
    class UploadImageSuccess {
        @Test
        void JPEG_이미지를_업로드한다() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // when
            String imageUrl = s3ImageService.uploadImage(file);

            // then
            assertThat(imageUrl).isNotNull();
            assertThat(imageUrl).startsWith("https://" + BUCKET_NAME + ".s3." + REGION + ".amazonaws.com/images/");
            assertThat(imageUrl).endsWith(".jpg");
            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        void PNG_이미지를_업로드한다() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "image",
                    "test.png",
                    "image/png",
                    "test image content".getBytes()
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // when
            String imageUrl = s3ImageService.uploadImage(file);

            // then
            assertThat(imageUrl).isNotNull();
            assertThat(imageUrl).endsWith(".png");
            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        void GIF_이미지를_업로드한다() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "image",
                    "test.gif",
                    "image/gif",
                    "test image content".getBytes()
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // when
            String imageUrl = s3ImageService.uploadImage(file);

            // then
            assertThat(imageUrl).isNotNull();
            assertThat(imageUrl).endsWith(".gif");
            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }
    }

    @Nested
    @DisplayName("파일 검증 실패")
    class FileValidationFail {
        @Test
        void 빈_파일은_업로드할_수_없다() {
            // given
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    "image/jpeg",
                    new byte[0]
            );

            // when & then
            assertThatThrownBy(() -> s3ImageService.uploadImage(emptyFile))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .extracting("code")
                    .isEqualTo(ImageErrorStatus.EMPTY_FILE.getCode());
        }

        @Test
        void null_파일은_업로드할_수_없다() {
            // when & then
            assertThatThrownBy(() -> s3ImageService.uploadImage(null))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .extracting("code")
                    .isEqualTo(ImageErrorStatus.EMPTY_FILE.getCode());
        }

        @Test
        void 파일_크기가_10MB를_초과하면_업로드할_수_없다() {
            // given
            byte[] largeContent = new byte[10 * 1024 * 1024 + 1]; // 10MB + 1 byte
            MockMultipartFile largeFile = new MockMultipartFile(
                    "image",
                    "large.jpg",
                    "image/jpeg",
                    largeContent
            );

            // when & then
            assertThatThrownBy(() -> s3ImageService.uploadImage(largeFile))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .extracting("code")
                    .isEqualTo(ImageErrorStatus.FILE_TOO_LARGE.getCode());
        }

        @Test
        void 허용되지_않은_파일_형식은_업로드할_수_없다() {
            // given
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "image",
                    "test.pdf",
                    "application/pdf",
                    "test content".getBytes()
            );

            // when & then
            assertThatThrownBy(() -> s3ImageService.uploadImage(invalidFile))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .extracting("code")
                    .isEqualTo(ImageErrorStatus.INVALID_FILE_TYPE.getCode());
        }

        @Test
        void ContentType이_null이면_업로드할_수_없다() {
            // given
            MockMultipartFile fileWithoutContentType = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    null,
                    "test content".getBytes()
            );

            // when & then
            assertThatThrownBy(() -> s3ImageService.uploadImage(fileWithoutContentType))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .extracting("code")
                    .isEqualTo(ImageErrorStatus.INVALID_FILE_TYPE.getCode());
        }
    }

    @Nested
    @DisplayName("S3 업로드 실패")
    class S3UploadFail {
        @Test
        void S3_업로드_실패시_예외를_던진다() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(software.amazon.awssdk.services.s3.model.S3Exception.builder()
                            .message("S3 upload failed")
                            .build());

            // when & then
            assertThatThrownBy(() -> s3ImageService.uploadImage(file))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .extracting("code")
                    .isEqualTo(ImageErrorStatus.FILE_UPLOAD_FAILED.getCode());
        }

        @Test
        void IOException_발생시_예외를_던진다() throws Exception {
            // given
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getSize()).thenReturn(1024L);
            when(file.getContentType()).thenReturn("image/jpeg");
            when(file.getOriginalFilename()).thenReturn("test.jpg");
            when(file.getInputStream()).thenThrow(new IOException("IO error"));

            // when & then
            assertThatThrownBy(() -> s3ImageService.uploadImage(file))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .extracting("code")
                    .isEqualTo(ImageErrorStatus.FILE_UPLOAD_FAILED.getCode());
        }
    }

    @Nested
    @DisplayName("여러 이미지 업로드")
    class UploadImagesSuccess {
        @Test
        void 여러_이미지를_업로드한다() throws Exception {
            // given
            MockMultipartFile file1 = new MockMultipartFile(
                    "images",
                    "test1.jpg",
                    "image/jpeg",
                    "test image content 1".getBytes()
            );
            MockMultipartFile file2 = new MockMultipartFile(
                    "images",
                    "test2.png",
                    "image/png",
                    "test image content 2".getBytes()
            );
            MockMultipartFile file3 = new MockMultipartFile(
                    "images",
                    "test3.gif",
                    "image/gif",
                    "test image content 3".getBytes()
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // when
            List<String> imageUrls = s3ImageService.uploadImages(Arrays.asList(file1, file2, file3));

            // then
            assertThat(imageUrls).hasSize(3);
            assertThat(imageUrls.get(0)).startsWith("https://" + BUCKET_NAME + ".s3." + REGION + ".amazonaws.com/images/");
            assertThat(imageUrls.get(0)).endsWith(".jpg");
            assertThat(imageUrls.get(1)).endsWith(".png");
            assertThat(imageUrls.get(2)).endsWith(".gif");
            verify(s3Client, times(3)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        void 단일_이미지도_업로드할_수_있다() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "images",
                    "test.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            // when
            List<String> imageUrls = s3ImageService.uploadImages(Arrays.asList(file));

            // then
            assertThat(imageUrls).hasSize(1);
            assertThat(imageUrls.get(0)).startsWith("https://" + BUCKET_NAME + ".s3." + REGION + ".amazonaws.com/images/");
            assertThat(imageUrls.get(0)).endsWith(".jpg");
            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }
    }
}

