# 백엔드

저장만 하고 다시 보지 않는 링크를 다시 행동으로 이어지게 만드는 북마크 리마인드 서비스의 백엔드입니다.  
북마크, 체크리스트, AI 알림, SSE 실시간 전송, 점수/레벨 시스템을 Spring Boot 기반 API로 제공합니다.

## 팀원 및 역할 분담

| 이름 | GitHub | 담당 영역 | 상세 기여 |
| --- | --- | --- | --- |
| 최정인 | [ch0iii](https://github.com/ch0iii) | CI/CD, 인증, 홈 화면 | GitHub Actions 기반 빌드/검증 파이프라인 구성, JWT 로그인 및 인증 흐름 구현, 홈 화면 API와 오늘의 아카이브/태그 컬렉션 조회 기능 구현 |
| 김서윤 | [SY0518](https://github.com/SY0518) | 북마크 도메인 | 북마크 등록/목록/상세/수정/삭제 API 구현, 태그·리마인드·조회수 연동 처리, 사용자 소유권 검증과 응답 DTO 구성 |
| 양은서 | [yepot](https://github.com/yepot) | 체크리스트, 알림 | 체크리스트 CRUD 및 체크 토글 구현, OpenAI 기반 리마인드 알림 생성 로직 설계, 스케줄러·알림 내역 조회·SSE 실시간 전송 기능 구현 |

## System Architecture
<img width="893" height="493" alt="image" src="https://github.com/user-attachments/assets/7d0599ee-0fb6-4330-8ec5-bab62189e116" />


## 주요 기능

- 회원 인증
  - 회원가입, 로그인, 내 정보 조회, 회원탈퇴
  - JWT 기반 인증/인가
- 북마크 관리
  - 북마크 등록, 목록 조회, 상세 조회, 수정, 삭제
  - 북마크 재방문 처리와 조회수 증가
  - 태그, 리마인드 시간 기반 관리
- 체크리스트 관리
  - 북마크별 체크리스트 등록, 수정, 삭제
  - 체크/해제 처리
- AI 리마인드 알림
  - `bookmark.remind_at` 시점에 OpenAI API로 알림 제목/내용 생성
  - 체크리스트가 남아 있으면 5분 간격으로 추가 리마인드 생성
  - 북마크별 알림 내역 묶음 조회
  - SSE를 통한 실시간 알림 전송
- 점수
  - 링크 저장, 태그 설정, 리마인드 설정, 체크리스트 완료 등 행동별 점수 적립
  - 누적 점수 조회
  - 점수에 따른 레벨 산정
- 홈/마이페이지
  - 오늘의 아카이브 추천
  - 태그별 컬렉션 요약
  - 내 점수, 레벨, 저장/완료 북마크 수, 전체 조회 수 조회

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Build Tool | Gradle 9 |
| Web | Spring Web, Validation |
| Database | Spring Data JPA, PostgreSQL |
| Security | Spring Security, JWT (`jjwt`) |
| API Docs | SpringDoc OpenAPI, Swagger UI |
| AI | OpenAI Chat Completions API |
| Realtime | SSE (`SseEmitter`) |
| Test | JUnit 5, Spring Boot Test, Spring Security Test |

## 실행 방법

### 1. 실행 환경

- JDK 17
- PostgreSQL
- Gradle Wrapper 사용 가능 환경

### 2. 환경 설정

기본 실행 프로필은 `local`입니다.

데이터베이스 정보는 `src/main/resources/application-local.yml`에서 로컬 환경에 맞게 수정하거나, 별도 프로필/환경변수로 주입해서 사용하면 됩니다.

AI 알림 기능까지 함께 테스트하려면 아래 환경변수를 설정하세요.

```bash
export JWT_SECRET="your-jwt-secret-key-at-least-32-bytes"
export OPENAI_API_KEY="your-openai-api-key"
export OPENAI_MODEL="gpt-5-mini"
export OPENAI_BASE_URL="https://api.openai.com/v1"
```

OpenAI API 키 없이 기본 기능만 확인하려면 스케줄러를 꺼두는 것을 권장합니다.

```bash
export NOTIFICATION_SCHEDULER_ENABLED=false
```

추가로 사용할 수 있는 알림 관련 환경변수:

```bash
export NOTIFICATION_REMINDER_INTERVAL_MINUTES=5
export NOTIFICATION_SCHEDULER_FIXED_DELAY_MS=60000
```

### 3. 애플리케이션 실행

의존성 설치 및 빌드:

```bash
./gradlew clean build
```

로컬 서버 실행:

```bash
./gradlew bootRun
```

테스트 실행:

```bash
./gradlew test
```

### 4. 접속 주소

- Swagger UI: `http://localhost:8080/swagger-ui.html`, `https://cokerthon-team2.p-e.kr/swagger-ui.html`
- OpenAPI Docs: `http://localhost:8080/v3/api-docs`, `https://cokerthon-team2.p-e.kr/v3/api-docs`

## 주요 API

| 기능 | 메서드 | URL |
| --- | --- | --- |
| 회원가입 | `POST` | `/api/auth/signup` |
| 로그인 | `POST` | `/api/auth/login` |
| 내 정보 조회 | `GET` | `/api/auth/me` |
| 북마크 등록 | `POST` | `/api/bookmarks` |
| 북마크 목록 조회 | `GET` | `/api/bookmarks` |
| 북마크 상세 조회 | `GET` | `/api/bookmarks/{bookmarkId}` |
| 체크리스트 등록 | `POST` | `/api/bookmarks/{bookmarkId}/checklists` |
| 체크리스트 체크/해제 | `POST` | `/api/bookmarks/{bookmarkId}/checklists/{checklistId}/check` |
| 홈 조회 | `GET` | `/api/home` |
| 알림 내역 조회 | `GET` | `/api/notifications` |
| 알림 생성 | `POST` | `/api/notifications` |
| 알림 SSE 연결 | `GET` | `/api/notifications/stream` |
| 점수 조회 | `GET` | `/api/score` |
| 마이페이지 조회 | `GET` | `/api/mypage` |

## 알림 기능 참고

- 스케줄러는 `@EnableScheduling` 기반으로 동작합니다.
- 기본 설정상 `notification.scheduler.enabled=true`이며, 1분마다 리마인드 대상 북마크를 확인합니다.
- 알림 생성 시점에 미완료 체크리스트가 없으면 추가 알림을 만들지 않습니다.
- 실시간 알림은 SSE로 전송되며, 연결이 끊긴 경우 `GET /api/notifications`로 누락된 내역을 조회할 수 있습니다.

## 프로젝트 구조

```text
src/main/java/com/hackathon
├── domain
│   ├── bookmark
│   ├── checklist
│   ├── home
│   ├── member
│   ├── mypage
│   ├── notification
│   └── score
└── global
    ├── config
    ├── exception
    └── security
```

## 비고

- 인증이 필요한 API는 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.
- 로컬 개발 시 실제 DB 계정, JWT 시크릿, OpenAI API 키는 코드에 고정하지 말고 환경변수 또는 별도 설정 파일로 관리하는 것을 권장합니다.
