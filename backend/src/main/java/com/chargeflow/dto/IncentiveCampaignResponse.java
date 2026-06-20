package com.chargeflow.dto;

public class IncentiveCampaignResponse {
    private Long id;
    private Long sourceStationId;
    private String sourceStationName;
    private Long targetStationId;
    private String targetStationName;
    private double discountPercentage;
    private boolean active;
    private double thresholdWaitTimeMinutes;
    private String description;

    public IncentiveCampaignResponse() {}

    public IncentiveCampaignResponse(Long id, Long sourceStationId, String sourceStationName, Long targetStationId,
                                     String targetStationName, double discountPercentage, boolean active,
                                     double thresholdWaitTimeMinutes, String description) {
        this.id = id;
        this.sourceStationId = sourceStationId;
        this.sourceStationName = sourceStationName;
        this.targetStationId = targetStationId;
        this.targetStationName = targetStationName;
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

    public Long getSourceStationId() {
        return sourceStationId;
    }

    public void setSourceStationId(Long sourceStationId) {
        this.sourceStationId = sourceStationId;
    }

    public String getSourceStationName() {
        return sourceStationName;
    }

    public void setSourceStationName(String sourceStationName) {
        this.sourceStationName = sourceStationName;
    }

    public Long getTargetStationId() {
        return targetStationId;
    }

    public void setTargetStationId(Long targetStationId) {
        this.targetStationId = targetStationId;
    }

    public String getTargetStationName() {
        return targetStationName;
    }

    public void setTargetStationName(String targetStationName) {
        this.targetStationName = targetStationName;
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

    public static IncentiveCampaignResponseBuilder builder() {
        return new IncentiveCampaignResponseBuilder();
    }

    public static class IncentiveCampaignResponseBuilder {
        private Long id;
        private Long sourceStationId;
        private String sourceStationName;
        private Long targetStationId;
        private String targetStationName;
        private double discountPercentage;
        private boolean active;
        private double thresholdWaitTimeMinutes;
        private String description;

        public IncentiveCampaignResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public IncentiveCampaignResponseBuilder sourceStationId(Long sourceStationId) {
            this.sourceStationId = sourceStationId;
            return this;
        }

        public IncentiveCampaignResponseBuilder sourceStationName(String sourceStationName) {
            this.sourceStationName = sourceStationName;
            return this;
        }

        public IncentiveCampaignResponseBuilder targetStationId(Long targetStationId) {
            this.targetStationId = targetStationId;
            return this;
        }

        public IncentiveCampaignResponseBuilder targetStationName(String targetStationName) {
            this.targetStationName = targetStationName;
            return this;
        }

        public IncentiveCampaignResponseBuilder discountPercentage(double discountPercentage) {
            this.discountPercentage = discountPercentage;
            return this;
        }

        public IncentiveCampaignResponseBuilder active(boolean active) {
            this.active = active;
            return this;
        }

        public IncentiveCampaignResponseBuilder thresholdWaitTimeMinutes(double thresholdWaitTimeMinutes) {
            this.thresholdWaitTimeMinutes = thresholdWaitTimeMinutes;
            return this;
        }

        public IncentiveCampaignResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public IncentiveCampaignResponse build() {
            return new IncentiveCampaignResponse(id, sourceStationId, sourceStationName, targetStationId,
                    targetStationName, discountPercentage, active, thresholdWaitTimeMinutes, description);
        }
    }
}
