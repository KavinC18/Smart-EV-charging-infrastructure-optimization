package com.chargeflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "charging_history")
public class ChargingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private ChargingStation station;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(name = "duration_minutes", nullable = false)
    private double durationMinutes;

    @Column(name = "energy_delivered_kwh", nullable = false)
    private double energyDeliveredKwh;

    @Column(nullable = false)
    private double cost;

    public ChargingHistory() {}

    public ChargingHistory(Long id, User user, ChargingStation station, LocalDateTime date,
                           double durationMinutes, double energyDeliveredKwh, double cost) {
        this.id = id;
        this.user = user;
        this.station = station;
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

    public static ChargingHistoryBuilder builder() {
        return new ChargingHistoryBuilder();
    }

    public static class ChargingHistoryBuilder {
        private Long id;
        private User user;
        private ChargingStation station;
        private LocalDateTime date;
        private double durationMinutes;
        private double energyDeliveredKwh;
        private double cost;

        public ChargingHistoryBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ChargingHistoryBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ChargingHistoryBuilder station(ChargingStation station) {
            this.station = station;
            return this;
        }

        public ChargingHistoryBuilder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public ChargingHistoryBuilder durationMinutes(double durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public ChargingHistoryBuilder energyDeliveredKwh(double energyDeliveredKwh) {
            this.energyDeliveredKwh = energyDeliveredKwh;
            return this;
        }

        public ChargingHistoryBuilder cost(double cost) {
            this.cost = cost;
            return this;
        }

        public ChargingHistory build() {
            return new ChargingHistory(id, user, station, date, durationMinutes, energyDeliveredKwh, cost);
        }
    }
}
