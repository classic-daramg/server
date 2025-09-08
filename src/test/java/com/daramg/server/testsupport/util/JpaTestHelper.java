package com.daramg.server.testsupport.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.stereotype.Component;

@Component
@TestComponent
public class JpaTestHelper {

    @PersistenceContext
    EntityManager em;

    public <T> T saveAndFlush(T entity) {
        em.persist(entity);
        em.flush();
        return entity;
    }

    public void saveAllAndFlush(Object... entities) {
        for (Object e : entities) {em.persist(e);}
        em.flush();
    }

    public <T> T findByIdAfterClear(Class<T> clazz, Object id) {
        em.flush();
        em.clear();
        return em.find(clazz, id);
    }
}
