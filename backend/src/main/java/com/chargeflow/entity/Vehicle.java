package com.chargeflow.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String model;

    @Column(name = "battery_capacity_kwh", nullable = false)
    private double batteryCapacityKwh;

    @Column(name = "current_battery_level_pct", nullable = false)
    private double currentBatteryLevelPct;

    @Column(name = "current_range_km", nullable = false)
    private double currentRangeKm;

    public Vehicle() {}

    public Vehicle(Long id, User user, String model, double batteryCapacityKwh, double currentBatteryLevelPct, double currentRangeKm) {
        this.id = id;
        this.user = user;
        this.model = model;
        this.batteryCapacityKwh = batteryCapacityKwh;
        this.currentBatteryLevelPct = currentBatteryLevelPct;
        this.currentRangeKm = currentRangeKm;
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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getBatteryCapacityKwh() {
        return batteryCapacityKwh;
    }

    public void setBatteryCapacityKwh(double batteryCapacityKwh) {
        this.batteryCapacityKwh = batteryCapacityKwh;
    }

    public double getCurrentBatteryLevelPct() {
        return currentBatteryLevelPct;
    }

    public void setCurrentBatteryLevelPct(double currentBatteryLevelPct) {
        this.currentBatteryLevelPct = currentBatteryLevelPct;
    }

    public double getCurrentRangeKm() {
        return currentRangeKm;
    }

    public void setCurrentRangeKm(double currentRangeKm) {
        this.currentRangeKm = currentRangeKm;
    }

    public static VehicleBuilder builder() {
        return new VehicleBuilder();
    }

    public static class VehicleBuilder {
        private Long id;
        private User user;
        private String model;
        private double batteryCapacityKwh;
        private double currentBatteryLevelPct;
        private double currentRangeKm;

        public VehicleBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public VehicleBuilder user(User user) {
            this.user = user;
            return this;
        }

        public VehicleBuilder model(String model) {
            this.model = model;
            return this;
        }

        public VehicleBuilder batteryCapacityKwh(double batteryCapacityKwh) {
            this.batteryCapacityKwh = batteryCapacityKwh;
            return this;
        }

        public VehicleBuilder currentBatteryLevelPct(double currentBatteryLevelPct) {
            this.currentBatteryLevelPct = currentBatteryLevelPct;
            return this;
        }

        public VehicleBuilder currentRangeKm(double currentRangeKm) {
            this.currentRangeKm = currentRangeKm;
            return this;
        }

        public Vehicle build() {
            return new Vehicle(id, user, model, batteryCapacityKwh, currentBatteryLevelPct, currentRangeKm);
        }
    }
}
