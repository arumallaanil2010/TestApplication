package com.example.beatroot.listener;

import com.example.beatroot.model.Event;
import com.example.beatroot.model.EventEntity;
import com.example.beatroot.repo.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class BeatrootListener {
    private final Logger log = LoggerFactory.getLogger(BeatrootListener.class);

    @Autowired
    private EventRepository repository; // optional; available in azure profile

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @KafkaListener(topics = "${app.downstream.beatroot-topic:beatroot-topic}", groupId = "beatroot-group")
    public void onMessage(String message, @Header(name = "env", required = false) String envHeader) {
        log.debug("Received beatroot message with env header={} : {}", envHeader, message);
        // If header is present and doesn't match this deployment's active profile, ignore
        if (envHeader != null && !envHeader.isEmpty() && !envHeader.equalsIgnoreCase(activeProfile)) {
            log.info("Ignoring message not intended for this environment (header={} active={}).", envHeader, activeProfile);
            return;
        }

        log.info("Beatroot consumed message: {}", message);

        // If running in Azure profile, process and persist to DB
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
