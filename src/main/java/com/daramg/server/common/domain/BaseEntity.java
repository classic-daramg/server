package com.daramg.server.common.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public class BaseEntity<T extends AbstractAggregateRoot<T>> extends AbstractAggregateRoot<T> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.deletedAt = null;
        this.isDeleted = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity<?> that)) return false;

        if (this.id == null || that.id == null) {
            return false;
        }
        return id.equals(that.id);
    }

//    @Override
//    public final boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null) return false;
//
//        // ✅ 프록시까지 고려한 "실제 엔티티 클래스" 비교
//        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
//
//        BaseEntity<?> that = (BaseEntity<?>) o;
//
//        // ✅ id가 없을 때는 엔티티 동일성 비교를 true로 두지 않는다 (컬렉션/캐시 안전)
//        if (this.id == null || that.id == null) return false;
//
//        return this.id.equals(that.id);
//    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

//    @Override
//    public final int hashCode() {
//        // equals와 동일한 기준(실제 클래스)을 사용
//        return Hibernate.getClass(this).hashCode();
//    }

    public void softDelete() {
        if(this.isDeleted) { return; }

        this.deletedAt = LocalDateTime.now();
        this.isDeleted = true;
    }

    public void restore() {
        if(!this.isDeleted) { return; }

        this.deletedAt = null;
        this.isDeleted = false;
    }
}