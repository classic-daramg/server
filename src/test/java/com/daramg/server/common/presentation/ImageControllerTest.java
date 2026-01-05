package com.daramg.server.common.presentation;

import com.daramg.server.common.application.S3ImageService;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ImageController.class)
public class ImageControllerTest extends ControllerTestSupport {

    @MockitoBean
    private S3ImageService s3ImageService;

    @Test
    void 이미지를_업로드한다() throws Exception {
        // given
        MockMultipartFile imageFile1 = new MockMultipartFile(
                "images",
                "test-image1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 1".getBytes()
        );
        MockMultipartFile imageFile2 = new MockMultipartFile(
                "images",
                "test-image2.png",
                MediaType.IMAGE_PNG_VALUE,
                "test image content 2".getBytes()
        );

        List<String> expectedImageUrls = Arrays.asList(
                "https://bucket-name.s3.ap-northeast-2.amazonaws.com/images/uuid1.jpg",
                "https://bucket-name.s3.ap-northeast-2.amazonaws.com/images/uuid2.png"
        );
        when(s3ImageService.uploadImages(any())).thenReturn(expectedImageUrls);

        // when
        ResultActions result = mockMvc.perform(
                multipart("/images/upload")
                        .file(imageFile1)
                        .file(imageFile2)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageUrls[0]").value(expectedImageUrls.get(0)))
                .andExpect(jsonPath("$.imageUrls[1]").value(expectedImageUrls.get(1)))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Image API")
                                .summary("이미지 업로드")
                                .description("여러 이미지 파일을 S3에 업로드하고 영구적인 URL 목록을 반환합니다. " +
                                        "업로드된 이미지는 UUID 기반의 고유한 파일명으로 저장되며, " +
                                        "반환된 URL을 통해 영구적으로 접근할 수 있습니다.")
                                .responseFields(
                                        fieldWithPath("imageUrls").type(JsonFieldType.ARRAY)
                                                .description("업로드된 이미지들의 S3 URL 목록 (형식: https://{bucket}.s3.{region}.amazonaws.com/images/{uuid}.{extension})")
                                )
                                .build()
                        ),
                        requestParts(
                                partWithName("images").description("업로드할 이미지 파일들 (여러 개 가능)\n\n" +
                                        "**제약조건:**\n" +
                                        "- 지원 형식: JPEG, JPG, PNG, GIF (Content-Type: image/jpeg, image/jpg, image/png, image/gif)\n" +
                                        "- 최대 파일 크기: 파일당 10MB\n" +
                                        "- 최대 요청 크기: 50MB (여러 파일 합계)\n" +
                                        "- 빈 파일 리스트는 업로드할 수 없습니다\n\n" +
                                        "**참고:**\n" +
                                        "- 파일명은 UUID로 자동 생성되어 원본 파일명과 무관하게 저장됩니다\n" +
                                        "- 업로드된 이미지는 `images/` 디렉토리 하위에 저장됩니다")
                        )
                ));
    }
}
