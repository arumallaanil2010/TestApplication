package com.example.apple.controller;

import com.example.apple.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Profile;

import java.nio.charset.StandardCharsets;

@Profile("local") // only enable this controller for local testing
@RestController
@RequestMapping("/api/upstream")
public class UpstreamController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.kafka.topics.upstream:upstream-topic}")
    private String upstreamTopic;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @PostMapping("/publish")
    public String publish(@RequestBody Event event) throws Exception {
        String json = mapper.writeValueAsString(event);
        ProducerRecord<String, String> record = new ProducerRecord<>(upstreamTopic, event.getId(), json);
        // annotate origin for observability
        record.headers().add(new RecordHeader("origin", activeProfile.getBytes(StandardCharsets.UTF_8)));
        kafkaTemplate.send(record);
        return "published to " + upstreamTopic;
    }
}
