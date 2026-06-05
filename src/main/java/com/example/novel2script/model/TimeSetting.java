package com.example.novel2script.model;

public class TimeSetting {
    private Period period;
    private String specific;
    private String continuity;

    public TimeSetting() {
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public String getSpecific() {
        return specific;
    }

    public void setSpecific(String specific) {
        this.specific = specific;
    }

    public String getContinuity() {
        return continuity;
    }

    public void setContinuity(String continuity) {
        this.continuity = continuity;
    }

    public enum Period {
        dawn, morning, noon, afternoon, evening, night, midnight, day, unknown
    }
}
