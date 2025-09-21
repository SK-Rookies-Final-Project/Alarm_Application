# 📧 이메일 설정 가이드

알람 서버에서 Gmail을 통해 이메일을 발송하기 위한 설정 방법입니다.

## 🔐 Gmail 앱 비밀번호 생성

### 1. Gmail 2단계 인증 활성화
1. Google 계정 관리 페이지로 이동: https://myaccount.google.com/
2. **보안** 탭 클릭
3. **2단계 인증** 설정 (이미 설정되어 있다면 다음 단계로)

### 2. 앱 비밀번호 생성
1. Google 계정 관리 > **보안** > **2단계 인증** 클릭
2. 하단의 **앱 비밀번호** 클릭
3. **앱 선택**에서 "기타(맞춤 이름)" 선택
4. 이름에 "Alarm Server" 입력
5. **생성** 클릭
6. 생성된 16자리 비밀번호를 복사 (예: `abcd efgh ijkl mnop`)

## ⚙️ 환경 변수 설정

`.env` 파일에서 다음 값들을 수정하세요:

```bash
# 발송할 Gmail 계정 정보
SPRING_MAIL_USERNAME=your-actual-email@gmail.com
SPRING_MAIL_PASSWORD=abcd efgh ijkl mnop  # 위에서 생성한 앱 비밀번호

# 알람 수신자 (필요시 변경)
SYSTEM_DEVELOPER_EMAIL=tgsduser@gmail.com
USER_RESOURCE_DEVELOPER_EMAIL=tgurd123@gmail.com
```

## 🧪 이메일 발송 테스트

### 방법 1: 직접 Kafka 메시지 발송
Kafka Producer를 사용하여 테스트 메시지를 발송합니다:

```bash
# system-level-false 토픽에 테스트 메시지 발송
kafka-console-producer --bootstrap-server localhost:29092 \
  --topic system-level-false \
  --property "security.protocol=SASL_PLAINTEXT" \
  --property "sasl.mechanism=SCRAM-SHA-512" \
  --property "sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username=\"admin\" password=\"admin-secret\";"
```

메시지 입력 예시:
```json
{"timestamp":"2024-01-15T14:30:25","level":"CRITICAL","message":"Unauthorized system access detected","userId":"admin","ip":"192.168.1.100"}
```

### 방법 2: 로그 확인
알람 서버 로그에서 이메일 발송 결과를 확인합니다:

```bash
# 성공 로그 예시
2024-01-15 14:30:26 INFO  - 이메일 발송 성공: [🚨 시스템 보안 알람] system-level-false 토픽에서 보안 이벤트 감지 -> tgsduser@gmail.com

# 실패 로그 예시
2024-01-15 14:30:26 ERROR - 이메일 발송 실패: [🚨 시스템 보안 알람] system-level-false 토픽에서 보안 이벤트 감지 -> tgsduser@gmail.com, 오류: Authentication failed
```

## 🚨 문제 해결

### 1. "Authentication failed" 오류
- Gmail 앱 비밀번호가 올바른지 확인
- 2단계 인증이 활성화되어 있는지 확인
- 앱 비밀번호에 공백이 포함되어 있어도 괜찮음

### 2. "Connection timeout" 오류
- 방화벽에서 587 포트가 차단되어 있는지 확인
- 네트워크 연결 상태 확인

### 3. 이메일이 스팸함으로 분류되는 경우
- Gmail 발송 계정을 신뢰할 수 있는 발송자로 추가
- 이메일 제목과 내용이 스팸 필터에 걸리지 않도록 확인

## 📝 추가 설정 옵션

### SMTP 서버 변경 (Gmail 외 다른 서비스 사용시)
```bash
# Naver Mail 예시
SPRING_MAIL_HOST=smtp.naver.com
SPRING_MAIL_PORT=587

# Outlook 예시  
SPRING_MAIL_HOST=smtp-mail.outlook.com
SPRING_MAIL_PORT=587
```

### 이메일 템플릿 커스터마이징
`EmailService.java` 파일에서 `buildSystemLevelAlertContent()` 및 `buildResourceLevelAlertContent()` 메서드를 수정하여 이메일 템플릿을 변경할 수 있습니다.

## 🔒 보안 고려사항

1. **앱 비밀번호 관리**: 앱 비밀번호는 안전한 곳에 보관하고 정기적으로 교체
2. **환경 변수 보호**: `.env` 파일을 Git에 커밋하지 않도록 주의
3. **발송 계정 분리**: 가능하면 알람 전용 Gmail 계정을 별도로 생성하여 사용



