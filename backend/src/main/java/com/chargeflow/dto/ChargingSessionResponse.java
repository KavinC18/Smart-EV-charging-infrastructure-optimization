package com.chargeflow.dto;

import java.time.LocalDateTime;

public class ChargingSessionResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long stationId;
    private String stationName;
    private Long vehicleId;
    private String vehicleModel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double energyDeliveredKwh;
    private double cost;
    private String status;

    public ChargingSessionResponse() {}

    public ChargingSessionResponse(Long id, Long userId, String username, Long stationId, String stationName,
                                   Long vehicleId, String vehicleModel, LocalDateTime startTime, LocalDateTime endTime,
                                   double energyDeliveredKwh, double cost, String status) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.stationId = stationId;
        this.stationName = stationName;
        this.vehicleId = vehicleId;
        this.vehicleModel = vehicleModel;
        this.startTime = startTime;
        this.endTime = endTime;
        this.energyDeliveredKwh = energyDeliveredKwh;
        this.cost = cost;
        this.status = status;
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

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static ChargingSessionResponseBuilder builder() {
        return new ChargingSessionResponseBuilder();
    }

    public static class ChargingSessionResponseBuilder {
        private Long id;
        private Long userId;
        private String username;
        private Long stationId;
        private String stationName;
        private Long vehicleId;
        private String vehicleModel;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private double energyDeliveredKwh;
        private double cost;
        private String status;

        public ChargingSessionResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChargingSessionResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public ChargingSessionResponseBuilder username(String username) {
            this.username = username;
            return this;
        }

        public ChargingSessionResponseBuilder stationId(Long stationId) {
            this.stationId = stationId;
            return this;
        }

        public ChargingSessionResponseBuilder stationName(String stationName) {
            this.stationName = stationName;
            return this;
        }

        public ChargingSessionResponseBuilder vehicleId(Long vehicleId) {
            this.vehicleId = vehicleId;
            return this;
        }

        public ChargingSessionResponseBuilder vehicleModel(String vehicleModel) {
            this.vehicleModel = vehicleModel;
            return this;
        }

        public ChargingSessionResponseBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public ChargingSessionResponseBuilder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public ChargingSessionResponseBuilder energyDeliveredKwh(double energyDeliveredKwh) {
            this.energyDeliveredKwh = energyDeliveredKwh;
            return this;
        }

        public ChargingSessionResponseBuilder cost(double cost) {
            this.cost = cost;
            return this;
        }

        public ChargingSessionResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public ChargingSessionResponse build() {
            return new ChargingSessionResponse(id, userId, username, stationId, stationName, vehicleId,
                    vehicleModel, startTime, endTime, energyDeliveredKwh, cost, status);
        }
    }
}
