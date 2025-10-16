package com.daramg.server.testsupport.support;

import com.daramg.server.testsupport.util.DatabaseIsolation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@DatabaseIsolation
public abstract class ServiceTestSupport {
}
