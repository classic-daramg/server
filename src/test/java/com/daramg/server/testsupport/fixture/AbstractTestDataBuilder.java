package com.daramg.server.testsupport.fixture;

import com.daramg.server.testsupport.util.JpaTestHelper;

import java.time.Clock;
import java.util.Objects;

public abstract class AbstractTestDataBuilder<T, SELF extends AbstractTestDataBuilder<T, SELF>> {

    protected Clock clock;

    protected AbstractTestDataBuilder() {
        this.clock = Clock.systemUTC();
    }

    @SuppressWarnings("unchecked")
    public SELF withClock(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
        return (SELF) this;
    }

    public final T build() {
        return buildInternal();
    }

    protected abstract T buildInternal();

    public final T buildAndPersist(JpaTestHelper helper) {
        T built = build();
        return helper.saveAndFlush(built);
    }

}
