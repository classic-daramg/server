# 컨트롤러 구현 가이드

## 1. 기본 애노테이션 정의
- 클래스 레벨에 @RestController, @RequiredArgsConstructor 붙이기
- 클래스 레벨에 디폴트 uri 매핑
  - @RequestMapping("/teams"): 일반적으로 해당 도메인에 s를 붙여서 표현

## 2. 반환 타입
- 반환 타입은 기본적으로 void로 한다. 단, HttpStatus로 상태를 표현한다.
- update 메서드는 기본적으로 PATCH로 작성한다.

## 3. 메서드명
- 메서드명은 동사로 작성하고 기본적으로 create, update, delete로 CRUD를 표현한다.
