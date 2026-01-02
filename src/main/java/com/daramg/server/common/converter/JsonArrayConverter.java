package com.daramg.server.common.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Converter
public class JsonArrayConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String EMPTY_JSON_ARRAY = "[]";
    private static final String NULL_STRING = "null";
    private static final String DOUBLE_QUOTE = "\"";
    private static final String SINGLE_QUOTE = "'";
    private static final String ARRAY_START = "[";
    private static final String ARRAY_END = "]";
    private static final int QUOTE_START_INDEX = 1;

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return EMPTY_JSON_ARRAY;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            log.error("Error converting List<String> to JSON", e);
            return EMPTY_JSON_ARRAY;
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return new ArrayList<>();
        }
        
        String trimmed = dbData.trim();
        
        // 빈 문자열이나 빈 배열 처리
        if (trimmed.isEmpty() || trimmed.equals(EMPTY_JSON_ARRAY) || trimmed.equals(NULL_STRING)) {
            return new ArrayList<>();
        }
        
        // 큰따옴표로 감싸진 JSON 문자열인 경우 제거 (예: "[\"image2\"]" -> ["image2"])
        if (trimmed.startsWith(DOUBLE_QUOTE) && trimmed.endsWith(DOUBLE_QUOTE)) {
            // 외부 따옴표 제거
            trimmed = trimmed.substring(QUOTE_START_INDEX, trimmed.length() - 1);
            // 이스케이프된 따옴표를 실제 따옴표로 변환
            trimmed = trimmed.replace("\\\"", "\"");
        }
        
        // 작은따옴표로 감싸진 경우 제거
        if (trimmed.startsWith(SINGLE_QUOTE) && trimmed.endsWith(SINGLE_QUOTE)) {
            trimmed = trimmed.substring(QUOTE_START_INDEX, trimmed.length() - 1);
        }
        
        try {
            // JSON 배열 형식인지 확인
            if (!trimmed.startsWith(ARRAY_START) || !trimmed.endsWith(ARRAY_END)) {
                log.warn("Invalid JSON array format: {}", dbData);
                return new ArrayList<>();
            }
            
            return objectMapper.readValue(trimmed, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Error converting JSON to List<String>. dbData: {}", dbData, e);
            return new ArrayList<>();
        }
    }
}

