package com.chargeflow.service;

import com.chargeflow.entity.ChargingStation;
import com.chargeflow.entity.IncentiveCampaign;
import com.chargeflow.entity.Vehicle;
import com.chargeflow.repository.ChargingStationRepository;
import com.chargeflow.repository.IncentiveCampaignRepository;
import com.chargeflow.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RoutingEngine {

    private final ChargingStationRepository stationRepository;
    private final VehicleRepository vehicleRepository;
    private final IncentiveCampaignRepository incentiveCampaignRepository;

    // Average driving speed in km/h
    private static final double AVERAGE_SPEED_KMH = 50.0;
    // Standard charger output in kW (assumed)
    private static final double CHARGER_OUTPUT_KW = 120.0;

    public RoutingEngine(ChargingStationRepository stationRepository,
                         VehicleRepository vehicleRepository,
                         IncentiveCampaignRepository incentiveCampaignRepository) {
        this.stationRepository = stationRepository;
        this.vehicleRepository = vehicleRepository;
        this.incentiveCampaignRepository = incentiveCampaignRepository;
    }

    public static class RecommendationResult {
        private ChargingStation station;
        private double distanceKm;
        private double travelTimeMinutes;
        private double expectedWaitTimeMinutes;
        private double chargingTimeMinutes;
        private double totalTimeMinutes;
        private boolean reachable;
        private double discountPercentage;
        private String recommendationNotes;

        public RecommendationResult() {}

        public RecommendationResult(ChargingStation station, double distanceKm, double travelTimeMinutes,
                                    double expectedWaitTimeMinutes, double chargingTimeMinutes, double totalTimeMinutes,
                                    boolean reachable, double discountPercentage, String recommendationNotes) {
            this.station = station;
            this.distanceKm = distanceKm;
            this.travelTimeMinutes = travelTimeMinutes;
            this.expectedWaitTimeMinutes = expectedWaitTimeMinutes;
            this.chargingTimeMinutes = chargingTimeMinutes;
            this.totalTimeMinutes = totalTimeMinutes;
            this.reachable = reachable;
            this.discountPercentage = discountPercentage;
            this.recommendationNotes = recommendationNotes;
        }

        public ChargingStation getStation() {
            return station;
        }

        public void setStation(ChargingStation station) {
            this.station = station;
        }

        public double getDistanceKm() {
            return distanceKm;
        }

        public void setDistanceKm(double distanceKm) {
            this.distanceKm = distanceKm;
        }

        public double getTravelTimeMinutes() {
            return travelTimeMinutes;
        }

        public void setTravelTimeMinutes(double travelTimeMinutes) {
            this.travelTimeMinutes = travelTimeMinutes;
        }

        public double getExpectedWaitTimeMinutes() {
            return expectedWaitTimeMinutes;
        }

        public void setExpectedWaitTimeMinutes(double expectedWaitTimeMinutes) {
            this.expectedWaitTimeMinutes = expectedWaitTimeMinutes;
        }

        public double getChargingTimeMinutes() {
            return chargingTimeMinutes;
        }

        public void setChargingTimeMinutes(double chargingTimeMinutes) {
            this.chargingTimeMinutes = chargingTimeMinutes;
        }

        public double getTotalTimeMinutes() {
            return totalTimeMinutes;
        }

        public void setTotalTimeMinutes(double totalTimeMinutes) {
            this.totalTimeMinutes = totalTimeMinutes;
        }

        public boolean isReachable() {
            return reachable;
        }

        public void setReachable(boolean reachable) {
            this.reachable = reachable;
        }

        public double getDiscountPercentage() {
            return discountPercentage;
        }

        public void setDiscountPercentage(double discountPercentage) {
            this.discountPercentage = discountPercentage;
        }

        public String getRecommendationNotes() {
            return recommendationNotes;
        }

        public void setRecommendationNotes(String recommendationNotes) {
            this.recommendationNotes = recommendationNotes;
        }

        public static RecommendationResultBuilder builder() {
            return new RecommendationResultBuilder();
        }

        public static class RecommendationResultBuilder {
            private ChargingStation station;
            private double distanceKm;
            private double travelTimeMinutes;
            private double expectedWaitTimeMinutes;
            private double chargingTimeMinutes;
            private double totalTimeMinutes;
            private boolean reachable;
            private double discountPercentage;
            private String recommendationNotes;

            public RecommendationResultBuilder station(ChargingStation station) {
                this.station = station;
                return this;
            }

            public RecommendationResultBuilder distanceKm(double distanceKm) {
                this.distanceKm = distanceKm;
                return this;
            }

            public RecommendationResultBuilder travelTimeMinutes(double travelTimeMinutes) {
                this.travelTimeMinutes = travelTimeMinutes;
                return this;
            }

            public RecommendationResultBuilder expectedWaitTimeMinutes(double expectedWaitTimeMinutes) {
                this.expectedWaitTimeMinutes = expectedWaitTimeMinutes;
                return this;
            }

            public RecommendationResultBuilder chargingTimeMinutes(double chargingTimeMinutes) {
                this.chargingTimeMinutes = chargingTimeMinutes;
                return this;
            }

            public RecommendationResultBuilder totalTimeMinutes(double totalTimeMinutes) {
                this.totalTimeMinutes = totalTimeMinutes;
                return this;
            }

            public RecommendationResultBuilder reachable(boolean reachable) {
                this.reachable = reachable;
                return this;
            }

            public RecommendationResultBuilder discountPercentage(double discountPercentage) {
                this.discountPercentage = discountPercentage;
                return this;
            }

            public RecommendationResultBuilder recommendationNotes(String recommendationNotes) {
                this.recommendationNotes = recommendationNotes;
                return this;
            }

            public RecommendationResult build() {
                return new RecommendationResult(station, distanceKm, travelTimeMinutes, expectedWaitTimeMinutes,
                        chargingTimeMinutes, totalTimeMinutes, reachable, discountPercentage, recommendationNotes);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<RecommendationResult> recommendStations(double userLat, double userLng, Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with id: " + vehicleId));

        List<ChargingStation> stations = stationRepository.findAll();
        List<RecommendationResult> results = new ArrayList<>();

        double vehicleRange = vehicle.getCurrentRangeKm();
        double batteryCapacity = vehicle.getBatteryCapacityKwh();
        double batteryLevelPct = vehicle.getCurrentBatteryLevelPct();

        // Calculate energy needed to reach 100%
        double energyNeededKwh = batteryCapacity * (1.0 - (batteryLevelPct / 100.0));
        // Estimated charging duration in minutes at standard 120kW DC fast charger
        double chargingTimeMinutes = (energyNeededKwh / CHARGER_OUTPUT_KW) * 60.0;

        List<IncentiveCampaign> activeCampaigns = incentiveCampaignRepository.findByActiveTrue();

        for (ChargingStation station : stations) {
            if ("MAINTENANCE".equalsIgnoreCase(station.getStatus())) {
                continue; // Skip stations under maintenance
            }

            // 1. Calculate distance (Haversine)
            double distance = calculateDistance(userLat, userLng, station.getLatitude(), station.getLongitude());

            // 2. Determine if station is reachable with current range (with 5km safety margin)
            boolean reachable = distance <= (vehicleRange - 5.0);

            // 3. Travel Time in minutes
            double travelTimeMinutes = (distance / AVERAGE_SPEED_KMH) * 60.0;

            // 4. Expected Wait Time from queue calculations
            double expectedWaitTime = station.getExpectedWaitTimeMinutes();

            // 5. Total time = Travel Time + Wait Time + Charging Time
            double totalTimeMinutes = travelTimeMinutes + expectedWaitTime + chargingTimeMinutes;

            // 6. Check for active incentive campaigns diverting FROM a congested station to this station
            double discount = 0.0;
            for (IncentiveCampaign campaign : activeCampaigns) {
                if (campaign.getTargetStation().getId().equals(station.getId())) {
                    // Check if the source station of the campaign is congested and nearby the driver
                    double distToSource = calculateDistance(userLat, userLng, campaign.getSourceStation().getLatitude(), campaign.getSourceStation().getLongitude());
                    if (distToSource < 8.0 && campaign.getSourceStation().getExpectedWaitTimeMinutes() >= campaign.getThresholdWaitTimeMinutes()) {
                        // Apply the discount of this campaign
                        discount = Math.max(discount, campaign.getDiscountPercentage());
                    }
                }
            }

            // Create recommendation notes
            String notes = "";
            if (discount > 0) {
                notes = "Incentive Available: Save " + (int) discount + "% on dynamic charging pricing!";
            } else if (expectedWaitTime > 15) {
                notes = "High congestion predicted. Consider alternative stations.";
            } else if (expectedWaitTime == 0 && station.getAvailableChargers() > 0) {
                notes = "No queue. Chargers available immediately.";
            } else {
                notes = "Normal operational load.";
            }

            results.add(RecommendationResult.builder()
                    .station(station)
                    .distanceKm(Math.round(distance * 100.0) / 100.0)
                    .travelTimeMinutes(Math.round(travelTimeMinutes * 10.0) / 10.0)
                    .expectedWaitTimeMinutes(Math.round(expectedWaitTime * 10.0) / 10.0)
                    .chargingTimeMinutes(Math.round(chargingTimeMinutes * 10.0) / 10.0)
                    .totalTimeMinutes(Math.round(totalTimeMinutes * 10.0) / 10.0)
                    .reachable(reachable)
                    .discountPercentage(discount)
                    .recommendationNotes(notes)
                    .build());
        }

        // Sort by total time ascending
        results.sort(Comparator.comparingDouble(RecommendationResult::getTotalTimeMinutes));

        return results;
    }

    /**
     * Haversine formula to compute distance in kilometers between two points
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radious of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
