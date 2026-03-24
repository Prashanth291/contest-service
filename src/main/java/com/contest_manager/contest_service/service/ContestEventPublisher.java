package com.contest_manager.contest_service.service;

import com.contest_manager.contest_service.dto.ContestStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContestEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "contest-events";

    public void publishContestStartedEvent(ContestStartedEvent event) {
        log.info("Publishing ContestStartedEvent to topic '{}' for contestId: {}", TOPIC, event.getContestId());
        kafkaTemplate.send(TOPIC, event.getContestId(), event);
    }
}