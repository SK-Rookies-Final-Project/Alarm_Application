# 🚨 Kafka 보안 알람 서버

Kafka 토픽에서 보안 이벤트를 실시간으로 모니터링하고 관리자에게 이메일 알람을 발송하는 Spring Boot 애플리케이션입니다.

## 📋 주요 기능

- **실시간 Kafka 토픽 모니터링**: 4개의 보안 관련 토픽을 실시간으로 구독
- **SCRAM 인증**: Kafka 브로커에 SCRAM-SHA-512 인증으로 안전한 연결
- **자동 이메일 알람**: 보안 이벤트 발생 시 담당자에게 자동 이메일 발송
- **담당자별 알람 분류**: 시스템 레벨과 리소스 레벨로 알람 분류

## 🎯 모니터링 대상 토픽

| 토픽명 | 담당자 | 이메일 주소 | 설명 |
|--------|--------|-------------|------|
| `system-level-false` | 시스템 개발자 | tgsduser@gmail.com | 시스템 레벨 보안 이벤트 |
| `resource-level-false` | 유저/리소스 개발자 | tgurd123@gmail.com | 리소스 레벨 보안 이벤트 |
| `certified-2time` | 유저/리소스 개발자 | tgurd123@gmail.com | 인증 2회 시도 이벤트 |
| `certified-notMove` | 유저/리소스 개발자 | tgurd123@gmail.com | 인증 후 미이동 이벤트 |

## 🔧 설정

### 환경 변수 설정 (.env)

```bash
# Kafka 브로커 설정
KAFKA_BOOTSTRAP_SERVERS=localhost:29092,localhost:39092,localhost:49092

# Kafka 관리자 계정 (SCRAM 인증)
KAFKA_ADMIN_USERNAME=admin
KAFKA_ADMIN_PASSWORD=admin-secret

# Consumer 그룹 ID
CONSUMER_GROUP_ID=alarm-server-group

# 모니터링 토픽
KAFKA_TOPIC_SYSTEM_LEVEL_FALSE=system-level-false
KAFKA_TOPIC_RESOURCE_LEVEL_FALSE=resource-level-false
KAFKA_TOPIC_CERTIFIED_2TIME=certified-2time
KAFKA_TOPIC_CERTIFIED_NOTMOVE=certified-notMove

# 이메일 설정 (Gmail SMTP)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true

# 알람 수신자
SYSTEM_DEVELOPER_EMAIL=tgsduser@gmail.com
USER_RESOURCE_DEVELOPER_EMAIL=tgurd123@gmail.com

# 서버 포트
SERVER_PORT=8081
```

### Gmail 설정

1. Gmail 계정에서 2단계 인증 활성화
2. 앱 비밀번호 생성
3. `.env` 파일의 `SPRING_MAIL_USERNAME`과 `SPRING_MAIL_PASSWORD`에 설정

## 🚀 실행 방법

### 1. 의존성 설치 및 빌드

```bash
./gradlew build
```

### 2. 환경 변수 설정

`.env` 파일을 프로젝트 루트에 생성하고 위의 설정값들을 입력합니다.

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는

```bash
java -jar build/libs/alarm-1.0.0.jar
```

## 📊 API 엔드포인트

### 헬스체크

```bash
GET http://localhost:8081/api/health
```

응답 예시:
```json
{
  "status": "UP",
  "service": "Alarm Server",
  "timestamp": "2024-01-15 14:30:25",
  "port": "8081"
}
```

### 설정 정보 조회

```bash
GET http://localhost:8081/api/config
```

응답 예시:
```json
{
  "kafkaBootstrapServers": "localhost:29092,localhost:39092,localhost:49092",
  "systemDeveloperEmail": "tgsduser@gmail.com",
  "userResourceDeveloperEmail": "tgurd123@gmail.com",
  "monitoringTopics": [
    "system-level-false",
    "resource-level-false",
    "certified-2time",
    "certified-notMove"
  ]
}
```

## 📧 이메일 알람 형식

### 시스템 레벨 알람
- **제목**: `[🚨 시스템 보안 알람] {토픽명} 토픽에서 보안 이벤트 감지`
- **수신자**: 시스템 개발자 (tgsduser@gmail.com)

### 리소스/사용자 레벨 알람
- **제목**: `[⚠️ 리소스/사용자 보안 알람] {토픽명} 토픽에서 보안 이벤트 감지`
- **수신자**: 유저/리소스 개발자 (tgurd123@gmail.com)

## 🔍 로그 모니터링

애플리케이션 실행 중 다음과 같은 로그를 확인할 수 있습니다:

```
2024-01-15 14:30:25 INFO  - 🚨 [SYSTEM LEVEL] 보안 이벤트 수신 - 토픽: system-level-false, 파티션: 0, 오프셋: 123
2024-01-15 14:30:25 INFO  - 수신 데이터: {"userId": "admin", "action": "unauthorized_access", "timestamp": "2024-01-15T14:30:25"}
2024-01-15 14:30:26 INFO  - 시스템 레벨 보안 알람 발송 완료: system-level-false -> tgsduser@gmail.com
2024-01-15 14:30:26 INFO  - ✅ 시스템 레벨 보안 알람 처리 완료: system-level-false
```

## 🛠️ 기술 스택

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Kafka**
- **Spring Mail**
- **Lombok**
- **Gradle**

## 🔒 보안 고려사항

- Kafka 브로커와의 통신은 SCRAM-SHA-512 인증을 사용
- 이메일 발송 시 Gmail SMTP with TLS 사용
- 환경 변수를 통한 민감한 정보 관리
- 비동기 이메일 발송으로 성능 최적화

## 📞 문의

- 시스템 관련: tgsduser@gmail.com
- 리소스/사용자 관련: tgurd123@gmail.com
