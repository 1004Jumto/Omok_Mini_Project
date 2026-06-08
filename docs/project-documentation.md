# 프로젝트 전체 문서화

## 프로젝트 개요

Omok Mini Project는 Java Servlet/JSP와 Java WebSocket API를 활용해 구현한 실시간 오목 웹 서비스입니다. 사용자는 회원 또는 게스트로 로그인한 뒤 로비에서 방을 생성하거나 빠른 입장으로 대국에 참여할 수 있습니다. 게임방에서는 착수, 채팅, 관전, 카운트다운, 제한 시간, 게임 종료, 랭킹 갱신이 WebSocket을 통해 실시간으로 동기화됩니다.

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Language | Java 17, JavaScript |
| Backend | Servlet 4.0, JSP/JSTL, Java WebSocket API |
| Frontend | JSP, HTML, CSS, Vanilla JavaScript |
| Database | PostgreSQL, JDBC |
| Build | Maven, WAR |
| Server | Apache Tomcat 9 |
| Library | Jackson, Lombok, JUnit 5 |

## 주요 기능

### 사용자 및 인증

- 회원가입 및 로그인
- 게스트 로그인
- `HttpSession` 기반 로그인 사용자 관리
- WebSocket Handshake 시 HTTP 세션의 사용자 정보를 WebSocket 세션으로 전달

### 로비

- 방 목록 조회
- 방 생성
- 가장 오래된 대기 방 기준 빠른 입장
- 로비 채팅
- 랭킹 TOP 10 조회
- 방 생성/삭제 및 랭킹 변경 시 WebSocket 기반 실시간 갱신

### 게임방

- 플레이어 2명 입장 시 자동 게임 준비
- 5초 카운트다운 후 게임 시작
- 15x15 오목판 렌더링
- 서버 권위 기반 착수 처리
- 30초 턴 제한
- 플레이어/관전자 채팅
- 관전자 입장 및 현재 보드 스냅샷 전달
- 승리, 무승부, 시간 초과, 플레이어 이탈에 따른 게임 종료 처리
- 게임 종료 후 전적 및 랭킹 갱신

## 아키텍처

```text
Client
  JSP / CSS / JavaScript
  - lobby.jsp
  - room.jsp
  - websocket.js
  - game.js
  - game_ui.js

Servlet Controller
  - LoginServlet
  - RegisterServlet
  - LobbyServlet
  - RoomServlet
  - LogoutServlet

WebSocket Endpoint
  - LobbyWebSocket   (/ws/lobby)
  - GameWebSocket    (/ws/game/{roomId})

Service
  - UserService
  - RoomService
  - RoomBroadcaster

Manager
  - RoomManager

Domain
  - Room
  - Game
  - GameState
  - OmokRule
  - MoveResult

Repository
  - UserDAO
  - RecordDAO

Database
  - PostgreSQL
```

프로젝트는 Servlet/JSP 기반 MVC 구조 위에 WebSocket 엔드포인트를 분리해 실시간 기능을 추가했습니다. HTTP는 로그인, 화면 이동, 방 생성, 방 입장처럼 요청-응답이 명확한 기능을 담당하고, WebSocket은 로비 갱신과 게임방 이벤트 동기화를 담당합니다.

## 실시간 통신 설계

### 로비 WebSocket

`LobbyWebSocket`은 `/ws/lobby` 엔드포인트로 접속한 모든 로비 사용자의 WebSocket 세션을 관리합니다.

| 메시지 | 방향 | 설명 |
| --- | --- | --- |
| `CONNECTED` | Server -> Client | 로비 연결 성공 응답 |
| `ROOM_LIST` | Server -> Client | 현재 방 목록 브로드캐스트 |
| `RANKING` | Server -> Client | 랭킹 TOP 10 브로드캐스트 |
| `CHAT` | 양방향 | 로비 채팅 |
| `REQUEST_ROOM_LIST` | Client -> Server | 방 목록 재요청 |
| `REQUEST_RANKING` | Client -> Server | 랭킹 재요청 |
| `PROFILE_UPDATE` | Server -> Client | 프로필 변경 알림 |

방이 생성되거나 삭제되면 `RoomManager`에서 `LobbyWebSocket.broadcastRoomList()`를 호출해 로비 방 목록을 갱신합니다. 게임 종료 후 전적이 변경되면 `LobbyWebSocket.broadcastRanking()`을 호출해 랭킹을 다시 전송합니다.

### 게임방 WebSocket

`GameWebSocket`은 `/ws/game/{roomId}` 엔드포인트로 게임방 단위 실시간 통신을 처리합니다.

```text
ws://localhost:8080/omok/ws/game/{roomId}?role=player
ws://localhost:8080/omok/ws/game/{roomId}?role=spectator
```

`role` 쿼리 파라미터로 플레이어와 관전자를 구분합니다. 서버는 관전자 세션의 착수 요청을 차단하고 `SPECTATOR_CANNOT_MOVE` 에러를 반환합니다.

