package com.example.carrot.model;

public class Event {
    private String id;
    private String flag;
    private String payload;

    public Event() {}
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
