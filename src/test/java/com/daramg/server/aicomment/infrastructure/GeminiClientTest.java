package com.daramg.server.aicomment.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiClientTest {

    @Test
    @DisplayName("응답 파싱: candidates → content → parts → text 추출에 성공한다")
    void 응답에서_텍스트를_정상적으로_추출한다() {
        // given
        GeminiClient client = new GeminiClient("fake-key");

        Map<String, Object> response = Map.of(
                "candidates", List.of(
                        Map.of("content", Map.of(
                                "parts", List.of(
                                        Map.of("text", "AI가 생성한 댓글입니다.")
                                )
                        ))
                )
        );

        // when
        String result = (String) ReflectionTestUtils.invokeMethod(client, "extractText", response);

        // then
        assertThat(result).isEqualTo("AI가 생성한 댓글입니다.");
    }

    @Test
    @DisplayName("응답 파싱: candidates가 비어있으면 예외가 발생한다")
    void candidates가_비어있으면_예외가_발생한다() {
        // given
        GeminiClient client = new GeminiClient("fake-key");

        Map<String, Object> response = Map.of(
                "candidates", List.of()
        );

        // when & then
        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(client, "extractText", response)
        ).isInstanceOf(Exception.class);
    }
}
