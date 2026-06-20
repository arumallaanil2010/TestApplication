package com.example.carrot.controller;

import com.example.carrot.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carrot")
public class ProducerController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    @PostMapping("/publish")
    public String publish(@RequestBody Event event) throws Exception {
        String json = mapper.writeValueAsString(event);
        kafkaTemplate.send("carrot-topic", event.getId(), json);
        return "sent";
    }
}
