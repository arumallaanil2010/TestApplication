package com.example.carrot.listener;

import com.example.carrot.model.Event;
import com.example.carrot.model.EventEntity;
import com.example.carrot.repo.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class CarrotListener {
    private final Logger log = LoggerFactory.getLogger(CarrotListener.class);

    @Autowired
    private EventRepository repository;

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @KafkaListener(topics = "${app.downstream.carrot-topic:carrot-topic}", groupId = "carrot-group")
    public void onMessage(String message, @Header(name = "env", required = false) String envHeader) {
        log.debug("Received carrot message with env header={} : {}", envHeader, message);
        if (envHeader != null && !envHeader.isEmpty() && !envHeader.equalsIgnoreCase(activeProfile)) {
            log.info("Ignoring message not intended for this environment (header={} active={}).", envHeader, activeProfile);
            return;
        }

        log.info("Carrot consumed message: {}", message);
        System.out.println("[Carrot] consumed and processed message: " + message);

        if ("azure".equalsIgnoreCase(activeProfile)) {
            try {
                Event e = mapper.readValue(message, Event.class);
                EventEntity entity = new EventEntity();
                entity.setEventId(e.getId());
                entity.setFlag(e.getFlag());
                entity.setPayload(e.getPayload());
                entity.setSourceEnv(envHeader != null ? envHeader : "azure");
                repository.save(entity);
                log.info("Persisted event {} to DB", e.getId());
            } catch (Exception ex) {
                log.error("Failed to persist event", ex);
            }
        }
    }
}
