package com.daramg.server.common.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BadWordFilterTest {

    private BadWordFilter badWordFilter;

    @BeforeEach
    void setUp() {
        badWordFilter = new BadWordFilter();
    }

    @Nested
    @DisplayName("한국어 비속어 필터링")
    class KoreanBadWords {

        @Test
        @DisplayName("한국어 비속어가 포함된 텍스트를 탐지한다")
        void detectsKoreanBadWord() {
            assertThat(badWordFilter.containsBadWord("이 시발 뭐야")).isTrue();
            assertThat(badWordFilter.containsBadWord("병신같은")).isTrue();
        }

        @Test
        @DisplayName("글자 사이 공백이 있어도 탐지한다")
        void detectsKoreanBadWordWithSpaces() {
            assertThat(badWordFilter.containsBadWord("시 발")).isTrue();
            assertThat(badWordFilter.containsBadWord("병 신")).isTrue();
        }

        @Test
        @DisplayName("정상적인 한국어 텍스트는 통과한다")
        void passesNormalKoreanText() {
            assertThat(badWordFilter.containsBadWord("안녕하세요")).isFalse();
            assertThat(badWordFilter.containsBadWord("좋은 하루 보내세요")).isFalse();
        }
    }

    @Nested
    @DisplayName("영어 비속어 필터링")
    class EnglishBadWords {

        @Test
        @DisplayName("영어 비속어가 포함된 텍스트를 탐지한다")
        void detectsEnglishBadWord() {
            assertThat(badWordFilter.containsBadWord("what the fuck")).isTrue();
            assertThat(badWordFilter.containsBadWord("this is shit")).isTrue();
        }

        @Test
        @DisplayName("대소문자를 무시하고 탐지한다")
        void detectsCaseInsensitive() {
            assertThat(badWordFilter.containsBadWord("FUCK")).isTrue();
            assertThat(badWordFilter.containsBadWord("Shit")).isTrue();
        }

        @Test
        @DisplayName("leet-speak 치환을 탐지한다")
        void detectsLeetSpeak() {
            assertThat(badWordFilter.containsBadWord("fvck")).isFalse(); // v는 치환 대상 아님
            assertThat(badWordFilter.containsBadWord("sh1t")).isTrue();
            assertThat(badWordFilter.containsBadWord("@sshole")).isTrue();
        }

        @Test
        @DisplayName("정상적인 영어 텍스트는 통과한다")
        void passesNormalEnglishText() {
            assertThat(badWordFilter.containsBadWord("hello world")).isFalse();
            assertThat(badWordFilter.containsBadWord("good morning")).isFalse();
        }
    }

    @Nested
    @DisplayName("null/빈 문자열 처리")
    class NullAndBlank {

        @Test
        @DisplayName("null은 통과한다")
        void passesNull() {
            assertThat(badWordFilter.containsBadWord(null)).isFalse();
        }

        @Test
        @DisplayName("빈 문자열은 통과한다")
        void passesEmptyString() {
            assertThat(badWordFilter.containsBadWord("")).isFalse();
        }

        @Test
        @DisplayName("공백 문자열은 통과한다")
        void passesBlankString() {
            assertThat(badWordFilter.containsBadWord("   ")).isFalse();
        }
    }
}
