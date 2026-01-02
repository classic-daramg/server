package com.daramg.server.common.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JsonArrayConverter 테스트")
class JsonArrayConverterTest {

    private final JsonArrayConverter converter = new JsonArrayConverter();

    @Nested
    @DisplayName("정상 테스트 - convertToDatabaseColumn")
    class ConvertToDatabaseColumnNormalTest {

        @Test
        @DisplayName("null 리스트를 JSON 배열 문자열로 변환한다")
        void convertNullList() {
            // when
            String result = converter.convertToDatabaseColumn(null);

            // then
            assertThat(result).isEqualTo("[]");
        }

        @Test
        @DisplayName("빈 리스트를 JSON 배열 문자열로 변환한다")
        void convertEmptyList() {
            // given
            List<String> emptyList = new ArrayList<>();

            // when
            String result = converter.convertToDatabaseColumn(emptyList);

            // then
            assertThat(result).isEqualTo("[]");
        }

        @Test
        @DisplayName("단일 요소 리스트를 JSON 배열 문자열로 변환한다")
        void convertSingleElementList() {
            // given
            List<String> list = Arrays.asList("image1");

            // when
            String result = converter.convertToDatabaseColumn(list);

            // then
            assertThat(result).isEqualTo("[\"image1\"]");
        }

        @Test
        @DisplayName("여러 요소 리스트를 JSON 배열 문자열로 변환한다")
        void convertMultipleElementList() {
            // given
            List<String> list = Arrays.asList("image1", "image2", "image3");

            // when
            String result = converter.convertToDatabaseColumn(list);

            // then
            assertThat(result).isEqualTo("[\"image1\",\"image2\",\"image3\"]");
        }

        @Test
        @DisplayName("특수 문자가 포함된 요소를 JSON 배열 문자열로 변환한다")
        void convertListWithSpecialCharacters() {
            // given
            List<String> list = Arrays.asList("image_1", "image-2", "image 3");

            // when
            String result = converter.convertToDatabaseColumn(list);

            // then
            assertThat(result).isEqualTo("[\"image_1\",\"image-2\",\"image 3\"]");
        }
    }

    @Nested
    @DisplayName("정상 테스트 - convertToEntityAttribute")
    class ConvertToEntityAttributeNormalTest {

        @Test
        @DisplayName("null을 빈 리스트로 변환한다")
        void convertNull() {
            // when
            List<String> result = converter.convertToEntityAttribute(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열을 빈 리스트로 변환한다")
        void convertEmptyString() {
            // when
            List<String> result = converter.convertToEntityAttribute("");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 JSON 배열 문자열을 빈 리스트로 변환한다")
        void convertEmptyJsonArray() {
            // when
            List<String> result = converter.convertToEntityAttribute("[]");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null 문자열을 빈 리스트로 변환한다")
        void convertNullString() {
            // when
            List<String> result = converter.convertToEntityAttribute("null");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("정상 JSON 배열 문자열을 리스트로 변환한다")
        void convertValidJsonArray() {
            // when
            List<String> result = converter.convertToEntityAttribute("[\"image1\",\"image2\"]");

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly("image1", "image2");
        }

        @Test
        @DisplayName("작은따옴표로 감싸진 JSON 배열 문자열을 리스트로 변환한다")
        void convertSingleQuotedJsonArray() {
            // when
            List<String> result = converter.convertToEntityAttribute("'[\"image1\",\"image2\"]'");

            // then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly("image1", "image2");
        }

        @Test
        @DisplayName("숫자 배열을 문자열 리스트로 변환한다")
        void convertNumberArray() {
            // when
            List<String> result = converter.convertToEntityAttribute("[1,2,3]");

            // then
            // ObjectMapper가 숫자를 문자열로 자동 변환
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly("1", "2", "3");
        }
    }

    @Nested
    @DisplayName("비정상 테스트 - convertToEntityAttribute")
    class ConvertToEntityAttributeAbnormalTest {

        @Test
        @DisplayName("유효하지 않은 JSON 형식 문자열을 빈 리스트로 변환한다")
        void convertInvalidJsonFormat() {
            // when
            List<String> result = converter.convertToEntityAttribute("invalid json");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("잘못된 JSON 배열 형식을 빈 리스트로 변환한다")
        void convertWithoutArrayStart() {
            // when
            List<String> result = converter.convertToEntityAttribute("\"image1\",\"image2\"]");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("양방향 변환 테스트")
    class BidirectionalConversionTest {

        @Test
        @DisplayName("리스트를 JSON으로 변환 후 다시 리스트로 변환하면 원본과 동일하다")
        void bidirectionalConversion() {
            // given
            List<String> original = Arrays.asList("image1", "image2", "image3");

            // when
            String json = converter.convertToDatabaseColumn(original);
            List<String> converted = converter.convertToEntityAttribute(json);

            // then
            assertThat(converted).isEqualTo(original);
        }

        @Test
        @DisplayName("빈 리스트를 JSON으로 변환 후 다시 리스트로 변환하면 빈 리스트가 된다")
        void bidirectionalConversionEmptyList() {
            // given
            List<String> original = new ArrayList<>();

            // when
            String json = converter.convertToDatabaseColumn(original);
            List<String> converted = converter.convertToEntityAttribute(json);

            // then
            assertThat(converted).isEmpty();
            assertThat(converted).isEqualTo(original);
        }
    }
}

