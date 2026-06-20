package com.chargeflow.controller;

import com.chargeflow.dto.SimulationStatusResponse;
import com.chargeflow.entity.ChargingStation;
import com.chargeflow.entity.User;
import com.chargeflow.entity.Vehicle;
import com.chargeflow.exception.ResourceNotFoundException;
import com.chargeflow.repository.*;
import com.chargeflow.service.QueuePredictionEngine;
import com.chargeflow.service.SimulationEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final SimulationEngine simulationEngine;
    private final ChargingStationRepository stationRepository;
    private final ChargingSessionRepository sessionRepository;
    private final ChargingHistoryRepository historyRepository;
    private final IncentiveCampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final QueuePredictionEngine queuePredictionEngine;

    private final Random random = new Random();

    public SimulationController(SimulationEngine simulationEngine,
                                ChargingStationRepository stationRepository,
                                ChargingSessionRepository sessionRepository,
                                ChargingHistoryRepository historyRepository,
                                IncentiveCampaignRepository campaignRepository,
                                UserRepository userRepository,
                                VehicleRepository vehicleRepository,
                                QueuePredictionEngine queuePredictionEngine) {
        this.simulationEngine = simulationEngine;
        this.stationRepository = stationRepository;
        this.sessionRepository = sessionRepository;
        this.historyRepository = historyRepository;
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.queuePredictionEngine = queuePredictionEngine;
    }

    @GetMapping("/status")
    public ResponseEntity<SimulationStatusResponse> getStatus() {
        String simTime = String.format("%02d:%02d", 
                simulationEngine.getSimulatedHour(), 
                simulationEngine.getSimulatedMinute());
        
        return ResponseEntity.ok(SimulationStatusResponse.builder()
                .running(simulationEngine.isRunning())
                .speed(simulationEngine.getSimulationSpeed())
                .scenario(simulationEngine.getCurrentScenario())
                .simulatedTime(simTime)
                .build());
    }

    @PostMapping("/toggle")
    public ResponseEntity<Boolean> toggleSimulation() {
        simulationEngine.toggleRunning();
        return ResponseEntity.ok(simulationEngine.isRunning());
    }

    @PostMapping("/scenario")
    public ResponseEntity<String> setScenario(@RequestBody Map<String, String> body) {
        String scenario = body.getOrDefault("scenario", "NORMAL");
        simulationEngine.setCurrentScenario(scenario);
        return ResponseEntity.ok(simulationEngine.getCurrentScenario());
    }

    @PostMapping("/speed")
    public ResponseEntity<Integer> setSpeed(@RequestBody Map<String, Integer> body) {
        int speed = body.getOrDefault("speed", 12);
        simulationEngine.setSimulationSpeed(speed);
        return ResponseEntity.ok(simulationEngine.getSimulationSpeed());
    }

    @PostMapping("/trigger-arrival")
    public ResponseEntity<String> triggerArrival(@RequestBody Map<String, Long> body) {
        Long stationId = body.get("stationId");
        ChargingStation station = stationRepository.findById(stationId)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found"));

        if ("MAINTENANCE".equalsIgnoreCase(station.getStatus())) {
            return ResponseEntity.badRequest().body("Cannot trigger arrival. Station is under maintenance.");
        }

        // Fetch a random driver
        List<User> drivers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == com.chargeflow.entity.UserRole.ROLE_DRIVER)
                .toList();

        if (drivers.isEmpty()) {
            return ResponseEntity.badRequest().body("No driver accounts found to simulate arrival.");
        }

        User randomDriver = drivers.get(random.nextInt(drivers.size()));
        List<Vehicle> vehicles = vehicleRepository.findByUserId(randomDriver.getId());
        if (vehicles.isEmpty()) {
            return ResponseEntity.badRequest().body("No vehicles registered for driver.");
        }

        if (station.getAvailableChargers() > 0) {
            // Occupy a charger
            station.setAvailableChargers(station.getAvailableChargers() - 1);
            station.setActiveSessions(station.getActiveSessions() + 1);
            stationRepository.save(station);

            com.chargeflow.entity.ChargingSession session = com.chargeflow.entity.ChargingSession.builder()
                    .user(randomDriver)
                    .station(station)
                    .vehicle(vehicles.get(0))
                    .startTime(java.time.LocalDateTime.now())
                    .energyDeliveredKwh(0.0)
                    .cost(0.0)
                    .status("ACTIVE")
                    .build();
            sessionRepository.save(session);
            queuePredictionEngine.recomputeAllStations();
            return ResponseEntity.ok("Vehicle connected to charger directly.");
        } else {
            // Put in queue
            station.setQueueLength(station.getQueueLength() + 1);
            stationRepository.save(station);
            queuePredictionEngine.recomputeAllStations();
            return ResponseEntity.ok("Chargers full. Vehicle added to station queue.");
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetSimulation() {
        // Clear dynamic tables
        campaignRepository.deleteAll();
        sessionRepository.deleteAll();
        historyRepository.deleteAll();
        vehicleRepository.deleteAll();
        stationRepository.deleteAll();
        userRepository.deleteAll();

        // Reseed
        simulationEngine.setRunning(true);
        simulationEngine.setCurrentScenario("NORMAL");
        simulationEngine.setSimulationSpeed(12);
        simulationEngine.setSimulatedTime(8, 0);
        simulationEngine.initializeSimulationData();

        return ResponseEntity.ok("Simulation database reset and seeded successfully.");
    }
}
