package com.chargeflow.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "charging_stations")
public class ChargingStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "total_chargers", nullable = false)
    private int totalChargers;

    @Column(name = "available_chargers", nullable = false)
    private int availableChargers;

    @Column(name = "active_sessions", nullable = false)
    private int activeSessions;

    @Column(name = "queue_length", nullable = false)
    private int queueLength;

    @Column(name = "average_charging_duration_minutes", nullable = false)
    private double averageChargingDurationMinutes;

    @Column(name = "service_rate", nullable = false)
    private double serviceRate;

    @Column(name = "arrival_rate", nullable = false)
    private double arrivalRate;

    @Column(name = "utilization_percentage", nullable = false)
    private double utilizationPercentage;

    @Column(name = "expected_wait_time_minutes", nullable = false)
    private double expectedWaitTimeMinutes;

    @Column(name = "wait_probability", nullable = false)
    private double waitProbability;

    @Column(nullable = false)
    private String status;

    @Column(name = "dynamic_pricing_per_kwh", nullable = false)
    private double dynamicPricingPerKwh;

    public ChargingStation() {}

    public ChargingStation(Long id, String name, double latitude, double longitude, int totalChargers,
                           int availableChargers, int activeSessions, int queueLength,
                           double averageChargingDurationMinutes, double serviceRate, double arrivalRate,
                           double utilizationPercentage, double expectedWaitTimeMinutes,
                           double waitProbability, String status, double dynamicPricingPerKwh) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.totalChargers = totalChargers;
        this.availableChargers = availableChargers;
        this.activeSessions = activeSessions;
        this.queueLength = queueLength;
        this.averageChargingDurationMinutes = averageChargingDurationMinutes;
        this.serviceRate = serviceRate;
        this.arrivalRate = arrivalRate;
        this.utilizationPercentage = utilizationPercentage;
        this.expectedWaitTimeMinutes = expectedWaitTimeMinutes;
        this.waitProbability = waitProbability;
        this.status = status;
        this.dynamicPricingPerKwh = dynamicPricingPerKwh;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getTotalChargers() {
        return totalChargers;
    }

    public void setTotalChargers(int totalChargers) {
        this.totalChargers = totalChargers;
    }

    public int getAvailableChargers() {
        return availableChargers;
    }

    public void setAvailableChargers(int availableChargers) {
        this.availableChargers = availableChargers;
    }

    public int getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(int activeSessions) {
        this.activeSessions = activeSessions;
    }

    public int getQueueLength() {
        return queueLength;
    }

    public void setQueueLength(int queueLength) {
        this.queueLength = queueLength;
    }

    public double getAverageChargingDurationMinutes() {
        return averageChargingDurationMinutes;
    }

    public void setAverageChargingDurationMinutes(double averageChargingDurationMinutes) {
        this.averageChargingDurationMinutes = averageChargingDurationMinutes;
    }

    public double getServiceRate() {
        return serviceRate;
    }

    public void setServiceRate(double serviceRate) {
        this.serviceRate = serviceRate;
    }

    public double getArrivalRate() {
        return arrivalRate;
    }

    public void setArrivalRate(double arrivalRate) {
        this.arrivalRate = arrivalRate;
    }

    public double getUtilizationPercentage() {
        return utilizationPercentage;
    }

    public void setUtilizationPercentage(double utilizationPercentage) {
        this.utilizationPercentage = utilizationPercentage;
    }

    public double getExpectedWaitTimeMinutes() {
        return expectedWaitTimeMinutes;
    }

    public void setExpectedWaitTimeMinutes(double expectedWaitTimeMinutes) {
        this.expectedWaitTimeMinutes = expectedWaitTimeMinutes;
    }

    public double getWaitProbability() {
        return waitProbability;
    }

    public void setWaitProbability(double waitProbability) {
        this.waitProbability = waitProbability;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getDynamicPricingPerKwh() {
        return dynamicPricingPerKwh;
    }

    public void setDynamicPricingPerKwh(double dynamicPricingPerKwh) {
        this.dynamicPricingPerKwh = dynamicPricingPerKwh;
    }

    public static ChargingStationBuilder builder() {
        return new ChargingStationBuilder();
    }

    public static class ChargingStationBuilder {
        private Long id;
        private String name;
        private double latitude;
        private double longitude;
        private int totalChargers;
        private int availableChargers;
        private int activeSessions;
        private int queueLength;
        private double averageChargingDurationMinutes;
        private double serviceRate;
        private double arrivalRate;
        private double utilizationPercentage;
        private double expectedWaitTimeMinutes;
        private double waitProbability;
        private String status;
        private double dynamicPricingPerKwh;

        public ChargingStationBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChargingStationBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ChargingStationBuilder latitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public ChargingStationBuilder longitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public ChargingStationBuilder totalChargers(int totalChargers) {
            this.totalChargers = totalChargers;
            return this;
        }

        public ChargingStationBuilder availableChargers(int availableChargers) {
            this.availableChargers = availableChargers;
            return this;
        }

        public ChargingStationBuilder activeSessions(int activeSessions) {
            this.activeSessions = activeSessions;
            return this;
        }

        public ChargingStationBuilder queueLength(int queueLength) {
            this.queueLength = queueLength;
            return this;
        }

        public ChargingStationBuilder averageChargingDurationMinutes(double averageChargingDurationMinutes) {
            this.averageChargingDurationMinutes = averageChargingDurationMinutes;
            return this;
        }

        public ChargingStationBuilder serviceRate(double serviceRate) {
            this.serviceRate = serviceRate;
            return this;
        }

        public ChargingStationBuilder arrivalRate(double arrivalRate) {
            this.arrivalRate = arrivalRate;
            return this;
        }

        public ChargingStationBuilder utilizationPercentage(double utilizationPercentage) {
            this.utilizationPercentage = utilizationPercentage;
            return this;
        }

        public ChargingStationBuilder expectedWaitTimeMinutes(double expectedWaitTimeMinutes) {
            this.expectedWaitTimeMinutes = expectedWaitTimeMinutes;
            return this;
        }

        public ChargingStationBuilder waitProbability(double waitProbability) {
            this.waitProbability = waitProbability;
            return this;
        }

        public ChargingStationBuilder status(String status) {
            this.status = status;
            return this;
        }

        public ChargingStationBuilder dynamicPricingPerKwh(double dynamicPricingPerKwh) {
            this.dynamicPricingPerKwh = dynamicPricingPerKwh;
            return this;
        }

        public ChargingStation build() {
            return new ChargingStation(id, name, latitude, longitude, totalChargers, availableChargers,
                    activeSessions, queueLength, averageChargingDurationMinutes, serviceRate, arrivalRate,
                    utilizationPercentage, expectedWaitTimeMinutes, waitProbability, status, dynamicPricingPerKwh);
        }
    }
}
