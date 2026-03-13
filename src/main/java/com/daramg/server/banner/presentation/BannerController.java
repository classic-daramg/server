package com.daramg.server.banner.presentation;

import com.daramg.server.banner.application.BannerService;
import com.daramg.server.banner.dto.BannerResponseDto;
import com.daramg.server.banner.dto.BannerUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public List<BannerResponseDto> getBanners() {
        return bannerService.getBanners();
    }

    @PatchMapping("/{bannerId}")
    public BannerResponseDto updateBanner(
            @PathVariable Long bannerId,
            @RequestBody BannerUpdateRequestDto request
    ) {
        return bannerService.updateBanner(bannerId, request);
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public BannerResponseDto uploadBannerImage(
            @RequestPart("image") MultipartFile image
    ) {
        return bannerService.uploadBannerImage(image);
    }
}
