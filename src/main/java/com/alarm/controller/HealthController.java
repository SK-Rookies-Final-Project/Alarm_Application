package com.alarm.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${KAFKA_BOOTSTRAP_SERVERS}")
    private String kafkaBootstrapServers;

    @Value("${SYSTEM_DEVELOPER_EMAIL}")
    private String systemDeveloperEmail;

    @Value("${USER_RESOURCE_DEVELOPER_EMAIL}")
    private String userResourceDeveloperEmail;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Alarm Server");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.put("port", "8081");
        
        log.info("헬스체크 요청 수신");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> config() {
        Map<String, Object> response = new HashMap<>();
        response.put("kafkaBootstrapServers", kafkaBootstrapServers);
        response.put("systemDeveloperEmail", systemDeveloperEmail);
        response.put("userResourceDeveloperEmail", userResourceDeveloperEmail);
        response.put("monitoringTopics", new String[]{
            "system-level-false",
            "resource-level-false", 
            "certified-2time",
            "certified-notMove"
        });
        
        log.info("설정 정보 요청 수신");
        return ResponseEntity.ok(response);
    }
}





