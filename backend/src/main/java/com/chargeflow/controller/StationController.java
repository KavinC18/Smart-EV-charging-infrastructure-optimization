package com.chargeflow.controller;

import com.chargeflow.entity.ChargingStation;
import com.chargeflow.exception.ResourceNotFoundException;
import com.chargeflow.repository.ChargingStationRepository;
import com.chargeflow.service.QueuePredictionEngine;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final ChargingStationRepository stationRepository;
    private final QueuePredictionEngine queuePredictionEngine;

    public StationController(ChargingStationRepository stationRepository,
                             QueuePredictionEngine queuePredictionEngine) {
        this.stationRepository = stationRepository;
        this.queuePredictionEngine = queuePredictionEngine;
    }

    @GetMapping
    public List<ChargingStation> getAllStations() {
        return stationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ChargingStation getStationById(@PathVariable Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charging station not found with id: " + id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ChargingStation createStation(@Valid @RequestBody ChargingStation station) {
        // Initial setup
        station.setAvailableChargers(station.getTotalChargers());
        station.setActiveSessions(0);
        station.setQueueLength(0);
        if (station.getAverageChargingDurationMinutes() <= 0) {
            station.setAverageChargingDurationMinutes(45.0);
        }
        station.setStatus("ACTIVE");
        
        queuePredictionEngine.calculateMetrics(station);
        return stationRepository.save(station);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ChargingStation> updateStation(@PathVariable Long id, @Valid @RequestBody ChargingStation stationDetails) {
        ChargingStation station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charging station not found with id: " + id));

        station.setName(stationDetails.getName());
        station.setLatitude(stationDetails.getLatitude());
        station.setLongitude(stationDetails.getLongitude());
        
        // Handle changes in total chargers
        int diff = stationDetails.getTotalChargers() - station.getTotalChargers();
        station.setTotalChargers(stationDetails.getTotalChargers());
        station.setAvailableChargers(Math.max(0, station.getAvailableChargers() + diff));
        
        station.setAverageChargingDurationMinutes(stationDetails.getAverageChargingDurationMinutes());
        station.setStatus(stationDetails.getStatus());
        station.setDynamicPricingPerKwh(stationDetails.getDynamicPricingPerKwh());

        queuePredictionEngine.calculateMetrics(station);
        ChargingStation updatedStation = stationRepository.save(station);
        return ResponseEntity.ok(updatedStation);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteStation(@PathVariable Long id) {
        ChargingStation station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charging station not found with id: " + id));

        stationRepository.delete(station);
        return ResponseEntity.ok().body("Station deleted successfully.");
    }
}
