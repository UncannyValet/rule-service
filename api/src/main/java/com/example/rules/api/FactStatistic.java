package com.example.rules.api;

public class FactStatistic {

    private int count;
    private long duration;

    @SuppressWarnings("unused")
    public FactStatistic() {
    }

    public FactStatistic(int count, long duration) {
        this.count = count;
        this.duration = duration;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }
}
