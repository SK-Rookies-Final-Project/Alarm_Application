package com.alarm.controller;

import com.alarm.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final EmailService emailService;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.password}")
    private String mailPassword;

    @Value("${spring.mail.host}")
    private String mailHost;

    @Value("${spring.mail.port}")
    private String mailPort;

    /**
     * 이메일 설정 확인
     */
    @GetMapping("/email-config")
    public ResponseEntity<Map<String, Object>> checkEmailConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("mailHost", mailHost);
        response.put("mailPort", mailPort);
        response.put("fromEmail", fromEmail);
        response.put("passwordLength", mailPassword != null ? mailPassword.length() : 0);
        response.put("passwordMasked", mailPassword != null ? mailPassword.replaceAll(".", "*") : "null");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 직접 이메일 발송 테스트 (JavaMailSender 사용)
     */
    @PostMapping("/direct-email")
    public ResponseEntity<Map<String, Object>> testDirectEmail(@RequestParam String to) {
        log.info("🧪 직접 이메일 발송 테스트 시작 - 수신자: {}", to);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("[테스트] 알람 서버 이메일 발송 테스트");
            message.setText(String.format(
                "알람 서버 이메일 발송 테스트\n\n" +
                "발송 시간: %s\n" +
                "발송자: %s\n" +
                "수신자: %s\n" +
                "SMTP 서버: %s:%s\n\n" +
                "이 메일이 정상적으로 수신되었다면 이메일 설정이 올바르게 구성되었습니다.",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                fromEmail,
                to,
                mailHost,
                mailPort
            ));
            
            log.info("이메일 발송 시도 중...");
            mailSender.send(message);
            log.info("✅ 직접 이메일 발송 성공!");
            
            response.put("status", "SUCCESS");
            response.put("message", "직접 이메일 발송 성공");
            response.put("recipient", to);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 직접 이메일 발송 실패: {}", e.getMessage(), e);
            
            response.put("status", "ERROR");
            response.put("message", "직접 이메일 발송 실패: " + e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 리소스/사용자 레벨 보안 알람 테스트
     */
    @PostMapping("/resource-alarm")
    public ResponseEntity<Map<String, Object>> testResourceAlarm() {
        log.info("🧪 리소스/사용자 레벨 보안 알람 테스트 시작");
        
        String testMessage = String.format(
            "{\n" +
            "  \"id\": \"test-resource-%s\",\n" +
            "  \"clientIp\": \"167.10.31.130\",\n" +
            "  \"eventTimeKST\": \"%s+09:00\",\n" +
            "  \"granted\": false,\n" +
            "  \"methodName\": \"getResource\",\n" +
            "  \"operation\": \"READ\",\n" +
            "  \"principal\": \"user123\",\n" +
            "  \"processingTimeKST\": \"%s+09:00\",\n" +
            "  \"resourceName\": \"/api/secure/data\",\n" +
            "  \"resourceType\": \"REST_API\"\n" +
            "}",
            System.currentTimeMillis(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"))
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        try {
            emailService.sendResourceLevelAlert("resource-level-false", testMessage);
            
            response.put("status", "SUCCESS");
            response.put("message", "리소스/사용자 레벨 보안 알람 테스트 발송 완료");
            response.put("recipient", "tgurd123@gmail.com");
            response.put("topic", "resource-level-false");
            response.put("testData", testMessage);
            
            log.info("✅ 리소스/사용자 레벨 보안 알람 테스트 성공");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 리소스/사용자 레벨 보안 알람 테스트 실패: {}", e.getMessage(), e);
            
            response.put("status", "ERROR");
            response.put("message", "리소스/사용자 레벨 보안 알람 테스트 실패: " + e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 시스템 레벨 보안 알람 테스트
     */
    @PostMapping("/system-alarm")
    public ResponseEntity<Map<String, Object>> testSystemAlarm() {
        log.info("🧪 시스템 레벨 보안 알람 테스트 시작");
        
        String testMessage = String.format(
            "{\n" +
            "  \"id\": \"test-system-%s\",\n" +
            "  \"clientIp\": \"253.204.67.41\",\n" +
            "  \"eventTimeKST\": \"%s+09:00\",\n" +
            "  \"granted\": false,\n" +
            "  \"methodName\": \"systemOperation\",\n" +
            "  \"operation\": \"ADMIN\",\n" +
            "  \"principal\": \"user123\",\n" +
            "  \"processingTimeKST\": \"%s+09:00\",\n" +
            "  \"resourceName\": \"system/config\",\n" +
            "  \"resourceType\": \"SYSTEM\"\n" +
            "}",
            System.currentTimeMillis(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"))
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        try {
            emailService.sendSystemLevelAlert("system-level-false", testMessage);
            
            response.put("status", "SUCCESS");
            response.put("message", "시스템 레벨 보안 알람 테스트 발송 완료");
            response.put("recipient", "tgsduser@gmail.com");
            response.put("topic", "system-level-false");
            response.put("testData", testMessage);
            
            log.info("✅ 시스템 레벨 보안 알람 테스트 성공");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ 시스템 레벨 보안 알람 테스트 실패: {}", e.getMessage(), e);
            
            response.put("status", "ERROR");
            response.put("message", "시스템 레벨 보안 알람 테스트 실패: " + e.getMessage());
            response.put("errorType", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
