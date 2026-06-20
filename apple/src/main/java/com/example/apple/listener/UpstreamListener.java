package com.example.apple.listener;

import com.example.apple.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class UpstreamListener {
    private final Logger log = LoggerFactory.getLogger(UpstreamListener.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${spring.profiles.active:pcf}")
    private String activeProfile;

    @Value("${app.downstream.beatroot-topic:beatroot-topic}")
    private String beatrootTopic;

    @Value("${app.downstream.carrot-topic:carrot-topic}")
    private String carrotTopic;

    @KafkaListener(topics = "${app.kafka.topics.upstream}", groupId = "apple-group")
    public void onMessage(String message, @Header(name = "env", required = false) String envHeader) {
        try {
            // optional: ignore messages that carry an env header not matching this instance
            if (envHeader != null && !envHeader.isEmpty() && !envHeader.equalsIgnoreCase(activeProfile)) {
                log.info("Ignoring upstream message for env={} on activeProfile={}", envHeader, activeProfile);
                return;
            }

            Event e = mapper.readValue(message, Event.class);
            log.info("Apple consumed upstream event: {}", e);

            String targetTopic = carrotTopic;
            if (e.getFlag() != null && e.getFlag().equalsIgnoreCase("beatroot")) {
                targetTopic = beatrootTopic;
            }

            // forward as JSON string and add env header so downstream knows which environment produced it
            String out = mapper.writeValueAsString(e);
            ProducerRecord<String, String> record = new ProducerRecord<>(targetTopic, e.getId(), out);
            record.headers().add(new RecordHeader("env", activeProfile.getBytes(StandardCharsets.UTF_8)));
            kafkaTemplate.send(record);
            log.info("Forwarded event {} to {} with env={}", e.getId(), targetTopic, activeProfile);
        } catch (Exception ex) {
            log.error("Failed processing message", ex);
        }
    }
}
