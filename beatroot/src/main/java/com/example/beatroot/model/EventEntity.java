package com.example.beatroot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "events")
public class EventEntity {
    @Id
    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "flag")
    private String flag;

    @Column(name = "payload", length = 4000)
    private String payload;

    @Column(name = "source_env")
    private String sourceEnv;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getSourceEnv() { return sourceEnv; }
    public void setSourceEnv(String sourceEnv) { this.sourceEnv = sourceEnv; }
}
