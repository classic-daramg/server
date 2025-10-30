package com.daramg.server.composer.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QComposerPost is a Querydsl query type for ComposerPost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QComposerPost extends EntityPathBase<ComposerPost> {

    private static final long serialVersionUID = 136457868L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QComposerPost composerPost = new QComposerPost("composerPost");

    public final com.daramg.server.common.domain.QBaseEntity _super = new com.daramg.server.common.domain.QBaseEntity(this);

    public final QComposer composer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final com.daramg.server.post.domain.QPost post;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QComposerPost(String variable) {
        this(ComposerPost.class, forVariable(variable), INITS);
    }

    public QComposerPost(Path<? extends ComposerPost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QComposerPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QComposerPost(PathMetadata metadata, PathInits inits) {
        this(ComposerPost.class, metadata, inits);
    }

    public QComposerPost(Class<? extends ComposerPost> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.composer = inits.isInitialized("composer") ? new QComposer(forProperty("composer")) : null;
        this.post = inits.isInitialized("post") ? new com.daramg.server.post.domain.QPost(forProperty("post"), inits.get("post")) : null;
    }

}

