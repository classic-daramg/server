package com.daramg.server.composer.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QComposer is a Querydsl query type for Composer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QComposer extends EntityPathBase<Composer> {

    private static final long serialVersionUID = -343087156L;

    public static final QComposer composer = new QComposer("composer");

    public final com.daramg.server.common.domain.QBaseEntity _super = new com.daramg.server.common.domain.QBaseEntity(this);

    public final StringPath bio = createString("bio");

    public final NumberPath<Short> birthYear = createNumber("birthYear", Short.class);

    public final EnumPath<Continent> continent = createEnum("continent", Continent.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Short> deathYear = createNumber("deathYear", Short.class);

    public final StringPath englishName = createString("englishName");

    public final EnumPath<Era> era = createEnum("era", Era.class);

    public final EnumPath<Gender> gender = createEnum("gender", Gender.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath koreanName = createString("koreanName");

    public final StringPath nationality = createString("nationality");

    public final StringPath nativeName = createString("nativeName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QComposer(String variable) {
        super(Composer.class, forVariable(variable));
    }

    public QComposer(Path<? extends Composer> path) {
        super(path.getType(), path.getMetadata());
    }

    public QComposer(PathMetadata metadata) {
        super(Composer.class, metadata);
    }

}

