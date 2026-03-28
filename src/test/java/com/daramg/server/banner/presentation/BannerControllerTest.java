package com.daramg.server.banner.presentation;

import com.daramg.server.banner.application.BannerService;
import com.daramg.server.banner.dto.BannerResponseDto;
import com.daramg.server.banner.dto.BannerUpdateRequestDto;
import com.daramg.server.testsupport.support.ControllerTestSupport;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;
import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BannerController.class)
public class BannerControllerTest extends ControllerTestSupport {

    @MockitoBean
    private BannerService bannerService;

    private BannerResponseDto sampleBanner(Long id) {
        return new BannerResponseDto(id, "https://s3.example.com/banner.jpg", "https://example.com", true, 0, Instant.now(), Instant.now());
    }

    @Test
    void 배너_목록을_조회한다() throws Exception {
        // given
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");
        when(bannerService.getBanners()).thenReturn(List.of(sampleBanner(1L), sampleBanner(2L)));

        // when
        ResultActions result = mockMvc.perform(get("/banners")
                .cookie(cookie));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Banner API")
                                .summary("배너 목록 조회")
                                .description("배너 전체 목록을 orderIndex 순서로 조회합니다. (ADMIN 전용)")
                                .responseFields(
                                        fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("배너 ID"),
                                        fieldWithPath("[].imageUrl").type(JsonFieldType.STRING).description("배너 이미지 URL"),
                                        fieldWithPath("[].linkUrl").type(JsonFieldType.STRING).description("배너 클릭 시 이동할 URL").optional(),
                                        fieldWithPath("[].isActive").type(JsonFieldType.BOOLEAN).description("배너 활성화 여부"),
                                        fieldWithPath("[].orderIndex").type(JsonFieldType.NUMBER).description("배너 노출 순서"),
                                        fieldWithPath("[].createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                        fieldWithPath("[].updatedAt").type(JsonFieldType.STRING).description("수정일시")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("ADMIN 유저의 토큰")
                        )
                ));
    }

    @Test
    void 배너를_수정한다() throws Exception {
        // given
        Long bannerId = 1L;
        BannerUpdateRequestDto request = new BannerUpdateRequestDto(null, "https://example.com/new", false, 2);
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");
        when(bannerService.updateBanner(anyLong(), any())).thenReturn(
                new BannerResponseDto(bannerId, "https://s3.example.com/banner.jpg", "https://example.com/new", false, 2, Instant.now(), Instant.now())
        );

        // when
        ResultActions result = mockMvc.perform(patch("/banners/{bannerId}", bannerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .cookie(cookie));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.orderIndex").value(2))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Banner API")
                                .summary("배너 수정")
                                .description("배너의 정보를 수정합니다. null 필드는 수정되지 않습니다. (ADMIN 전용)")
                                .pathParameters(
                                        parameterWithName("bannerId").description("수정할 배너 ID")
                                )
                                .requestFields(
                                        fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("배너 이미지 URL (null이면 유지)").optional(),
                                        fieldWithPath("linkUrl").type(JsonFieldType.STRING).description("배너 링크 URL (null이면 유지)").optional(),
                                        fieldWithPath("isActive").type(JsonFieldType.BOOLEAN).description("활성화 여부 (null이면 유지)").optional(),
                                        fieldWithPath("orderIndex").type(JsonFieldType.NUMBER).description("노출 순서 (null이면 유지)").optional()
                                )
                                .responseFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("배너 ID"),
                                        fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("배너 이미지 URL"),
                                        fieldWithPath("linkUrl").type(JsonFieldType.STRING).description("배너 링크 URL"),
                                        fieldWithPath("isActive").type(JsonFieldType.BOOLEAN).description("활성화 여부"),
                                        fieldWithPath("orderIndex").type(JsonFieldType.NUMBER).description("노출 순서"),
                                        fieldWithPath("createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                        fieldWithPath("updatedAt").type(JsonFieldType.STRING).description("수정일시")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("ADMIN 유저의 토큰")
                        )
                ));
    }

    @Test
    void 배너_이미지를_업로드하고_배너를_생성한다() throws Exception {
        // given
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");
        MockMultipartFile image = new MockMultipartFile(
                "image", "banner.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image-data".getBytes()
        );
        when(bannerService.uploadBannerImage(any())).thenReturn(sampleBanner(1L));

        // when
        ResultActions result = mockMvc.perform(multipart("/banners/images")
                .file(image)
                .cookie(cookie));

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageUrl").value("https://s3.example.com/banner.jpg"))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Banner API")
                                .summary("배너 이미지 업로드 및 배너 생성")
                                .description("이미지를 업로드하고 새 배너를 생성합니다. (ADMIN 전용)")
                                .responseFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("생성된 배너 ID"),
                                        fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("업로드된 이미지 URL"),
                                        fieldWithPath("linkUrl").type(JsonFieldType.STRING).description("배너 링크 URL").optional(),
                                        fieldWithPath("isActive").type(JsonFieldType.BOOLEAN).description("활성화 여부 (기본값: true)"),
                                        fieldWithPath("orderIndex").type(JsonFieldType.NUMBER).description("노출 순서 (기본값: 0)"),
                                        fieldWithPath("createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                        fieldWithPath("updatedAt").type(JsonFieldType.STRING).description("수정일시")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("ADMIN 유저의 토큰")
                        )
                ));
    }

    @Test
    void 배너_이미지를_교체한다() throws Exception {
        // given
        Long bannerId = 1L;
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");
        MockMultipartFile image = new MockMultipartFile(
                "image", "new-banner.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image-data".getBytes()
        );
        when(bannerService.updateBannerImage(anyLong(), any())).thenReturn(
                new BannerResponseDto(bannerId, "https://s3.example.com/new-banner.jpg", null, true, 0, Instant.now(), Instant.now())
        );

        // when
        ResultActions result = mockMvc.perform(multipart("/banners/{bannerId}/images", bannerId)
                .file(image)
                .with(req -> { req.setMethod("PUT"); return req; })
                .cookie(cookie));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://s3.example.com/new-banner.jpg"))
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Banner API")
                                .summary("배너 이미지 교체")
                                .description("기존 배너의 이미지를 새 이미지로 교체합니다. (ADMIN 전용)")
                                .pathParameters(
                                        parameterWithName("bannerId").description("이미지를 교체할 배너 ID")
                                )
                                .responseFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("배너 ID"),
                                        fieldWithPath("imageUrl").type(JsonFieldType.STRING).description("교체된 이미지 URL"),
                                        fieldWithPath("linkUrl").type(JsonFieldType.STRING).description("배너 링크 URL").optional(),
                                        fieldWithPath("isActive").type(JsonFieldType.BOOLEAN).description("활성화 여부"),
                                        fieldWithPath("orderIndex").type(JsonFieldType.NUMBER).description("노출 순서"),
                                        fieldWithPath("createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                        fieldWithPath("updatedAt").type(JsonFieldType.STRING).description("수정일시")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("ADMIN 유저의 토큰")
                        )
                ));
    }

    @Test
    void 배너를_삭제한다() throws Exception {
        // given
        Long bannerId = 1L;
        Cookie cookie = new Cookie(COOKIE_NAME, "access_token");
        doNothing().when(bannerService).deleteBanner(anyLong());

        // when
        ResultActions result = mockMvc.perform(delete("/banners/{bannerId}", bannerId)
                .cookie(cookie));

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        resource(ResourceSnippetParameters.builder()
                                .tag("Banner API")
                                .summary("배너 삭제")
                                .description("배너를 영구 삭제합니다. S3 이미지도 함께 삭제됩니다. (ADMIN 전용)")
                                .pathParameters(
                                        parameterWithName("bannerId").description("삭제할 배너 ID")
                                )
                                .build()
                        ),
                        requestCookies(
                                cookieWithName(COOKIE_NAME).description("ADMIN 유저의 토큰")
                        )
                ));
    }
}
