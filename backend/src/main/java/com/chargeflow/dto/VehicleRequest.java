package com.chargeflow.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class VehicleRequest {
    @NotBlank(message = "Vehicle model is required")
    private String model;

    @Min(value = 10, message = "Battery capacity must be at least 10 kWh")
    private double batteryCapacityKwh;

    @Min(value = 0, message = "Battery level cannot be negative")
    private double currentBatteryLevelPct;

    @Min(value = 0, message = "Range cannot be negative")
    private double currentRangeKm;

    public VehicleRequest() {}

    public VehicleRequest(String model, double batteryCapacityKwh, double currentBatteryLevelPct, double currentRangeKm) {
        this.model = model;
        this.batteryCapacityKwh = batteryCapacityKwh;
        this.currentBatteryLevelPct = currentBatteryLevelPct;
        this.currentRangeKm = currentRangeKm;
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
}
