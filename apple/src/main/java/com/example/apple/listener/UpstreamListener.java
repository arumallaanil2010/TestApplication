package com.example.apple.listener;

import com.example.apple.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UpstreamListener {
    private final Logger log = LoggerFactory.getLogger(UpstreamListener.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(topics = "upstream-topic", groupId = "apple-group")
    public void onMessage(String message) {
        try {
            Event e = mapper.readValue(message, Event.class);
            log.info("Apple consumed upstream event: {}", e);
            String targetTopic = "carrot-topic";
            if (e.getFlag() != null && e.getFlag().equalsIgnoreCase("beatroot")) {
                targetTopic = "beatroot-topic";
            }
            // forward as JSON string
            String out = mapper.writeValueAsString(e);
            kafkaTemplate.send(targetTopic, e.getId(), out);
            log.info("Forwarded event {} to {}", e.getId(), targetTopic);
        } catch (Exception ex) {
            log.error("Failed processing message", ex);
        }
    }
}
