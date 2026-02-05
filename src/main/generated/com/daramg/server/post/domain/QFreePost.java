package com.daramg.server.post.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFreePost is a Querydsl query type for FreePost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFreePost extends EntityPathBase<FreePost> {

    private static final long serialVersionUID = 533742424L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFreePost freePost = new QFreePost("freePost");

    public final QPost _super;

    //inherited
    public final NumberPath<Integer> commentCount;

    //inherited
    public final StringPath content;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt;

    //inherited
    public final ListPath<String, StringPath> hashtags;

    //inherited
    public final NumberPath<Long> id;

    //inherited
    public final ListPath<String, StringPath> images;

    //inherited
    public final BooleanPath isBlocked;

    //inherited
    public final BooleanPath isDeleted;

    //inherited
    public final NumberPath<Integer> likeCount;

    //inherited
    public final EnumPath<PostStatus> postStatus;

    //inherited
    public final StringPath title;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt;

    // inherited
    public final com.daramg.server.user.domain.QUser user;

    //inherited
    public final StringPath videoUrl;

    public QFreePost(String variable) {
        this(FreePost.class, forVariable(variable), INITS);
    }

    public QFreePost(Path<? extends FreePost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFreePost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFreePost(PathMetadata metadata, PathInits inits) {
        this(FreePost.class, metadata, inits);
    }

    public QFreePost(Class<? extends FreePost> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QPost(type, metadata, inits);
        this.commentCount = _super.commentCount;
        this.content = _super.content;
        this.createdAt = _super.createdAt;
        this.deletedAt = _super.deletedAt;
        this.hashtags = _super.hashtags;
        this.id = _super.id;
        this.images = _super.images;
        this.isBlocked = _super.isBlocked;
        this.isDeleted = _super.isDeleted;
        this.likeCount = _super.likeCount;
        this.postStatus = _super.postStatus;
        this.title = _super.title;
        this.updatedAt = _super.updatedAt;
        this.user = _super.user;
        this.videoUrl = _super.videoUrl;
    }

}

