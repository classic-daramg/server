# 도메인 구현 가이드
- 기본적으로 엔티티 간에는 단방향 매핑을 우선적으로 고려한다.

## 기본 엔티티 클래스 구현

### 1. 기본 클래스 레벨 애노테이션
- @Entity
- @Getter
- @Table(name = "테이블명")
  - 테이블명은 엔티티 클래스에 's'를 붙여 소문자로 정의한다
- @NoArgsConstructor(access = AccessLevel.PROTECTED)


### 2. 상속 관계
- 엔티티 클래스는 모두 BaseEntity 클래스를 extends 한다
- 예시: public class User extends BaseEntity<User> {...}


### 3. 생성자
- 생성자 정의는 빌더 패턴을 적용한다(@Builder 애노테이션 추가)


### 4. 업데이트 로직
- 엔티티 클래스는 기본적으로 update 메서드를 정의한다.
- 이때 PATCH에 맞게 구현한다. 즉 일부 필드만 파라미터로 들어와도 그 일부 필드만 업데이트할 수 있도록 구현한다.


### 5. 컬럼 정의 및 연관관계 매핑
- 엔티티 필드에는 반드시 컬럼을 정의해주어야 한다. 단, name은 snake_case로 명시한다. ex)  @JoinColumn(name = "user_id")
- @ManyToOne 관계는 지연로딩으로 설정한다. ex)  @ManyToOne(fetch = FetchType.LAZY)
- @OneToMany 관계는 연관관계의 주인을 명시, 기본으로 cascade 관계를 설정한다
  - ex)  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)

### 6. 필드의 비즈니스 로직
- 엔티티 필드 자체에 특별한 비즈니스 로직이 포함되어야 하는 경우에는 해당 필드를 따로 정의한다.
- 예를 들어 선수 엔티티 클래스의 학번 필드의 경우 '9자리 숫자여야 한다'는 규칙에 따라 미리 검증이 필요하므로 별도 클래스로 생성하여 관리한다.


## 매핑 테이블 성격의 엔티티 클래스 구현

### 1. 기본 클래스 레벨 애노테이션 예시: 매핑 관계 명시
```
@Entity
@Getter
@Table(name = "team_players",
uniqueConstraints = {
@UniqueConstraint(
name = "uc_team_player",
columnNames = {"team_id", "player_id"}
)
}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
```
- @UniqueConstraint로 관계 명시
- name은 'uc_첫번째엔티티_두번째엔티티' 형식으로 정의

### 2. 매핑 테이블 엔티티 생성
- 기본 생성자는 private으로 막는다.
- of라는 static 메서드를 통해 객체를 생성하도록 한다.

```
    private TeamPlayer(Team team, Player player, Integer jerseyNumber) {
    this.team = team;
    this.player = player;
    this.jerseyNumber = jerseyNumber;
    }

    public static TeamPlayer of(Team team, Player player, Integer jerseyNumber) {
        TeamPlayer teamPlayer = new TeamPlayer(team, player, jerseyNumber);
        team.addTeamPlayer(teamPlayer);
        player.addTeamPlayer(teamPlayer);
        return teamPlayer;
    }
   
```


## 좋은 엔티티 클래스의 예시 코드

```
@Entity
@Getter
@Table(name = "players")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player extends BaseEntity<Player> {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "student_number", nullable = true, unique = true)
    private String studentNumber;
    
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamPlayer> teamPlayers = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LeagueTopScorer> leagueTopScorers = new ArrayList<>();

    @Builder
    public Player(@NonNull String name, @NonNull String studentNumber) {
        StudentNumber.validate(studentNumber);
        this.name = name;
        this.studentNumber = studentNumber;
    }

    public void update(String name, String studentNumber) {
        StudentNumber.validate(studentNumber);
        this.name = name;
        this.studentNumber = studentNumber;
    }

---

public class StudentNumber {
    
    private static final int ADMISSION_YEAR_START_INDEX = 2;
    private static final int ADMISSION_YEAR_END_INDEX = 4;
    private static final String STUDENT_NUMBER_PATTERN = "^[0-9]{9}$";
    
    private StudentNumber() {}

    public static boolean isInvalid(String studentNumber) {
        return studentNumber == null || !studentNumber.matches(STUDENT_NUMBER_PATTERN);
    }

    public static void validate(String studentNumber) {
        if (isInvalid(studentNumber)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "학생번호는 9자리 숫자여야 합니다.");
        }
    }

    public static String extractAdmissionYear(String studentNumber) {
        if (isInvalid(studentNumber)) return null;
        return studentNumber.substring(ADMISSION_YEAR_START_INDEX, ADMISSION_YEAR_END_INDEX);
    }
}

```

## 레포지토리 클래스 구현
- 엔티티 클래스를 정의하고 나면 그 도메인 클래스의 레포지토리 인터페이스도 해당 /domain 패키지에 정의해야 한다.
- 레포지토리 클래스명은 엔티티 클래스명 + Repository로 한다. ex) PlayerRepository
- 레포지토리 클래스는 JpaRepository를 상속받는다.
  - ex)  public interface PlayerRepository extends JpaRepository<Player, Long>

