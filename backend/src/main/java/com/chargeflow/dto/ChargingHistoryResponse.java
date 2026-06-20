package com.chargeflow.dto;

import java.time.LocalDateTime;

public class ChargingHistoryResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long stationId;
    private String stationName;
    private LocalDateTime date;
    private double durationMinutes;
    private double energyDeliveredKwh;
    private double cost;

    public ChargingHistoryResponse() {}

    public ChargingHistoryResponse(Long id, Long userId, String username, Long stationId, String stationName,
                                   LocalDateTime date, double durationMinutes, double energyDeliveredKwh, double cost) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.stationId = stationId;
        this.stationName = stationName;
        this.date = date;
        this.durationMinutes = durationMinutes;
        this.energyDeliveredKwh = energyDeliveredKwh;
        this.cost = cost;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public double getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(double durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public double getEnergyDeliveredKwh() {
        return energyDeliveredKwh;
    }

    public void setEnergyDeliveredKwh(double energyDeliveredKwh) {
        this.energyDeliveredKwh = energyDeliveredKwh;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public static ChargingHistoryResponseBuilder builder() {
        return new ChargingHistoryResponseBuilder();
    }

    public static class ChargingHistoryResponseBuilder {
        private Long id;
        private Long userId;
        private String username;
        private Long stationId;
        private String stationName;
        private LocalDateTime date;
        private double durationMinutes;
        private double energyDeliveredKwh;
        private double cost;

        public ChargingHistoryResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChargingHistoryResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public ChargingHistoryResponseBuilder username(String username) {
            this.username = username;
            return this;
        }

        public ChargingHistoryResponseBuilder stationId(Long stationId) {
            this.stationId = stationId;
            return this;
        }

        public ChargingHistoryResponseBuilder stationName(String stationName) {
            this.stationName = stationName;
            return this;
        }

        public ChargingHistoryResponseBuilder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public ChargingHistoryResponseBuilder durationMinutes(double durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public ChargingHistoryResponseBuilder energyDeliveredKwh(double energyDeliveredKwh) {
            this.energyDeliveredKwh = energyDeliveredKwh;
            return this;
        }

        public ChargingHistoryResponseBuilder cost(double cost) {
            this.cost = cost;
            return this;
        }

        public ChargingHistoryResponse build() {
            return new ChargingHistoryResponse(id, userId, username, stationId, stationName, date,
                    durationMinutes, energyDeliveredKwh, cost);
        }
    }
}
