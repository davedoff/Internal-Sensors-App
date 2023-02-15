package com.davidhalldoff.internalsensors.model;

/**
 * This class represents the values of a sensor
 */
public class Sensors {
    private long timestamp;
    private double angle;

    /**
     * Constructs a new sensor
     */
    public Sensors() {
        this.timestamp = 0;
        this.angle = 0;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
