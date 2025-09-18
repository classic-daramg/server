# 서비스 클래스 구현 가이드

## 1. 기본 애노테이션 정의
- @Service, @RequiredArgsConstructor을 기본으로 붙인다.
- @Transactional(readOnly = true)을 클래스 레벨에 기본으로 붙이고, 데이터 변경이 필요한 쓰기 메서드에만 @Transactional을 추가로 붙인다.

