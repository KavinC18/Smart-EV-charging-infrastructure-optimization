package com.chargeflow.controller;

import com.chargeflow.dto.ChargingHistoryResponse;
import com.chargeflow.dto.ChargingSessionResponse;
import com.chargeflow.dto.VehicleRequest;
import com.chargeflow.entity.*;
import com.chargeflow.exception.BadRequestException;
import com.chargeflow.exception.ResourceNotFoundException;
import com.chargeflow.repository.*;
import com.chargeflow.security.UserPrincipal;
import com.chargeflow.service.QueuePredictionEngine;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ChargingStationRepository stationRepository;
    private final ChargingSessionRepository sessionRepository;
    private final ChargingHistoryRepository historyRepository;
    private final QueuePredictionEngine queuePredictionEngine;

    private final Random random = new Random();

    public DriverController(UserRepository userRepository,
                            VehicleRepository vehicleRepository,
                            ChargingStationRepository stationRepository,
                            ChargingSessionRepository sessionRepository,
                            ChargingHistoryRepository historyRepository,
                            QueuePredictionEngine queuePredictionEngine) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.stationRepository = stationRepository;
        this.sessionRepository = sessionRepository;
        this.historyRepository = historyRepository;
        this.queuePredictionEngine = queuePredictionEngine;
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getMyVehicles(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Vehicle> vehicles = vehicleRepository.findByUserId(userPrincipal.getId());
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/vehicles")
    public ResponseEntity<Vehicle> addVehicle(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                             @Valid @RequestBody VehicleRequest request) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Vehicle vehicle = Vehicle.builder()
                .user(user)
                .model(request.getModel())
                .batteryCapacityKwh(request.getBatteryCapacityKwh())
                .currentBatteryLevelPct(request.getCurrentBatteryLevelPct())
                .currentRangeKm(request.getCurrentRangeKm())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChargingHistoryResponse>> getMyHistory(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ChargingHistory> histories = historyRepository.findByUserId(userPrincipal.getId());
        List<ChargingHistoryResponse> responses = histories.stream()
                .map(this::mapToHistoryResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/active-sessions")
    public ResponseEntity<List<ChargingSessionResponse>> getActiveSessions(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ChargingSession> sessions = sessionRepository.findByUserIdAndStatus(userPrincipal.getId(), "ACTIVE");
        List<ChargingSessionResponse> responses = sessions.stream()
                .map(this::mapToSessionResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/start-charge")
    public ResponseEntity<ChargingSessionResponse> startCharge(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody java.util.Map<String, Long> payload) {
        
        Long stationId = payload.get("stationId");
        Long vehicleId = payload.get("vehicleId");

        if (stationId == null || vehicleId == null) {
            throw new BadRequestException("Station ID and Vehicle ID must be provided.");
        }

        ChargingStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging station not found."));

        if ("MAINTENANCE".equalsIgnoreCase(station.getStatus())) {
            throw new BadRequestException("This station is currently under maintenance.");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found."));

        if (!vehicle.getUser().getId().equals(userPrincipal.getId())) {
            throw new BadRequestException("You do not own this vehicle.");
        }

        // Check if there is already an active session for this user
        List<ChargingSession> activeSessions = sessionRepository.findByUserIdAndStatus(userPrincipal.getId(), "ACTIVE");
        if (!activeSessions.isEmpty()) {
            throw new BadRequestException("You already have an active charging session.");
        }

        if (station.getAvailableChargers() <= 0) {
            // Add to queue
            station.setQueueLength(station.getQueueLength() + 1);
            stationRepository.save(station);
            queuePredictionEngine.recomputeAllStations();
            throw new BadRequestException("Chargers are fully occupied! You have been placed in the queue.");
        }

        station.setAvailableChargers(station.getAvailableChargers() - 1);
        station.setActiveSessions(station.getActiveSessions() + 1);
        stationRepository.save(station);

        User user = userRepository.findById(userPrincipal.getId()).orElseThrow();
        ChargingSession session = ChargingSession.builder()
                .user(user)
                .station(station)
                .vehicle(vehicle)
                .startTime(LocalDateTime.now())
                .energyDeliveredKwh(0.0)
                .cost(0.0)
                .status("ACTIVE")
                .build();

        ChargingSession saved = sessionRepository.save(session);
        queuePredictionEngine.recomputeAllStations();

        return ResponseEntity.ok(mapToSessionResponse(saved));
    }

    @PostMapping("/stop-charge")
    public ResponseEntity<ChargingSessionResponse> stopCharge(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody java.util.Map<String, Long> payload) {
        
        Long sessionId = payload.get("sessionId");
        if (sessionId == null) {
            throw new BadRequestException("Session ID is required.");
        }

        ChargingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Charging session not found."));

        if (!session.getUser().getId().equals(userPrincipal.getId())) {
            throw new BadRequestException("You are not authorized to stop this session.");
        }

        if ("COMPLETED".equals(session.getStatus())) {
            throw new BadRequestException("Session is already completed.");
        }

        ChargingStation station = session.getStation();
        station.setAvailableChargers(Math.min(station.getTotalChargers(), station.getAvailableChargers() + 1));
        station.setActiveSessions(Math.max(0, station.getActiveSessions() - 1));
        stationRepository.save(station);

        // Compute simulated statistics
        double durationMins = java.time.Duration.between(session.getStartTime(), LocalDateTime.now()).toSeconds() / 60.0;
        if (durationMins < 1.0) durationMins = 5.0; // minimum duration for simulation display
        double energyDelivered = 120.0 * (durationMins / 60.0); // 120kW fast charge speed
        double cost = energyDelivered * station.getDynamicPricingPerKwh();

        session.setEndTime(LocalDateTime.now());
        session.setEnergyDeliveredKwh(Math.round(energyDelivered * 100.0) / 100.0);
        session.setCost(Math.round(cost * 100.0) / 100.0);
        session.setStatus("COMPLETED");
        sessionRepository.save(session);

        // Add history record
        ChargingHistory history = ChargingHistory.builder()
                .user(session.getUser())
                .station(station)
                .date(LocalDateTime.now())
                .durationMinutes(durationMins)
                .energyDeliveredKwh(session.getEnergyDeliveredKwh())
                .cost(session.getCost())
                .build();
        historyRepository.save(history);

        // Release first vehicle from queue if any
        if (station.getQueueLength() > 0) {
            station.setQueueLength(station.getQueueLength() - 1);
            station.setAvailableChargers(station.getAvailableChargers() - 1);
            station.setActiveSessions(station.getActiveSessions() + 1);
            stationRepository.save(station);

            // Fetch a random driver to simulate queue release
            List<User> drivers = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.ROLE_DRIVER)
                    .toList();
            if (!drivers.isEmpty()) {
                User driver = drivers.get(random.nextInt(drivers.size()));
                List<Vehicle> dv = vehicleRepository.findByUserId(driver.getId());
                if (!dv.isEmpty()) {
                    ChargingSession nextSession = ChargingSession.builder()
                            .user(driver)
                            .station(station)
                            .vehicle(dv.get(0))
                            .startTime(LocalDateTime.now())
                            .energyDeliveredKwh(0.0)
                            .cost(0.0)
                            .status("ACTIVE")
                            .build();
                    sessionRepository.save(nextSession);
                }
            }
        }

        queuePredictionEngine.recomputeAllStations();

        return ResponseEntity.ok(mapToSessionResponse(session));
    }

    private ChargingSessionResponse mapToSessionResponse(ChargingSession session) {
        return ChargingSessionResponse.builder()
                .id(session.getId())
                .userId(session.getUser().getId())
                .username(session.getUser().getUsername())
                .stationId(session.getStation().getId())
                .stationName(session.getStation().getName())
                .vehicleId(session.getVehicle().getId())
                .vehicleModel(session.getVehicle().getModel())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .energyDeliveredKwh(session.getEnergyDeliveredKwh())
                .cost(session.getCost())
                .status(session.getStatus())
                .build();
    }

    private ChargingHistoryResponse mapToHistoryResponse(ChargingHistory history) {
        return ChargingHistoryResponse.builder()
                .id(history.getId())
                .userId(history.getUser().getId())
                .username(history.getUser().getUsername())
                .stationId(history.getStation().getId())
                .stationName(history.getStation().getName())
                .date(history.getDate())
                .durationMinutes(Math.round(history.getDurationMinutes() * 10.0) / 10.0)
                .energyDeliveredKwh(history.getEnergyDeliveredKwh())
                .cost(history.getCost())
                .build();
    }
}
