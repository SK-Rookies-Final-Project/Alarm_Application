package com.alarm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAlarmConsumer {

    private final EmailService emailService;

    /**
     * system-level-false 토픽 구독
     * 시스템 개발자에게 알람 발송
     */
    @KafkaListener(topics = "${KAFKA_TOPIC_SYSTEM_LEVEL_FALSE}", groupId = "${CONSUMER_GROUP_ID}")
    public void consumeSystemLevelFalse(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("🚨 [SYSTEM LEVEL] 보안 이벤트 수신 - 토픽: {}, 파티션: {}, 오프셋: {}", topic, partition, offset);
        log.info("수신 데이터: {}", message);
        
        try {
            // 시스템 개발자에게 이메일 발송
            emailService.sendSystemLevelAlert(topic, message);
            log.info("✅ 시스템 레벨 보안 알람 처리 완료: {}", topic);
        } catch (Exception e) {
            log.error("❌ 시스템 레벨 보안 알람 처리 실패: {} - {}", topic, e.getMessage(), e);
        }
    }

    /**
     * resource-level-false 토픽 구독
     * 유저/리소스 개발자에게 알람 발송
     */
    @KafkaListener(topics = "${KAFKA_TOPIC_RESOURCE_LEVEL_FALSE}", groupId = "${CONSUMER_GROUP_ID}")
    public void consumeResourceLevelFalse(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("⚠️ [RESOURCE LEVEL] 보안 이벤트 수신 - 토픽: {}, 파티션: {}, 오프셋: {}", topic, partition, offset);
        log.info("수신 데이터: {}", message);
        
        try {
            // 유저/리소스 개발자에게 이메일 발송
            emailService.sendResourceLevelAlert(topic, message);
            log.info("✅ 리소스 레벨 보안 알람 처리 완료: {}", topic);
        } catch (Exception e) {
            log.error("❌ 리소스 레벨 보안 알람 처리 실패: {} - {}", topic, e.getMessage(), e);
        }
    }

    /**
     * certified-2time 토픽 구독
     * 유저/리소스 개발자에게 알람 발송
     */
    @KafkaListener(topics = "${KAFKA_TOPIC_CERTIFIED_2TIME}", groupId = "${CONSUMER_GROUP_ID}")
    public void consumeCertified2Time(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("⚠️ [CERTIFIED 2TIME] 보안 이벤트 수신 - 토픽: {}, 파티션: {}, 오프셋: {}", topic, partition, offset);
        log.info("수신 데이터: {}", message);
        
        try {
            // 유저/리소스 개발자에게 이메일 발송
            emailService.sendResourceLevelAlert(topic, message);
            log.info("✅ 인증 2회 시도 보안 알람 처리 완료: {}", topic);
        } catch (Exception e) {
            log.error("❌ 인증 2회 시도 보안 알람 처리 실패: {} - {}", topic, e.getMessage(), e);
        }
    }

    /**
     * certified-notMove 토픽 구독
     * 유저/리소스 개발자에게 알람 발송
     */
    @KafkaListener(topics = "${KAFKA_TOPIC_CERTIFIED_NOTMOVE}", groupId = "${CONSUMER_GROUP_ID}")
    public void consumeCertifiedNotMove(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("⚠️ [CERTIFIED NOT MOVE] 보안 이벤트 수신 - 토픽: {}, 파티션: {}, 오프셋: {}", topic, partition, offset);
        log.info("수신 데이터: {}", message);
        
        try {
            // 유저/리소스 개발자에게 이메일 발송
            emailService.sendResourceLevelAlert(topic, message);
            log.info("✅ 인증 후 미이동 보안 알람 처리 완료: {}", topic);
        } catch (Exception e) {
            log.error("❌ 인증 후 미이동 보안 알람 처리 실패: {} - {}", topic, e.getMessage(), e);
        }
    }
}
