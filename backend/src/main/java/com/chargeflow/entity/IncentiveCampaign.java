package com.chargeflow.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "incentive_campaigns")
public class IncentiveCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_station_id", nullable = false)
    private ChargingStation sourceStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_station_id", nullable = false)
    private ChargingStation targetStation;

    @Column(name = "discount_percentage", nullable = false)
    private double discountPercentage;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "threshold_wait_time_minutes", nullable = false)
    private double thresholdWaitTimeMinutes;

    @Column(length = 500)
    private String description;

    public IncentiveCampaign() {}

    public IncentiveCampaign(Long id, ChargingStation sourceStation, ChargingStation targetStation,
                             double discountPercentage, boolean active, double thresholdWaitTimeMinutes,
                             String description) {
        this.id = id;
        this.sourceStation = sourceStation;
        this.targetStation = targetStation;
        this.discountPercentage = discountPercentage;
        this.active = active;
        this.thresholdWaitTimeMinutes = thresholdWaitTimeMinutes;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChargingStation getSourceStation() {
        return sourceStation;
    }

    public void setSourceStation(ChargingStation sourceStation) {
        this.sourceStation = sourceStation;
    }

    public ChargingStation getTargetStation() {
        return targetStation;
    }

    public void setTargetStation(ChargingStation targetStation) {
        this.targetStation = targetStation;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getThresholdWaitTimeMinutes() {
        return thresholdWaitTimeMinutes;
    }

    public void setThresholdWaitTimeMinutes(double thresholdWaitTimeMinutes) {
        this.thresholdWaitTimeMinutes = thresholdWaitTimeMinutes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static IncentiveCampaignBuilder builder() {
        return new IncentiveCampaignBuilder();
    }

    public static class IncentiveCampaignBuilder {
        private Long id;
        private ChargingStation sourceStation;
        private ChargingStation targetStation;
        private double discountPercentage;
        private boolean active;
        private double thresholdWaitTimeMinutes;
        private String description;

        public IncentiveCampaignBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public IncentiveCampaignBuilder sourceStation(ChargingStation sourceStation) {
            this.sourceStation = sourceStation;
            return this;
        }

        public IncentiveCampaignBuilder targetStation(ChargingStation targetStation) {
            this.targetStation = targetStation;
            return this;
        }

        public IncentiveCampaignBuilder discountPercentage(double discountPercentage) {
            this.discountPercentage = discountPercentage;
            return this;
        }

        public IncentiveCampaignBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public IncentiveCampaignBuilder thresholdWaitTimeMinutes(double thresholdWaitTimeMinutes) {
            this.thresholdWaitTimeMinutes = thresholdWaitTimeMinutes;
            return this;
        }

        public IncentiveCampaignBuilder description(String description) {
            this.description = description;
            return this;
        }

        public IncentiveCampaign build() {
            return new IncentiveCampaign(id, sourceStation, targetStation, discountPercentage, active, thresholdWaitTimeMinutes, description);
        }
    }
}
