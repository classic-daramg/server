package com.daramg.server.banner.application;

import com.daramg.server.banner.domain.Banner;
import com.daramg.server.banner.dto.BannerResponseDto;
import com.daramg.server.banner.dto.BannerUpdateRequestDto;
import com.daramg.server.banner.exception.BannerErrorStatus;
import com.daramg.server.banner.repository.BannerRepository;
import com.daramg.server.common.application.S3ImageService;
import com.daramg.server.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;
    private final S3ImageService s3ImageService;

    @Transactional(readOnly = true)
    public List<BannerResponseDto> getBanners() {
        return bannerRepository.findAllByOrderByOrderIndexAsc().stream()
                .map(BannerResponseDto::from)
                .toList();
    }

    @Transactional
    public BannerResponseDto updateBanner(Long bannerId, BannerUpdateRequestDto request) {
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new BusinessException(BannerErrorStatus.BANNER_NOT_FOUND));
        banner.update(request.imageUrl(), request.linkUrl(), request.isActive(), request.orderIndex());
        return BannerResponseDto.from(banner);
    }

    @Transactional
    public BannerResponseDto uploadBannerImage(MultipartFile image) {
        String imageUrl = s3ImageService.uploadImage(image);
        Banner banner = bannerRepository.save(Banner.of(imageUrl));
        return BannerResponseDto.from(banner);
    }
}
