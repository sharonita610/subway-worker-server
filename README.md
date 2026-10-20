# Subway Worker Server

출퇴근길 위치를 기반으로 동료를 포획하고 성장시키는 수집형 게임의 백엔드입니다. 토스 미니앱으로 운영했으며 현재는 운영을 종료했습니다.

## 기술 스택

- Java 17, Spring Boot 3.3, Spring Security, Spring Data JPA (MySQL), Spring Data Redis
- springdoc OpenAPI, Lombok, JUnit 5 + Mockito, Gradle
- 운영 당시 인프라: AWS EC2 · RDS · ElastiCache · ECR, Parameter Store, Secrets Manager, GitHub Actions

## 핵심 기능

| 영역 | 내용 |
|---|---|
| GPS | 사용자 주변 3×3 grid에 동료·선물상자를 생성하고(Redis GEO, grid 단위 락) 반경 1km 안을 조회 |
| 포획 · 트랩 | 도구 확률 × 동료 레벨 확률로 판정. 트랩은 스케줄러가 매시 반경 안의 동료를 포획 처리 |
| 성장 | 동료 강화, 스킬카드 강화, 스테이지 · 던전 클리어 보상 |
| 경제 | 동료 · 스킬카드 장터, 가챠, 상점, 구독 · 출석 보상 메일 |
| 인증 | 토스 로그인(mTLS) 후 발급한 UUID를 `X-User-ID` 헤더로 전달 |

API 62개, 테스트 108개.

## 구조

```
controller/      컨트롤러와 요청 · 응답 DTO (도메인별 패키지)
service/
  common/        UserFinder, UserLevelProcessor, RewardProcessor (모든 도메인이 공유)
  user coworker skill market gacha gps stage store system
domain/          enum으로 표현한 게임 규칙 (확률표, 보상표, 레벨표)
entity/ repository/   JPA
config/          Security (X-User-ID 필터, 공개 경로 목록), Swagger, Redis, JPA Auditing
```

의존 방향은 `common ← 각 도메인`, `coworker ← user · market · stage`, `skill ← market`으로 한 방향이며 순환이 없습니다.

## 실행

```bash
./gradlew test       # 단위 · 슬라이스 테스트 108개
./gradlew bootRun    # MySQL, Redis 필요
```

로컬 실행에 필요한 환경변수: `LOCALHOST_USER`, `LOCAL_DB_PASSWORD`, `REDIS_HOST`

Swagger UI는 `http://localhost:8080/swagger-ui.html` 에서 열리며, Authorize 버튼에 `X-User-ID` 값을 넣으면 보호된 API를 호출할 수 있습니다.

## 테스트 전략

- **확률 로직은 통계 테스트로 검증**합니다. 경계값(항상 성공 · 항상 실패)은 정확히, 확률은 1만 회 표본이 기대값 ±3%p 안에 들어오는지 확인합니다.
