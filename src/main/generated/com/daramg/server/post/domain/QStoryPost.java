package com.daramg.server.post.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStoryPost is a Querydsl query type for StoryPost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoryPost extends EntityPathBase<StoryPost> {

    private static final long serialVersionUID = 1523913353L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStoryPost storyPost = new QStoryPost("storyPost");

    public final QPost _super;

    //inherited
    public final NumberPath<Integer> commentCount;

    //inherited
    public final StringPath content;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt;

    //inherited
    public final DateTimePath<java.time.Instant> deletedAt;

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

    public final com.daramg.server.composer.domain.QComposer primaryComposer;

    //inherited
    public final StringPath title;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt;

    // inherited
    public final com.daramg.server.user.domain.QUser user;

    //inherited
    public final StringPath videoUrl;

    //inherited
    public final NumberPath<Integer> viewCount;

    public QStoryPost(String variable) {
        this(StoryPost.class, forVariable(variable), INITS);
    }

    public QStoryPost(Path<? extends StoryPost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStoryPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStoryPost(PathMetadata metadata, PathInits inits) {
        this(StoryPost.class, metadata, inits);
    }

    public QStoryPost(Class<? extends StoryPost> type, PathMetadata metadata, PathInits inits) {
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
        this.primaryComposer = inits.isInitialized("primaryComposer") ? new com.daramg.server.composer.domain.QComposer(forProperty("primaryComposer")) : null;
        this.title = _super.title;
        this.updatedAt = _super.updatedAt;
        this.user = _super.user;
        this.videoUrl = _super.videoUrl;
        this.viewCount = _super.viewCount;
    }

}

