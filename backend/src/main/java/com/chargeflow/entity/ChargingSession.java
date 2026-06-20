package com.chargeflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "charging_sessions")
public class ChargingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStation station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "energy_delivered_kwh", nullable = false)
    private double energyDeliveredKwh;

    @Column(nullable = false)
    private double cost;

    @Column(nullable = false)
    private String status;

    public ChargingSession() {}

    public ChargingSession(Long id, User user, ChargingStation station, Vehicle vehicle,
                           LocalDateTime startTime, LocalDateTime endTime, double energyDeliveredKwh,
                           double cost, String status) {
        this.id = id;
        this.user = user;
        this.station = station;
        this.vehicle = vehicle;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ChargingStation getStation() {
        return station;
    }

    public void setStation(ChargingStation station) {
        this.station = station;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
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

    public static ChargingSessionBuilder builder() {
        return new ChargingSessionBuilder();
    }

    public static class ChargingSessionBuilder {
        private Long id;
        private User user;
        private ChargingStation station;
        private Vehicle vehicle;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private double energyDeliveredKwh;
        private double cost;
        private String status;

        public ChargingSessionBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChargingSessionBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ChargingSessionBuilder station(ChargingStation station) {
            this.station = station;
            return this;
        }

        public ChargingSessionBuilder vehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        public ChargingSessionBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public ChargingSessionBuilder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public ChargingSessionBuilder energyDeliveredKwh(double energyDeliveredKwh) {
            this.energyDeliveredKwh = energyDeliveredKwh;
            return this;
        }

        public ChargingSessionBuilder cost(double cost) {
            this.cost = cost;
            return this;
        }

        public ChargingSessionBuilder status(String status) {
            this.status = status;
            return this;
        }

        public ChargingSession build() {
            return new ChargingSession(id, user, station, vehicle, startTime, endTime, energyDeliveredKwh, cost, status);
        }
    }
}