| 메시지 | 방향 | 설명 |
| --- | --- | --- |
| `JOIN` | Server -> Client | 사용자 입장 알림 |
| `LEAVE` | Server -> Client | 사용자 퇴장 알림 |
| `ROOM_MEMBERS` | Server -> Client | 현재 플레이어 목록 전달 |
| `BOARD_SNAPSHOT` | Server -> Client | 관전자/재접속자용 현재 보드 상태 |
| `ROOM_READY` | Server -> Client | 2인 입장 완료 |
| `COUNTDOWN` | Server -> Client | 게임 시작 전 5초 카운트다운 |
| `GAME_START` | Server -> Client | 색상, 선공, 역할 정보 전달 |
| `MOVE` | Client -> Server | 착수 요청 |
| `MOVE_OK` | Server -> Client | 착수 확정 및 보드 반영 |
| `GAME_END` | Server -> Client | 게임 종료 |
| `CHAT` | 양방향 | 게임방 채팅 |
| `ERROR` | Server -> Client | 잘못된 요청 또는 권한 오류 |

## 방 관리 구조

방은 `RoomManager`의 `ConcurrentHashMap<String, Room>`에 저장됩니다. 서버 프로세스가 실행되는 동안 방 상태를 메모리에서 관리하며, 게임 종료 시 방을 제거합니다.

```text
WAIT
  -> READY       두 플레이어의 HTTP 입장 및 WebSocket 연결 완료
  -> COUNTDOWN   5초 카운트다운 시작
  -> PLAYING     Game 생성 및 GAME_START 브로드캐스트
  -> END         승리, 무승부, 시간 초과, 플레이어 이탈
```

### 핵심 클래스

- `RoomManager`: 방 생성, 조회, 삭제, 대기 방 목록 조회, 빠른 입장 대상 방 조회
- `Room`: 플레이어 목록, 플레이어 WebSocket 세션, 관전자 세션, 방 상태 전이, 카운트다운, 게임 시작/종료 관리
- `RoomService`: WebSocket 입장/퇴장, 착수, 채팅, 게임 종료 후처리 담당
- `RoomBroadcaster`: 플레이어, 관전자, 방 전체, 특정 세션 단위 메시지 전송

## 게임 진행 흐름

1. 사용자가 로비에서 방을 생성합니다.
2. `RoomManager`가 UUID 기반 `roomId`를 생성하고 방장을 첫 번째 플레이어로 등록합니다.
3. 다른 사용자가 플레이어로 입장하면 `Room.players`에 추가됩니다.
4. 두 플레이어가 게임방 WebSocket에 연결되면 방 상태가 `READY`가 됩니다.
5. 서버가 `COUNTDOWN` 메시지를 5초 동안 브로드캐스트합니다.
6. `Game`과 `GameState`가 생성되고 흑/백 플레이어 정보가 전송됩니다.
7. 클라이언트는 보드 클릭 시 `MOVE` 메시지를 서버로 전송합니다.
8. 서버는 `OmokRule`로 착수 가능 여부, 승리, 무승부, 시간 초과를 판정합니다.
9. 정상 착수는 `MOVE_OK`로 방 전체에 브로드캐스트됩니다.
10. 게임 종료 시 `GAME_END`를 전송하고 전적/랭킹을 갱신한 뒤 방을 제거합니다.

## 서버 권위 기반 게임 상태

클라이언트는 보드 클릭과 UI 표시를 담당하지만, 실제 게임 상태는 서버의 `GameState`가 관리합니다.

- 보드 크기: 15x15
- 현재 턴: `Stone.BLACK`, `Stone.WHITE`
- 게임 상태: `READY`, `IN_PROGRESS`, `FINISHED`
- 흑/백 사용자 ID
- 승자 ID
- 턴 제한 시간: 30초
- 관전자용 보드 스냅샷

이 구조를 통해 클라이언트 조작 여부와 관계없이 최종 착수 판정과 게임 결과는 서버 기준으로 결정됩니다.

## 데이터베이스

PostgreSQL과 JDBC를 사용합니다.

| 테이블 | 용도 |
| --- | --- |
| `users` | 사용자 계정, 닉네임, 프로필 이미지 |
| `record` | 레이팅, 승리 수, 패배 수, 갱신 시각 |

전적 정책:

- 승리: `rating + 15`, `win_count + 1`
- 패배: `rating - 10`, `lose_count + 1`
- 패배 시 레이팅은 `GREATEST(0, rating - 10)`으로 0 미만 방지
- 랭킹은 rating 내림차순 TOP 10 조회

## 실행 방법

### 요구 사항

- JDK 17
- Maven
- Apache Tomcat 9
- PostgreSQL

### 빌드

```bash
./mvnw clean package
```

Windows:

```bash
mvnw.cmd clean package
```

빌드 결과물:

```text
target/Omok_Mini_Project-1.0-SNAPSHOT.war
```

Tomcat 배포 시 context path를 `/omok`으로 맞추면 JSP와 JavaScript에서 사용하는 경로와 일치합니다.

## 개선 포인트

- DB 접속 정보를 환경 변수 또는 JNDI DataSource로 분리
- 비밀번호 해싱 적용
- WebSocket 메시지 스키마 검증 강화
- 게임방 카운트다운 Thread를 `ScheduledExecutorService` 기반으로 통일
- 방 상태 및 게임 상태를 Redis 등 외부 저장소로 분리해 서버 확장성 확보
- 전적 업데이트 트랜잭션 처리 강화
- 테스트 코드를 Maven 표준 테스트 경로(`src/test/java`)로 이동
