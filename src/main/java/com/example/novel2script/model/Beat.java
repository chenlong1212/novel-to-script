package com.example.novel2script.model;

public class Beat {
    private BeatType type;
    private String content;
    
    // Action fields
    private String subject;
    
    // Dialogue fields
    private String speaker;
    private Emotion emotion;
    private Intensity intensity;
    private String direction;
    private String to;

    public Beat() {
    }

    public BeatType getType() {
        return type;
    }

    public void setType(BeatType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }

    public Intensity getIntensity() {
        return intensity;
    }

    public void setIntensity(Intensity intensity) {
        this.intensity = intensity;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public enum BeatType {
        action, dialogue, narration, transition
    }

    public enum Emotion {
        happy, sad, angry, fearful, surprised, disgusted, neutral, sarcastic, loving, guilty, proud, shy
    }

    public enum Intensity {
        low, medium, high
    }
}
