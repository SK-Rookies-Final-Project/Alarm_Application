package com.alarm.service;

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
     * 시스템 레벨 알람 내용 구성
     */
    private String buildSystemLevelAlertContent(String topicName, String messageData) {
        return String.format(
            """
            🚨 시스템 보안 알람 🚨
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            📅 발생 시간: %s
            📋 토픽 이름: %s
            🔍 알람 유형: 시스템 레벨 보안 이벤트
            👤 담당자: 시스템 개발자
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            📝 수신된 데이터:
            %s
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            ⚠️  즉시 확인이 필요한 시스템 보안 이벤트가 감지되었습니다.
            📞 문의사항이 있으시면 시스템 관리팀에 연락해주세요.
            
            🤖 이 메일은 알람 서버에서 자동으로 발송되었습니다.
            """,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            topicName,
            messageData
        );
    }

    /**
     * 리소스/사용자 레벨 알람 내용 구성
     */
    private String buildResourceLevelAlertContent(String topicName, String messageData) {
        return String.format(
            """
            ⚠️ 리소스/사용자 보안 알람 ⚠️
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            📅 발생 시간: %s
            📋 토픽 이름: %s
            🔍 알람 유형: 리소스/사용자 레벨 보안 이벤트
            👤 담당자: 유저/리소스 개발자
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            📝 수신된 데이터:
            %s
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            ⚠️  리소스 접근 또는 사용자 인증 관련 보안 이벤트가 감지되었습니다.
            📞 문의사항이 있으시면 개발팀에 연락해주세요.
            
            🤖 이 메일은 알람 서버에서 자동으로 발송되었습니다.
            """,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            topicName,
            messageData
        );
    }
}
