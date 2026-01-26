package com.daramg.server.notice.domain;

import com.daramg.server.common.converter.JsonArrayConverter;
import com.daramg.server.common.domain.BaseEntity;
import com.daramg.server.common.exception.BusinessException;
import com.daramg.server.notice.domain.vo.NoticeUpdateVo;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Entity
public class Notice extends BaseEntity<Notice> {

    private static final String EMPTY_UPDATE_REQUEST = "수정 사항이 없습니다.";

    protected Notice() {
    }

    @Column
    private String title;

    @Column
    private String content;

    @Convert(converter = JsonArrayConverter.class)
    @Column(name = "images", columnDefinition = "JSON")
    private List<String> images;

    @Column
    private String videoUrl;

    public void update(NoticeUpdateVo vo) {
        if (vo.getTitle() != null) this.title = vo.getTitle();
        if (vo.getContent() != null) this.content = vo.getContent();
        if (vo.getImages() != null) this.images = vo.getImages();
        if (vo.getVideoUrl() != null) this.videoUrl = vo.getVideoUrl();


        if (vo.getTitle() == null && vo.getContent() == null
                && vo.getImages() == null && vo.getVideoUrl() == null) {
            throw new BusinessException(EMPTY_UPDATE_REQUEST);
        }
    }
}
