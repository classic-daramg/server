package com.daramg.server.notice.domain;

import com.daramg.server.common.converter.JsonArrayConverter;
import com.daramg.server.common.domain.BaseEntity;
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

    protected Notice() {
    }

    @Column
    private String title;

    @Column
    private String content;

    @Convert(converter = JsonArrayConverter.class)
    @Column(name = "images", columnDefinition = "JSON")
    private List<String> image;

    @Column
    private String videoUrl;

}
