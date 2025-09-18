# 서비스 클래스 구현 가이드

## 1. 기본 애노테이션 정의
- @Service, @RequiredArgsConstructor을 기본으로 붙인다.
- @Transactional도 붙이되, 필요없는 경우에는 제외하거나 필요한 메서드 레벨에만 붙인다.

