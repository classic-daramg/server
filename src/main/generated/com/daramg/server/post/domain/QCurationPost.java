package com.daramg.server.post.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCurationPost is a Querydsl query type for CurationPost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCurationPost extends EntityPathBase<CurationPost> {

    private static final long serialVersionUID = -2108421343L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCurationPost curationPost = new QCurationPost("curationPost");

    public final QPost _super;

    public final ListPath<com.daramg.server.composer.domain.Composer, com.daramg.server.composer.domain.QComposer> additionalComposers = this.<com.daramg.server.composer.domain.Composer, com.daramg.server.composer.domain.QComposer>createList("additionalComposers", com.daramg.server.composer.domain.Composer.class, com.daramg.server.composer.domain.QComposer.class, PathInits.DIRECT2);

    //inherited
    public final NumberPath<Integer> commentCount;

    //inherited
    public final StringPath content;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt;

    //inherited
    public final ListPath<String, StringPath> hashtags;

    //inherited
    public final NumberPath<Long> id;

    //inherited
    public final ListPath<String, StringPath> images;

    //inherited
    public final BooleanPath isBlocked;

    //inherited
    public final NumberPath<Integer> likeCount;

    //inherited
    public final EnumPath<PostStatus> postStatus;

    public final com.daramg.server.composer.domain.QComposer primaryComposer;

    //inherited
    public final StringPath title;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt;

    // inherited
    public final com.daramg.server.user.domain.QUser user;

    //inherited
    public final StringPath videoUrl;

    public QCurationPost(String variable) {
        this(CurationPost.class, forVariable(variable), INITS);
    }

    public QCurationPost(Path<? extends CurationPost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCurationPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCurationPost(PathMetadata metadata, PathInits inits) {
        this(CurationPost.class, metadata, inits);
    }

    public QCurationPost(Class<? extends CurationPost> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QPost(type, metadata, inits);
        this.commentCount = _super.commentCount;
        this.content = _super.content;
        this.createdAt = _super.createdAt;
        this.hashtags = _super.hashtags;
        this.id = _super.id;
        this.images = _super.images;
        this.isBlocked = _super.isBlocked;
        this.likeCount = _super.likeCount;
        this.postStatus = _super.postStatus;
        this.primaryComposer = inits.isInitialized("primaryComposer") ? new com.daramg.server.composer.domain.QComposer(forProperty("primaryComposer")) : null;
        this.title = _super.title;
        this.updatedAt = _super.updatedAt;
        this.user = _super.user;
        this.videoUrl = _super.videoUrl;
    }

}

