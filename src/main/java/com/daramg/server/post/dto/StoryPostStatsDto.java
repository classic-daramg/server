package com.daramg.server.post.dto;

import java.time.Instant;

public record StoryPostStatsDto(long storyPostCount, Instant lastStoryPostAt) {}
