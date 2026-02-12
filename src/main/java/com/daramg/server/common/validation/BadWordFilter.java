package com.daramg.server.common.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class BadWordFilter {

    private final List<Pattern> patterns = new ArrayList<>();

    public BadWordFilter() {
        loadBadWords();
    }

    private void loadBadWords() {
        try {
            ClassPathResource resource = new ClassPathResource("bad-words.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .forEach(word -> patterns.add(buildPattern(word)));
            }
            log.info("Loaded {} bad word patterns", patterns.size());
        } catch (IOException e) {
            log.warn("Failed to load bad-words.txt: {}", e.getMessage());
        }
    }

    private Pattern buildPattern(String word) {
        boolean isKorean = word.chars().anyMatch(ch -> ch >= 0xAC00 && ch <= 0xD7A3);

        if (isKorean) {
            // 한국어: 글자 사이 공백 무시 패턴
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                if (i > 0) {
                    sb.append("\\s*");
                }
                sb.append(Pattern.quote(String.valueOf(word.charAt(i))));
            }
            return Pattern.compile(sb.toString());
        } else {
            // 영어: 대소문자 무시 + leet-speak 치환
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                char ch = Character.toLowerCase(word.charAt(i));
                switch (ch) {
                    case 'a' -> sb.append("[aA@4]");
                    case 'e' -> sb.append("[eE3]");
                    case 'i' -> sb.append("[iI1!]");
                    case 'o' -> sb.append("[oO0]");
                    case 's' -> sb.append("[sS$5]");
                    default -> sb.append("[")
                            .append(Pattern.quote(String.valueOf(Character.toLowerCase(ch))))
                            .append(Pattern.quote(String.valueOf(Character.toUpperCase(ch))))
                            .append("]");
                }
            }
            return Pattern.compile(sb.toString());
        }
    }

    public boolean containsBadWord(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        return patterns.stream().anyMatch(pattern -> pattern.matcher(text).find());
    }
}
