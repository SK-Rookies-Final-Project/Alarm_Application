package com.alarm.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${SYSTEM_DEVELOPER_EMAIL}")
    private String systemDeveloperEmail;

    @Value("${USER_RESOURCE_DEVELOPER_EMAIL}")
    private String userResourceDeveloperEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 시스템 레벨 보안 알람을 시스템 개발자에게 발송
     */
    @Async
    public void sendSystemLevelAlert(String topicName, String messageData) {
        String subject = "[🚨 시스템 보안 알람] " + topicName + " 토픽에서 보안 이벤트 감지";
        String content = buildSystemLevelAlertContent(topicName, messageData);
        
        sendEmail(systemDeveloperEmail, subject, content);
        log.info("시스템 레벨 보안 알람 발송 완료: {} -> {}", topicName, systemDeveloperEmail);
    }

    /**
     * 리소스/사용자 레벨 보안 알람을 유저/리소스 개발자에게 발송
     */
    @Async
    public void sendResourceLevelAlert(String topicName, String messageData) {
        String subject = "[⚠️ 리소스/사용자 보안 알람] " + topicName + " 토픽에서 보안 이벤트 감지";
        String content = buildResourceLevelAlertContent(topicName, messageData);
        
        sendEmail(userResourceDeveloperEmail, subject, content);
        log.info("리소스/사용자 레벨 보안 알람 발송 완료: {} -> {}", topicName, userResourceDeveloperEmail);
    }

    /**
     * 인증 실패 보안 알람을 유저/리소스 개발자에게 발송 (certified-2time)
     */
    @Async
    public void sendAuthFailureAlert(String topicName, String messageData) {
        String subject = "[🔐 인증 실패 보안 알람] " + topicName + " 토픽에서 인증 실패 이벤트 감지";
        String content = buildAuthFailureAlertContent(topicName, messageData);
        
        sendEmail(userResourceDeveloperEmail, subject, content);
        log.info("인증 실패 보안 알람 발송 완료: {} -> {}", topicName, userResourceDeveloperEmail);
    }

    /**
     * 위치 변경 보안 알람을 유저/리소스 개발자에게 발송 (certified-notMove)
     */
    @Async
    public void sendLocationChangeAlert(String topicName, String messageData) {
        String subject = "[🌍 위치 변경 보안 알람] " + topicName + " 토픽에서 위치 변경 이벤트 감지";
        String content = buildLocationChangeAlertContent(topicName, messageData);
        
        sendEmail(userResourceDeveloperEmail, subject, content);
        log.info("위치 변경 보안 알람 발송 완료: {} -> {}", topicName, userResourceDeveloperEmail);
    }

    /**
     * 이메일 발송 공통 메서드
     */
    private void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("이메일 발송 성공: {} -> {}", subject, to);
        } catch (Exception e) {
            log.error("이메일 발송 실패: {} -> {}, 오류: {}", subject, to, e.getMessage(), e);
        }
    }

    /**
     * JSON 데이터를 파싱하고 시스템 레벨 알람용으로 포맷팅
     */
    private String parseAndFormatSystemData(String messageData) {
        try {
            JsonNode jsonNode = objectMapper.readTree(messageData);
            
            return String.format(
                """
                🔍 보안 이벤트 상세 정보:
                
                📋 이벤트 ID: %s
                🌐 클라이언트 IP: %s
                👤 사용자: %s
                🔧 메서드: %s
                ⚙️  작업: %s
                📂 리소스: %s
                📊 리소스 타입: %s
                ✅ 권한 부여: %s
                🕐 이벤트 시간: %s
                ⏱️  처리 시간: %s
                
                🚨 보안 위험도: 높음 (시스템 관리자 권한 관련)
                """,
                getJsonValue(jsonNode, "id", "N/A"),
                getJsonValue(jsonNode, "clientIp", "N/A"),
                getJsonValue(jsonNode, "principal", "N/A"),
                getJsonValue(jsonNode, "methodName", "N/A"),
                getJsonValue(jsonNode, "operation", "N/A"),
                getJsonValue(jsonNode, "resourceName", "N/A"),
                getJsonValue(jsonNode, "resourceType", "N/A"),
                getJsonValue(jsonNode, "granted", "false").equals("true") ? "✅ 허용됨" : "❌ 거부됨",
                formatTimestamp(getJsonValue(jsonNode, "eventTimeKST", "")),
                formatTimestamp(getJsonValue(jsonNode, "processingTimeKST", ""))
            );
        } catch (Exception e) {
            log.warn("JSON 파싱 실패, 원본 데이터 사용: {}", e.getMessage());
            return "📝 수신된 데이터:\n" + messageData;
        }
    }

    /**
     * 시스템 레벨 알람 내용 구성
     */
    private String buildSystemLevelAlertContent(String topicName, String messageData) {
        String formattedData = parseAndFormatSystemData(messageData);
        
        return String.format(
            """
            🚨 시스템 보안 알람 🚨
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            📅 알람 발생 시간: %s
            📋 토픽 이름: %s
            🔍 알람 유형: 시스템 레벨 보안 이벤트
            👤 담당자: 시스템 개발자
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            %s
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            ⚠️  즉시 확인이 필요한 시스템 보안 이벤트가 감지되었습니다.
            📞 문의사항이 있으시면 시스템 관리팀에 연락해주세요.
            
            🤖 이 메일은 알람 서버에서 자동으로 발송되었습니다.
            """,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            topicName,
            formattedData
        );
    }

    /**
     * JSON 데이터를 파싱하고 리소스 레벨 알람용으로 포맷팅
     */
    private String parseAndFormatResourceData(String messageData) {
        try {
            JsonNode jsonNode = objectMapper.readTree(messageData);
            
            String riskLevel = determineRiskLevel(jsonNode);
            
            return String.format(
                """
                🔍 보안 이벤트 상세 정보:
                
                📋 이벤트 ID: %s
                🌐 클라이언트 IP: %s
                👤 사용자: %s
                🔧 메서드: %s
                ⚙️  작업: %s
                📂 리소스: %s
                📊 리소스 타입: %s
                ✅ 권한 부여: %s
                🕐 이벤트 시간: %s
                ⏱️  처리 시간: %s
                
                %s
                """,
                getJsonValue(jsonNode, "id", "N/A"),
                getJsonValue(jsonNode, "clientIp", "N/A"),
                getJsonValue(jsonNode, "principal", "N/A"),
                getJsonValue(jsonNode, "methodName", "N/A"),
                getJsonValue(jsonNode, "operation", "N/A"),
                getJsonValue(jsonNode, "resourceName", "N/A"),
                getJsonValue(jsonNode, "resourceType", "N/A"),
                getJsonValue(jsonNode, "granted", "false").equals("true") ? "✅ 허용됨" : "❌ 거부됨",
                formatTimestamp(getJsonValue(jsonNode, "eventTimeKST", "")),
                formatTimestamp(getJsonValue(jsonNode, "processingTimeKST", "")),
                riskLevel
            );
        } catch (Exception e) {
            log.warn("JSON 파싱 실패, 원본 데이터 사용: {}", e.getMessage());
            return "📝 수신된 데이터:\n" + messageData;
        }
    }

    /**
     * 위험도를 판단하는 메서드
     */
    private String determineRiskLevel(JsonNode jsonNode) {
        String operation = getJsonValue(jsonNode, "operation", "");
        String resourceType = getJsonValue(jsonNode, "resourceType", "");
        boolean granted = getJsonValue(jsonNode, "granted", "false").equals("true");
        
        if (!granted) {
            if (operation.equals("ADMIN") || resourceType.equals("SYSTEM")) {
                return "🚨 보안 위험도: 높음 (권한 거부된 관리자 작업)";
            } else if (operation.equals("DELETE") || operation.equals("UPDATE")) {
                return "⚠️ 보안 위험도: 중간 (권한 거부된 수정 작업)";
            } else {
                return "ℹ️ 보안 위험도: 낮음 (권한 거부된 조회 작업)";
            }
        } else {
            if (operation.equals("ADMIN") || resourceType.equals("SYSTEM")) {
                return "🔍 보안 위험도: 모니터링 필요 (관리자 작업 허용)";
            } else {
                return "✅ 보안 위험도: 정상 (일반 작업 허용)";
            }
        }
    }

    /**
     * 리소스/사용자 레벨 알람 내용 구성
     */
    private String buildResourceLevelAlertContent(String topicName, String messageData) {
        String formattedData = parseAndFormatResourceData(messageData);
        
        return String.format(
            """
            ⚠️ 리소스/사용자 보안 알람 ⚠️
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            📅 알람 발생 시간: %s
            📋 토픽 이름: %s
            🔍 알람 유형: 리소스/사용자 레벨 보안 이벤트
            👤 담당자: 유저/리소스 개발자
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            %s
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            ⚠️  리소스 접근 또는 사용자 인증 관련 보안 이벤트가 감지되었습니다.
            📞 문의사항이 있으시면 개발팀에 연락해주세요.
            
            🤖 이 메일은 알람 서버에서 자동으로 발송되었습니다.
            """,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            topicName,
            formattedData
        );
    }

    /**
     * JSON 노드에서 값을 안전하게 추출하는 유틸리티 메서드
     */
    private String getJsonValue(JsonNode jsonNode, String fieldName, String defaultValue) {
        JsonNode node = jsonNode.get(fieldName);
        return node != null ? node.asText() : defaultValue;
    }

    /**
     * 인증 실패 데이터 파싱 및 포맷팅 (certified-2time)
     */
    private String parseAndFormatAuthFailureData(String messageData) {
        try {
            JsonNode jsonNode = objectMapper.readTree(messageData);
            
            return String.format(
                """
                🔐 인증 실패 이벤트 상세 정보:
                
                📋 이벤트 ID: %s
                🕐 알람 시간: %s
                🔍 알람 유형: %s
                🌐 클라이언트 IP: %s
                📝 설명: %s
                🔢 실패 횟수: %s
                
                🚨 보안 위험도: 높음 (연속 인증 실패)
                ⚠️  브루트 포스 공격 가능성을 검토해주세요.
                """,
                getJsonValue(jsonNode, "id", "N/A"),
                formatTimestamp(getJsonValue(jsonNode, "alertTimeKST", "")),
                getJsonValue(jsonNode, "alertType", "N/A"),
                getJsonValue(jsonNode, "clientIp", "N/A"),
                getJsonValue(jsonNode, "description", "N/A"),
                getJsonValue(jsonNode, "failureCount", "N/A")
            );
        } catch (Exception e) {
            log.warn("인증 실패 JSON 파싱 실패, 원본 데이터 사용: {}", e.getMessage());
            return "📝 수신된 데이터:\n" + messageData;
        }
    }

    /**
     * 위치 변경 데이터 파싱 및 포맷팅 (certified-notMove)
     */
    private String parseAndFormatLocationChangeData(String messageData) {
        try {
            JsonNode jsonNode = objectMapper.readTree(messageData);
            
            return String.format(
                """
                🌍 위치 변경 이벤트 상세 정보:
                
                📋 이벤트 ID: %s
                🕐 알람 시간: %s
                🔍 알람 유형: %s
                🌐 클라이언트 IP: %s
                📝 설명: %s
                🔢 실패 횟수: %s
                
                🚨 보안 위험도: 높음 (비정상적인 위치 접근)
                ⚠️  지리적 위치 변경을 검토하고 정당한 접근인지 확인해주세요.
                """,
                getJsonValue(jsonNode, "id", "N/A"),
                formatTimestamp(getJsonValue(jsonNode, "alertTimeKST", "")),
                getJsonValue(jsonNode, "alertType", "N/A"),
                getJsonValue(jsonNode, "clientIp", "N/A"),
                getJsonValue(jsonNode, "description", "N/A"),
                getJsonValue(jsonNode, "failureCount", "N/A")
            );
        } catch (Exception e) {
            log.warn("위치 변경 JSON 파싱 실패, 원본 데이터 사용: {}", e.getMessage());
            return "📝 수신된 데이터:\n" + messageData;
        }
    }

    /**
     * 인증 실패 알람 내용 구성
     */
    private String buildAuthFailureAlertContent(String topicName, String messageData) {
        String formattedData = parseAndFormatAuthFailureData(messageData);
        
        return String.format(
            """
            🔐 인증 실패 보안 알람 🔐
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            📅 알람 발생 시간: %s
            📋 토픽 이름: %s
            🔍 알람 유형: 인증 실패 보안 이벤트
            👤 담당자: 유저/리소스 개발자
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            %s
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            🚨 연속된 인증 실패가 감지되었습니다. 브루트 포스 공격 가능성을 검토해주세요.
            🔒 필요시 해당 IP 주소에 대한 차단 조치를 고려해주세요.
            📞 문의사항이 있으시면 보안팀에 연락해주세요.
            
            🤖 이 메일은 알람 서버에서 자동으로 발송되었습니다.
            """,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            topicName,
            formattedData
        );
    }

    /**
     * 위치 변경 알람 내용 구성
     */
    private String buildLocationChangeAlertContent(String topicName, String messageData) {
        String formattedData = parseAndFormatLocationChangeData(messageData);
        
        return String.format(
            """
            🌍 위치 변경 보안 알람 🌍
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            📅 알람 발생 시간: %s
            📋 토픽 이름: %s
            🔍 알람 유형: 위치 변경 보안 이벤트
            👤 담당자: 유저/리소스 개발자
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            %s
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            🌍 비정상적인 위치에서의 접근 시도가 감지되었습니다.
            🔍 사용자의 지리적 위치 변경을 검토하고 정당한 접근인지 확인해주세요.
            📞 문의사항이 있으시면 보안팀에 연락해주세요.
            
            🤖 이 메일은 알람 서버에서 자동으로 발송되었습니다.
            """,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            topicName,
            formattedData
        );
    }

    /**
     * 타임스탬프를 사람이 읽기 쉬운 형식으로 포맷팅
     */
    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty() || timestamp.equals("N/A")) {
            return "N/A";
        }
        
        try {
            // ISO 8601 형식의 타임스탬프를 간단한 형식으로 변환
            if (timestamp.contains("T")) {
                return timestamp.replace("T", " ").substring(0, 19);
            }
            return timestamp;
        } catch (Exception e) {
            return timestamp;
        }
    }
}



