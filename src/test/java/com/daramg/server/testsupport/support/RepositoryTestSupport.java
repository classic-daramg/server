package com.daramg.server.testsupport.support;

import com.daramg.server.testsupport.util.JpaTestHelper;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(JpaTestHelper.class)
@ActiveProfiles("local")
public abstract class RepositoryTestSupport {
}
