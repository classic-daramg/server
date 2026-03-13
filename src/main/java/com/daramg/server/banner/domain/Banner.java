package com.daramg.server.banner.domain;

import com.daramg.server.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Entity
@Getter
@Table(name = "banners")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banner extends BaseEntity<Banner> {

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "link_url", columnDefinition = "TEXT")
    private String linkUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 0;

    @Builder
    public Banner(@NonNull String imageUrl, String linkUrl, boolean isActive, int orderIndex) {
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.isActive = isActive;
        this.orderIndex = orderIndex;
    }

    public static Banner of(String imageUrl) {
        return Banner.builder()
                .imageUrl(imageUrl)
                .isActive(true)
                .orderIndex(0)
                .build();
    }

    public void update(String imageUrl, String linkUrl, Boolean isActive, Integer orderIndex) {
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (linkUrl != null) this.linkUrl = linkUrl;
        if (isActive != null) this.isActive = isActive;
        if (orderIndex != null) this.orderIndex = orderIndex;
    }
}
