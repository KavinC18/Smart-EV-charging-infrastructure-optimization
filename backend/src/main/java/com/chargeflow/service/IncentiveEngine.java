package com.chargeflow.service;

import com.chargeflow.entity.ChargingStation;
import com.chargeflow.entity.IncentiveCampaign;
import com.chargeflow.repository.ChargingStationRepository;
import com.chargeflow.repository.IncentiveCampaignRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class IncentiveEngine {
    private static final Logger logger = LoggerFactory.getLogger(IncentiveEngine.class);

    private final ChargingStationRepository stationRepository;
    private final IncentiveCampaignRepository campaignRepository;
    private final RoutingEngine routingEngine;

    // Thresholds for load balancing
    private static final double CONGESTION_WAIT_THRESHOLD_MINUTES = 15.0;
    private static final double MAX_TARGET_DISTANCE_KM = 8.0;
    private static final double UNDERUTILIZED_WAIT_THRESHOLD_MINUTES = 5.0;

    public IncentiveEngine(ChargingStationRepository stationRepository,
                           IncentiveCampaignRepository campaignRepository,
                           RoutingEngine routingEngine) {
        this.stationRepository = stationRepository;
        this.campaignRepository = campaignRepository;
        this.routingEngine = routingEngine;
    }

    /**
     * Scans all stations, detects congested ones, and creates or updates incentive campaigns
     * directing drivers to nearby under-utilized stations.
     */
    @Transactional
    public List<IncentiveCampaign> runAutoBalancing() {
        List<ChargingStation> stations = stationRepository.findAll();
        List<IncentiveCampaign> newCampaigns = new ArrayList<>();

        // Deactivate old campaigns that are no longer needed
        List<IncentiveCampaign> activeCampaigns = campaignRepository.findByActiveTrue();
        for (IncentiveCampaign campaign : activeCampaigns) {
            ChargingStation source = campaign.getSourceStation();
            if (source.getExpectedWaitTimeMinutes() < CONGESTION_WAIT_THRESHOLD_MINUTES) {
                campaign.setActive(false);
                campaignRepository.save(campaign);
                logger.info("Deactivated campaign {} because source station {} is no longer congested.", 
                        campaign.getId(), source.getName());
            }
        }

        // Detect new congestion and create campaigns
        for (ChargingStation source : stations) {
            if ("MAINTENANCE".equalsIgnoreCase(source.getStatus())) continue;

            if (source.getExpectedWaitTimeMinutes() >= CONGESTION_WAIT_THRESHOLD_MINUTES) {
                // Look for nearby under-utilized target stations
                for (ChargingStation target : stations) {
                    if (source.getId().equals(target.getId())) continue;
                    if ("MAINTENANCE".equalsIgnoreCase(target.getStatus())) continue;

                    if (target.getExpectedWaitTimeMinutes() < UNDERUTILIZED_WAIT_THRESHOLD_MINUTES 
                            && target.getAvailableChargers() > 0) {
                        
                        double distance = routingEngine.calculateDistance(
                                source.getLatitude(), source.getLongitude(),
                                target.getLatitude(), target.getLongitude()
                        );

                        if (distance <= MAX_TARGET_DISTANCE_KM) {
                            // Check if a campaign already exists between these two
                            boolean exists = false;
                            for (IncentiveCampaign campaign : campaignRepository.findByActiveTrue()) {
                                if (campaign.getSourceStation().getId().equals(source.getId()) 
                                        && campaign.getTargetStation().getId().equals(target.getId())) {
                                    exists = true;
                                    break;
                                }
                            }

                            if (!exists) {
                                // Calculate discount dynamically: 10% base + 1% per minute of wait time above threshold, capped at 30%
                                double excessWait = source.getExpectedWaitTimeMinutes() - CONGESTION_WAIT_THRESHOLD_MINUTES;
                                double discount = Math.min(30.0, 10.0 + excessWait * 1.0);
                                // Round to nearest 5% for clean business offerings
                                discount = Math.round(discount / 5.0) * 5.0;

                                IncentiveCampaign campaign = IncentiveCampaign.builder()
                                        .sourceStation(source)
                                        .targetStation(target)
                                        .discountPercentage(discount)
                                        .active(true)
                                        .thresholdWaitTimeMinutes(CONGESTION_WAIT_THRESHOLD_MINUTES)
                                        .description(String.format("Redirect from congested %s (Wait: %d mins) to nearby %s and receive %d%% off charging fees!", 
                                                source.getName(), (int) source.getExpectedWaitTimeMinutes(), target.getName(), (int) discount))
                                        .build();

                                campaignRepository.save(campaign);
                                newCampaigns.add(campaign);
                                logger.info("Created incentive campaign: redirect from {} to {}, discount {}%", 
                                        source.getName(), target.getName(), discount);
                            }
                        }
                    }
                }
            }
        }
        return newCampaigns;
    }
}
