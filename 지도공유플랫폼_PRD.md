# 협업 지도 플랫폼 PRD

## 📋 목차
- [개요](#개요)
- [제품 목표](#제품-목표)
- [핵심 가치](#핵심-가치)
- [타깃 사용자](#타깃-사용자)
- [핵심 기능](#핵심-기능)
- [기술 아키텍처](#기술-아키텍처)
- [개발 환경](#개발-환경)
- [로드맵](#로드맵)
- [성공 지표](#성공-지표)
- [리스크 관리](#리스크-관리)
- [QA 및 배포](#qa-및-배포)

## 📱 개요

커플, 가족, 친구 등 **소규모 그룹**이 실시간으로 하나의 지도를 공유하며, 마커·메모·경로·사진 등을 공동 편집하는 모바일 협업 지도 플랫폼입니다.

- 기존 네이버/카카오/구글 지도: 위치 공유만 지원, 실시간 공동 편집 불가.
- 우리만의 "실시간 협업 지도" 경험 제공 (여행 플래너, 일기장 컨셉 포함).

## 🎯 제품 목표

| 목표 | 측정 기준 | 달성 기한 |
|------|-----------|-----------|
| MVP 출시 | Play Store 베타 배포, 핵심 기능 95% 이상 버그-프리 | T0 + 4개월 |
| 활성 사용자 확보 | WAU 3,000명, 지도 당 평균 3인 협업 | T0 + 6개월 |
| 동기화 품질 보장 | 1초 이내 UI 반영, 충돌 오류율 0.1% 이하 | 지속 |

## 💎 핵심 가치

1. **실시간 공동 편집**: 지도, 마커, 메모, 경로, 사진 동시 편집 즉시 동기화
2. **그룹 중심 협업 경험**: 초대 코드/링크, 권한 관리, 로그 추적
3. **여행 플래너 통합**: 달력‧체크리스트‧메모를 지도와 1화면에 통합

## 👥 타깃 사용자

| 페르소나 | 특징 | 핵심 니즈 |
|----------|------|-----------|
| 커플A&B | 20대 직장인, 데이트/여행 | 실시간 위치·일정 공유, 추억 기록 |
| 가족C | 4인 가족, 여행·약속 | 경로·숙소·체크리스트 |
| 친구D팀 | 대학 동아리, 여행 | 역할 분담, 변경 알림 |

### 주요 유저 플로우

1. 앱 설치 및 회원가입/로그인
2. 새 지도 생성 또는 초대 코드/링크 참여
3. 지도 진입 후 마커/메모/사진/경로 추가
4. 실시간 편집 내용 확인
5. 달력, 체크리스트 등 부가 기능 활용
6. 테마/알림 등 개인화 설정

## ⚡ 핵심 기능

### 🗺️ 공유 지도 및 초대

- 그룹 단위 지도 생성, 초대 링크/코드 발급
- 소유자/에디터/뷰어 등 권한 설정
- 즐겨찾기 기능(개인/공용)

### 📍 지도 편집·동기화

- 마커/경로(폴리라인)/메모/사진 추가·수정·삭제
- 모든 변경 실시간 양방향 동기화
- 실시간 위치 공유(원할 때 커스텀)

### 📅 부가 기능

- 달력(방문 일정, 계획 기록, TimeTree 스타일 UI)
- 메모/체크리스트/플래너(AI 기반 추천 포함)
- 변경 이력 관리, 알림(푸시, Log)

### 🔐 권한 및 보안

- 3단계 권한(소유자/에디터/뷰어)
- 초대 링크 만료/비밀번호 옵션
- OAuth(카카오/네이버/구글) 인증
- 데이터 암호화 및 접근제어

## 🏗️ 기술 아키텍처

### 💻 기술 스택

| 계층 | 기술/도구 | 비고 |
|------|-----------|------|
| 프론트엔드 | Kotlin (JDK21), Jetpack Compose | Android 전용, Material3, 다크모드 |
| 실시간 DB | Firebase Realtime DB & Firestore | 동시 사용, 트랜잭션/확장성 |
| 서버리스 | Firebase Cloud Functions(Node.js) | 권한, 알림, 데이터 검증 |
| 지도 SDK | 네이버지도, Google Maps(추후) | API Key 분리 관리 |
| 인증 | Firebase Authentication + OAuth | 카카오/네이버/구글 계정 연동 |
| 알림 | Firebase Cloud Messaging | 토픽 및 개별 푸시 |
| 스토리지 | Firebase Storage | 사진, 파일 첨부 저장 |
| CI/CD | Gradle, GitHub Actions, Firebase CLI, Fastlane | 배포 자동화, 스테이징/프로덕션 분리 |

### 데이터 모델 개요

```kotlin
// 지도
data class Map(
    val mapId: String,
    val name: String,
    val ownerUid: String,
    val createdAt: Timestamp,
    val members: List
)
// 마커
data class Marker(
    val markerId: String,
    val mapId: String,
    val lat: Double,
    val lng: Double,
    val title: String,
    val description: String,
    val color: String,
    val photos: List,
    val favorites: List,
    val updatedAt: Timestamp
)
// 메모/일정/체크리스트
data class MemoItem(
    val itemId: String,
    val mapId: String,
    val type: ItemType, // MEMO, PLAN, CHECKLIST
    val content: String,
    val status: Boolean,
    val dueDate: Timestamp?
)
```

### 실시간 동기화 흐름

1. 클라이언트→StateFlow 상태 변경
2. Firestore batch 또는 Realtime DB 반영
3. Cloud Functions(권한·트리거 검증)
4. 실시간 이벤트 전파→UI 렌더링

## 🛠️ 개발 환경

- 코드 저장소: GitHub
- 이슈/프로젝트 관리: GitHub Projects
- 문서: Notion, README, API(Swagger 등)
- 디자인: Figma(chestnutMap.pdf 참고)
- IDE: IntelliJ/Android Studio
- 커밋 컨벤션: Conventional Commits
- 브랜치 전략: Git Flow
- 코드 리뷰: PR 기반
- 코드 품질: Ktlint, Detekt
- 환경 변수: .env 파일
- 협업: Notion, Figma

### 테스트 전략

- 단위: JUnit, Mockk
- UI: Jetpack Compose UI Test, Espresso
- E2E: UI Automator, Firebase Emulator Suite
- 성능: 동기화/편집 동작 200ms 이내

## 🗓️ 로드맵

| 단계 | 기간 | 목표 |
|------|------|------|
| T0 | 0–2주 | 요구사항 확정, Figma, 프로젝트 세팅 |
| M1 | 3–8주 | 지도 표시, 마커 CRUD, Auth, 초대 |
| M2 | 9–12주 | Realtime Sync, StateFlow, 변경로그, 푸시 |
| M3 | 13–16주 | 달력·체크리스트, MVP 베타 |
| M4 | 17–20주 | 성능/보안 강화, 베타 지표 수집 |
| M5 | 21–24주 | 경로 그리기, 사진, Google Maps 지원 등 |

### 확장 기능(Phase 2)

- 파일/사진 첨부, 경로 그리기
- 마커/메모에 댓글, 이모지, 색상 등
- 자동 위치 기록, 방문 통계, AI 추천, 웹/위젯/외부 캘린더 연동

## 📈 성공 지표 (KPI)

- 사용자당 지도 생성 1.5건 이상
- 동시 편집 충돌 오류율 < 0.1%
- 주간 재방문률 40% 이상
- 알림 클릭율 15% 이상
- 서비스가동률 99.5%+, UI변경 반영 1초 이내

## ⚠️ 리스크 관리

| 리스크 | 심각도 | 대응 전략 |
|--------|--------|-----------|
| 지도 API 사용량 초과 | 높음 | 쿼터 모니터링, SDK분산, 캐싱 |
| 데이터 충돌/유실 | 높음 | Firestore 버전 필드, 충돌 관리 알고리즘 |
| 개인정보 유출 | 중간 | 최소 수집/암호화/권한 강화 |
| 일정 지연 | 중간 | 리뷰·MVP범위 고정·버퍼 확보 |

- 보안: HTTPS 강제, API Key 암호화, Jetpack Security, 초대 링크 만료 및 패스워드

## 🚀 QA 및 배포 정책

1. 단위(JUnit, Mockk), UI, E2E(Emulator Suite) 테스트
2. PR→Static Analysis→Unit Test→빌드 아티팩트
3. QA 승인 후 스테이징→프로덕션 배포(Fastlane)
4. 운영: Crashlytics, Sentry, 로그 정책, 데이터 백업

## 🌍 국제화·접근성·오픈소스

- 다국어(strings.xml), 다크모드, TalkBack/VoiceOver 지원, Material3 컬러 콘트라스트
- 오픈소스: Apache 2.0/MIT 우선(GPL 불가), 6개월 마다 감사, 최신화 유지

## 📞 문의 및 변경 요청

- PO/Tech Lead: [이름/이메일 입력]
- 변경 요청: GitHub Issue 등록, 주간 PRD 리뷰 승인 필요

**최종 업데이트: 2025.01.23 / 문서 버전: v1.0**