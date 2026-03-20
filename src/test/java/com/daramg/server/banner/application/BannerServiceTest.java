package com.daramg.server.banner.application;

import com.daramg.server.banner.domain.Banner;
import com.daramg.server.banner.dto.BannerResponseDto;
import com.daramg.server.banner.dto.BannerUpdateRequestDto;
import com.daramg.server.banner.repository.BannerRepository;
import com.daramg.server.common.application.S3ImageService;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.testsupport.support.ServiceTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BannerServiceTest extends ServiceTestSupport {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private BannerRepository bannerRepository;

    @MockitoBean
    private S3ImageService s3ImageService;

    @Nested
    @DisplayName("배너 목록 조회")
    class GetBannersTest {

        @Test
        void 배너_목록을_orderIndex_순으로_반환한다() {
            // given
            bannerRepository.save(Banner.builder().imageUrl("https://s3.example.com/a.jpg").isActive(true).orderIndex(2).build());
            bannerRepository.save(Banner.builder().imageUrl("https://s3.example.com/b.jpg").isActive(true).orderIndex(0).build());
            bannerRepository.save(Banner.builder().imageUrl("https://s3.example.com/c.jpg").isActive(false).orderIndex(1).build());

            // when
            List<BannerResponseDto> result = bannerService.getBanners();

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).orderIndex()).isEqualTo(0);
            assertThat(result.get(1).orderIndex()).isEqualTo(1);
            assertThat(result.get(2).orderIndex()).isEqualTo(2);
        }

        @Test
        void 배너가_없으면_빈_목록을_반환한다() {
            // when
            List<BannerResponseDto> result = bannerService.getBanners();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("배너 수정")
    class UpdateBannerTest {

        @Test
        void 배너_필드를_정상적으로_수정한다() {
            // given
            Banner banner = bannerRepository.save(Banner.builder().imageUrl("https://s3.example.com/old.jpg").isActive(true).orderIndex(0).build());
            BannerUpdateRequestDto request = new BannerUpdateRequestDto("https://s3.example.com/new.jpg", "https://example.com", false, 5);

            // when
            BannerResponseDto result = bannerService.updateBanner(banner.getId(), request);

            // then
            assertThat(result.imageUrl()).isEqualTo("https://s3.example.com/new.jpg");
            assertThat(result.linkUrl()).isEqualTo("https://example.com");
            assertThat(result.isActive()).isFalse();
            assertThat(result.orderIndex()).isEqualTo(5);
        }

        @Test
        void null_필드는_기존_값을_유지한다() {
            // given
            Banner banner = bannerRepository.save(Banner.builder().imageUrl("https://s3.example.com/img.jpg").isActive(true).orderIndex(3).build());
            BannerUpdateRequestDto request = new BannerUpdateRequestDto(null, null, null, null);

            // when
            BannerResponseDto result = bannerService.updateBanner(banner.getId(), request);

            // then
            assertThat(result.imageUrl()).isEqualTo("https://s3.example.com/img.jpg");
            assertThat(result.isActive()).isTrue();
            assertThat(result.orderIndex()).isEqualTo(3);
        }

        @Test
        void 존재하지_않는_배너_수정_시_예외가_발생한다() {
            // given
            BannerUpdateRequestDto request = new BannerUpdateRequestDto(null, null, false, null);

            // when & then
            assertThatThrownBy(() -> bannerService.updateBanner(999L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("배너를 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("배너 이미지 업로드")
    class UploadBannerImageTest {

        @Test
        void 이미지_업로드_후_배너가_생성된다() {
            // given
            String imageUrl = "https://s3.example.com/banner-uuid.jpg";
            when(s3ImageService.uploadImage(any())).thenReturn(imageUrl);
            MockMultipartFile image = new MockMultipartFile("image", "banner.jpg", "image/jpeg", "fake".getBytes());

            // when
            BannerResponseDto result = bannerService.uploadBannerImage(image);

            // then
            assertThat(result.imageUrl()).isEqualTo(imageUrl);
            assertThat(result.isActive()).isTrue();
            assertThat(result.orderIndex()).isEqualTo(0);
            assertThat(result.id()).isNotNull();
            assertThat(bannerRepository.findAll()).hasSize(1);
        }
    }
}
