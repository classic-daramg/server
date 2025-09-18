# 컨트롤러 구현 가이드

## 1. 기본 애노테이션 정의
- 클래스 레벨에 @RestController, @RequiredArgsConstructor 붙이기
- 클래스 레벨에 디폴트 uri 매핑
  - @RequestMapping("/teams"): 일반적으로 해당 도메인에 s를 붙여서 표현
