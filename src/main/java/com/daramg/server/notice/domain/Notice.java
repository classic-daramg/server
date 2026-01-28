package com.daramg.server.notice.domain;

import com.daramg.server.common.converter.JsonArrayConverter;
import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.notice.domain.vo.NoticeCreateVo;
import com.daramg.server.notice.domain.vo.NoticeUpdateVo;
import com.daramg.server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Notice extends BaseEntity<Notice> {

    private static final String EMPTY_UPDATE_REQUEST = "수정 사항이 없습니다.";

    protected Notice() {
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Convert(converter = JsonArrayConverter.class)
    @Column(name = "images", columnDefinition = "JSON")
    private List<String> images = new ArrayList<>();

    @Column(name = "video_url")
    private String videoUrl;

    public void update(NoticeUpdateVo vo) {
        if (vo.getTitle() == null && vo.getContent() == null
                && vo.getImages() == null && vo.getVideoUrl() == null) {
            throw new BusinessException(EMPTY_UPDATE_REQUEST);
        }

        if (vo.getTitle() != null) {
            updateTitle(vo.getTitle());
        }

        if (vo.getContent() != null) {
            updateContent(vo.getContent());
        }

        if (vo.getImages() != null) {
            updateImages(vo.getImages());
        }

        if (vo.getVideoUrl() != null) {
            updateVideoUrl(vo.getVideoUrl());
        }

    }

    @Builder
    private Notice(String title, String content, List<String> images, String videoUrl, User user){
        this.title = title;
        this.content = content;
        this.images = images;
        this.videoUrl = videoUrl;
        this.user = user;
    }

    public static Notice from(NoticeCreateVo vo) {
        return Notice.builder()
                .title(vo.getTitle())
                .content(vo.getContent())
                .images(vo.getImages())
                .videoUrl(vo.getVideoUrl())
                .user(vo.getUser())
                .build();
    }

    protected void updateTitle(String title) { this.title = title; }

    protected void updateContent(String content) { this.content = content; }

    protected void updateImages(List<String> images) { this.images = images; }

    protected void updateVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
}
