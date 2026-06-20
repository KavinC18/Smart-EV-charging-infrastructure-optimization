package com.chargeflow.controller;

import com.chargeflow.dto.IncentiveCampaignResponse;
import com.chargeflow.entity.ChargingStation;
import com.chargeflow.entity.IncentiveCampaign;
import com.chargeflow.exception.ResourceNotFoundException;
import com.chargeflow.repository.ChargingHistoryRepository;
import com.chargeflow.repository.ChargingStationRepository;
import com.chargeflow.repository.IncentiveCampaignRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/operator")
@PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
public class OperatorController {

    private final IncentiveCampaignRepository campaignRepository;
    private final ChargingStationRepository stationRepository;
    private final ChargingHistoryRepository historyRepository;

    public OperatorController(IncentiveCampaignRepository campaignRepository,
                              ChargingStationRepository stationRepository,
                              ChargingHistoryRepository historyRepository) {
        this.campaignRepository = campaignRepository;
        this.stationRepository = stationRepository;
        this.historyRepository = historyRepository;
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<IncentiveCampaignResponse>> getCampaigns() {
        List<IncentiveCampaign> campaigns = campaignRepository.findAll();
        List<IncentiveCampaignResponse> responses = campaigns.stream()
                .map(this::mapToCampaignResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/campaigns")
    public ResponseEntity<IncentiveCampaignResponse> createCampaign(@RequestBody Map<String, Object> payload) {
        Long sourceId = Long.valueOf(payload.get("sourceStationId").toString());
        Long targetId = Long.valueOf(payload.get("targetStationId").toString());
        double discount = Double.parseDouble(payload.get("discountPercentage").toString());
        double threshold = Double.parseDouble(payload.get("thresholdWaitTimeMinutes").toString());
        String description = payload.getOrDefault("description", "").toString();

        ChargingStation source = stationRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source station not found"));
        ChargingStation target = stationRepository.findById(targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Target station not found"));

        IncentiveCampaign campaign = IncentiveCampaign.builder()
                .sourceStation(source)
                .targetStation(target)
                .discountPercentage(discount)
                .thresholdWaitTimeMinutes(threshold)
                .active(true)
                .description(description.isEmpty() ? 
                        String.format("Divert from %s to %s for %d%% off charging fees!", source.getName(), target.getName(), (int) discount) 
                        : description)
                .build();

        IncentiveCampaign saved = campaignRepository.save(campaign);
        return ResponseEntity.ok(mapToCampaignResponse(saved));
    }

    @PostMapping("/campaigns/{id}/deactivate")
    public ResponseEntity<IncentiveCampaignResponse> deactivateCampaign(@PathVariable Long id) {
        IncentiveCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));

        campaign.setActive(false);
        IncentiveCampaign saved = campaignRepository.save(campaign);
        return ResponseEntity.ok(mapToCampaignResponse(saved));
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getSystemAnalytics() {
        List<ChargingStation> stations = stationRepository.findAll();
        
        double totalWaitTime = 0;
        double totalUtilization = 0;
        int activeStationsCount = 0;
        int totalChargers = 0;
        int occupiedChargers = 0;
        int totalQueueLength = 0;

        for (ChargingStation s : stations) {
            totalWaitTime += s.getExpectedWaitTimeMinutes();
            totalUtilization += s.getUtilizationPercentage();
            totalChargers += s.getTotalChargers();
            occupiedChargers += s.getActiveSessions();
            totalQueueLength += s.getQueueLength();
            if (!"MAINTENANCE".equalsIgnoreCase(s.getStatus())) {
                activeStationsCount++;
            }
        }

        double avgWaitTime = stations.isEmpty() ? 0 : totalWaitTime / stations.size();
        double avgUtilization = activeStationsCount == 0 ? 0 : totalUtilization / activeStationsCount;
        long totalCompletedSessions = historyRepository.count();
        long activeCampaigns = campaignRepository.findByActiveTrue().size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("averageWaitTimeMinutes", Math.round(avgWaitTime * 10.0) / 10.0);
        stats.put("averageUtilizationPercentage", Math.round(avgUtilization * 10.0) / 10.0);
        stats.put("totalChargersCount", totalChargers);
        stats.put("occupiedChargersCount", occupiedChargers);
        stats.put("totalQueueLength", totalQueueLength);
        stats.put("totalCompletedSessions", totalCompletedSessions);
        stats.put("activeCampaignsCount", activeCampaigns);
        stats.put("driverSatisfactionScore", 94.0 - (totalQueueLength * 1.5)); // satisfaction score declines as queues grow

        // Mock chart history: hourly load trends (24 points)
        int[] hourlyLoads = new int[24];
        // Populate standard distribution profile
        for (int h = 0; h < 24; h++) {
            if (h >= 8 && h <= 10) hourlyLoads[h] = 60 + h * 2;
            else if (h >= 12 && h <= 14) hourlyLoads[h] = 40 + h;
            else if (h >= 17 && h <= 20) hourlyLoads[h] = 85 - (h - 17) * 5;
            else hourlyLoads[h] = 10 + h % 3 * 5;
        }
        stats.put("hourlyLoadDistribution", hourlyLoads);

        return ResponseEntity.ok(stats);
    }

    private IncentiveCampaignResponse mapToCampaignResponse(IncentiveCampaign campaign) {
        return IncentiveCampaignResponse.builder()
                .id(campaign.getId())
                .sourceStationId(campaign.getSourceStation().getId())
                .sourceStationName(campaign.getSourceStation().getName())
                .targetStationId(campaign.getTargetStation().getId())
                .targetStationName(campaign.getTargetStation().getName())
                .discountPercentage(campaign.getDiscountPercentage())
                .active(campaign.isActive())
                .thresholdWaitTimeMinutes(campaign.getThresholdWaitTimeMinutes())
                .description(campaign.getDescription())
                .build();
    }
}
