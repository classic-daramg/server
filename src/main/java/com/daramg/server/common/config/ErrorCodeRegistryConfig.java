package com.daramg.server.common.config;

import com.daramg.server.common.exception.BaseErrorCode;
import com.daramg.server.common.exception.CommonErrorStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ErrorCodeRegistryConfig {
    @Bean
    public List<BaseErrorCode> errorCodeList() {
        return new ArrayList<>(Arrays.asList(CommonErrorStatus.values()));
    }
}
