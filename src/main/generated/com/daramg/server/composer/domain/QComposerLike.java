package com.daramg.server.composer.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QComposerLike is a Querydsl query type for ComposerLike
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QComposerLike extends EntityPathBase<ComposerLike> {

    private static final long serialVersionUID = 136332675L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QComposerLike composerLike = new QComposerLike("composerLike");

    public final com.daramg.server.common.domain.QBaseEntity _super = new com.daramg.server.common.domain.QBaseEntity(this);

    public final QComposer composer;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public final com.daramg.server.user.domain.QUser user;

    public QComposerLike(String variable) {
        this(ComposerLike.class, forVariable(variable), INITS);
    }

    public QComposerLike(Path<? extends ComposerLike> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QComposerLike(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QComposerLike(PathMetadata metadata, PathInits inits) {
        this(ComposerLike.class, metadata, inits);
    }

    public QComposerLike(Class<? extends ComposerLike> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.composer = inits.isInitialized("composer") ? new QComposer(forProperty("composer")) : null;
        this.user = inits.isInitialized("user") ? new com.daramg.server.user.domain.QUser(forProperty("user")) : null;
    }

}

