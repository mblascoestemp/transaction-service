package org.mblascoespar.transactionservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.Objects;

public class Statistics {

    private int count;
    private double min;
    private double max;
    private double avg;
    private double sum;
    @JsonIgnore
    private long timestamp;

    public int getCount() {
        return count;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getAvg() {
        return avg;
    }

    public double getSum() {
        return sum;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Statistics(int count, double min, double max, double avg, double sum) {
        this.count = count;
        this.min = min;
        this.max = max;
        this.avg = avg;
        this.sum = sum;
        this.timestamp = Instant.now().toEpochMilli();
    }

    public static Statistics baseStatistics = new Statistics(0, 0, 0, 0, 0);


    public Statistics(Statistics stats) {
        this.count = stats.count;
        this.min = stats.min;
        this.max = stats.max;
        this.avg = stats.avg;
        this.sum = stats.sum;
        this.timestamp = stats.timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Statistics that = (Statistics) o;
        return count == that.count &&
                Double.compare(that.min, min) == 0 &&
                Double.compare(that.max, max) == 0 &&
                Double.compare(that.avg, avg) == 0 &&
                Double.compare(that.sum, sum) == 0 &&
                timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, min, max, avg, sum, timestamp);
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "count=" + count +
                ", min=" + min +
                ", max=" + max +
                ", avg=" + avg +
                ", sum=" + sum +
                ", timestamp=" + timestamp +
                '}';
    }
}
