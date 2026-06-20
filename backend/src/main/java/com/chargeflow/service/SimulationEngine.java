package com.chargeflow.service;

import com.chargeflow.entity.*;
import com.chargeflow.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class SimulationEngine {
    private static final Logger logger = LoggerFactory.getLogger(SimulationEngine.class);

    private final ChargingStationRepository stationRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ChargingSessionRepository sessionRepository;
    private final ChargingHistoryRepository historyRepository;
    private final QueuePredictionEngine queuePredictionEngine;
    private final IncentiveEngine incentiveEngine;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    // Simulation settings
    private boolean running = true;
    private int simulationSpeed = 12; // 1 second real-time = 12 seconds sim-time (1 min per 5 sec)
    private String currentScenario = "NORMAL"; // "NORMAL", "MORNING_PEAK", "EVENING_PEAK", "WEEKEND_LOAD", "OUTAGE"
    private int simulatedHour = 8;
    private int simulatedMinute = 0;

    public SimulationEngine(ChargingStationRepository stationRepository,
                            UserRepository userRepository,
                            VehicleRepository vehicleRepository,
                            ChargingSessionRepository sessionRepository,
                            ChargingHistoryRepository historyRepository,
                            QueuePredictionEngine queuePredictionEngine,
                            IncentiveEngine incentiveEngine,
                            PasswordEncoder passwordEncoder) {
        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.sessionRepository = sessionRepository;
        this.historyRepository = historyRepository;
        this.queuePredictionEngine = queuePredictionEngine;
        this.incentiveEngine = incentiveEngine;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void initializeSimulationData() {
        if (stationRepository.count() > 0) {
            return; // Data already exists
        }

        logger.info("Initializing mock database data for ChargeFlow simulation...");

        // 1. Create Users
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .email("admin@chargeflow.io")
                .role(UserRole.ROLE_ADMIN)
                .build();
        userRepository.save(admin);

        User operator = User.builder()
                .username("operator")
                .password(passwordEncoder.encode("operator123"))
                .email("operator@chargeflow.io")
                .role(UserRole.ROLE_OPERATOR)
                .build();
        userRepository.save(operator);

        // Drivers
        String[] driverNames = {"driver_kavin", "ev_alex", "spark_emma", "tesla_sophia", "volt_john"};
        for (String name : driverNames) {
            User driver = User.builder()
                    .username(name)
                    .password(passwordEncoder.encode("driver123"))
                    .email(name + "@chargeflow.io")
                    .role(UserRole.ROLE_DRIVER)
                    .build();
            userRepository.save(driver);

            // Add vehicle details
            Vehicle vehicle = Vehicle.builder()
                    .user(driver)
                    .model(name.contains("tesla") ? "Tesla Model Y" : "Hyundai Ioniq 5")
                    .batteryCapacityKwh(name.contains("tesla") ? 75.0 : 58.0)
                    .currentBatteryLevelPct(20.0 + random.nextDouble() * 30.0)
                    .currentRangeKm(100.0 + random.nextDouble() * 150.0)
                    .build();
            vehicleRepository.save(vehicle);
        }

        // 2. Create Stations (Centered around Coimbatore downtown grid)
        String[] stationNames = {
                "Gandhipuram Grid Hub",
                "Peelamedu Tech Node",
                "Saravanampatti IT Park Charger",
                "RS Puram Fast Charge Plaza",
                "Singanallur Transport Hub"
        };
        double[] lats = {11.0173, 11.0264, 11.0792, 11.0116, 11.0028};
        double[] lngs = {76.9691, 76.9961, 76.9997, 76.9452, 77.0225};
        int[] chargersCount = {8, 12, 6, 10, 4};
        double[] basePrices = {0.32, 0.45, 0.28, 0.38, 0.35};

        for (int i = 0; i < stationNames.length; i++) {
            ChargingStation station = ChargingStation.builder()
                    .name(stationNames[i])
                    .latitude(lats[i])
                    .longitude(lngs[i])
                    .totalChargers(chargersCount[i])
                    .availableChargers(chargersCount[i])
                    .activeSessions(0)
                    .queueLength(0)
                    .averageChargingDurationMinutes(40.0 + random.nextInt(15))
                    .arrivalRate(0.0) // calculated dynamically
                    .serviceRate(1.0 / 45.0)
                    .utilizationPercentage(0.0)
                    .expectedWaitTimeMinutes(0.0)
                    .waitProbability(0.0)
                    .status("ACTIVE")
                    .dynamicPricingPerKwh(basePrices[i])
                    .build();
            
            stationRepository.save(station);
        }
        
        logger.info("Initialized simulation data successfully!");
    }

    /**
     * Triggered every 5 seconds (real-time) to step the simulation forward
     */
    @Transactional
    public void stepSimulation() {
        if (!running) return;

        // Step 1: Advance Simulated Time
        simulatedMinute += (simulationSpeed / 12); // e.g. add 1 minute
        if (simulatedMinute >= 60) {
            simulatedHour = (simulatedHour + simulatedMinute / 60) % 24;
            simulatedMinute %= 60;
        }

        // Step 2: Retrieve all stations and users
        List<ChargingStation> stations = stationRepository.findAll();
        List<User> drivers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ROLE_DRIVER)
                .toList();

        if (stations.isEmpty() || drivers.isEmpty()) return;

        // Step 3: Complete active sessions based on time probability
        List<ChargingSession> activeSessions = sessionRepository.findByStatus("ACTIVE");
        for (ChargingSession session : activeSessions) {
            // Simulated charging time: average charging duration determines completion probability
            double completeProbability = 1.0 / session.getStation().getAverageChargingDurationMinutes();
            // Scale probability to match the simulation speed step size (simulating ~1 minute of real time)
            if (random.nextDouble() < (completeProbability * (simulationSpeed / 12.0))) {
                completeChargingSession(session);
            }
        }

        // Step 4: Simulate new vehicle arrivals based on Hour and Scenario
        double arrivalMultiplier = getArrivalMultiplierForTimeAndScenario();
        
        for (ChargingStation station : stations) {
            if ("MAINTENANCE".equalsIgnoreCase(station.getStatus())) {
                station.setArrivalRate(0.0);
                stationRepository.save(station);
                continue;
            }

            // Base arrival rate (vehicles/minute) is proportional to station location & size
            double baseArrivalRate = (station.getTotalChargers() * 0.015); // e.g., 8-charger station has 0.12 arrivals/min (1 every 8 mins)
            double currentArrivalRate = baseArrivalRate * arrivalMultiplier;
            station.setArrivalRate(currentArrivalRate);

            // Calculate dynamic pricing based on utilization
            double utilizationRatio = (double) station.getActiveSessions() / station.getTotalChargers();
            double basePrice = 0.30;
            double surgeMultiplier = 1.0 + (utilizationRatio * 0.6); // up to 60% surge
            if ("MORNING_PEAK".equals(currentScenario) || "EVENING_PEAK".equals(currentScenario)) {
                surgeMultiplier += 0.2;
            }
            station.setDynamicPricingPerKwh(Math.round(basePrice * surgeMultiplier * 100.0) / 100.0);

            // Vehicle arrival check
            // Probability of arrival in this simulation step
            double stepArrivalProb = currentArrivalRate * (simulationSpeed / 12.0);
            if (random.nextDouble() < stepArrivalProb) {
                // Generate a session or add to queue
                User randomDriver = drivers.get(random.nextInt(drivers.size()));
                List<Vehicle> driverVehicles = vehicleRepository.findByUserId(randomDriver.getId());
                if (!driverVehicles.isEmpty()) {
                    Vehicle vehicle = driverVehicles.get(0);
                    
                    if (station.getAvailableChargers() > 0) {
                        // Charger available: start charging immediately
                        startChargingSession(randomDriver, station, vehicle);
                    } else {
                        // Congested: add vehicle to queue
                        station.setQueueLength(station.getQueueLength() + 1);
                        stationRepository.save(station);
                        logger.info("Vehicle queued at station {}. New queue length: {}", station.getName(), station.getQueueLength());
                    }
                }
            }

            // Step 5: Handle queue release (if charger becomes free and queue is not empty)
            while (station.getAvailableChargers() > 0 && station.getQueueLength() > 0) {
                station.setQueueLength(station.getQueueLength() - 1);
                User randomDriver = drivers.get(random.nextInt(drivers.size()));
                List<Vehicle> driverVehicles = vehicleRepository.findByUserId(randomDriver.getId());
                if (!driverVehicles.isEmpty()) {
                    startChargingSession(randomDriver, station, driverVehicles.get(0));
                }
            }

            stationRepository.save(station);
        }

        // Step 6: Recalculate Erlang-C Queue Metrics for all stations
        queuePredictionEngine.recomputeAllStations();

        // Step 7: Run Auto-Balancing Incentive Campaigns
        incentiveEngine.runAutoBalancing();
    }

    private void startChargingSession(User driver, ChargingStation station, Vehicle vehicle) {
        if (station.getAvailableChargers() <= 0) return;

        station.setAvailableChargers(station.getAvailableChargers() - 1);
        station.setActiveSessions(station.getActiveSessions() + 1);
        stationRepository.save(station);

        ChargingSession session = ChargingSession.builder()
                .user(driver)
                .station(station)
                .vehicle(vehicle)
                .startTime(LocalDateTime.now())
                .energyDeliveredKwh(0.0)
                .cost(0.0)
                .status("ACTIVE")
                .build();
        sessionRepository.save(session);
        logger.info("Started active session for {} at station {}", driver.getUsername(), station.getName());
    }

    private void completeChargingSession(ChargingSession session) {
        ChargingStation station = session.getStation();
        
        station.setAvailableChargers(Math.min(station.getTotalChargers(), station.getAvailableChargers() + 1));
        station.setActiveSessions(Math.max(0, station.getActiveSessions() - 1));
        stationRepository.save(station);

        // Calculate energy metrics
        double chargeDurationMinutes = 15.0 + random.nextInt(35); // simulated duration in mins
        double chargerKw = 120.0;
        double energyDelivered = (chargerKw * (chargeDurationMinutes / 60.0));
        double pricePerKwh = station.getDynamicPricingPerKwh();
        double totalCost = energyDelivered * pricePerKwh;

        session.setEndTime(LocalDateTime.now());
        session.setEnergyDeliveredKwh(Math.round(energyDelivered * 100.0) / 100.0);
        session.setCost(Math.round(totalCost * 100.0) / 100.0);
        session.setStatus("COMPLETED");
        sessionRepository.save(session);

        // Add history record
        ChargingHistory history = ChargingHistory.builder()
                .user(session.getUser())
                .station(station)
                .date(LocalDateTime.now())
                .durationMinutes(chargeDurationMinutes)
                .energyDeliveredKwh(session.getEnergyDeliveredKwh())
                .cost(session.getCost())
                .build();
        historyRepository.save(history);

        logger.info("Completed session for {} at station {}. Cost: ${}", 
                session.getUser().getUsername(), station.getName(), session.getCost());
    }

    private double getArrivalMultiplierForTimeAndScenario() {
        // Hourly multiplier profile (peaks at 8-10 AM and 5-7 PM)
        double hourlyMultiplier = 0.3; // off-peak base
        if (simulatedHour >= 8 && simulatedHour <= 10) hourlyMultiplier = 1.8;
        else if (simulatedHour >= 12 && simulatedHour <= 14) hourlyMultiplier = 1.0;
        else if (simulatedHour >= 17 && simulatedHour <= 20) hourlyMultiplier = 2.2;
        else if (simulatedHour >= 21 || simulatedHour <= 6) hourlyMultiplier = 0.2;

        // Scenario multiplier profile
        double scenarioMultiplier = 1.0;
        switch (currentScenario) {
            case "MORNING_PEAK":
                scenarioMultiplier = 2.0;
                break;
            case "EVENING_PEAK":
                scenarioMultiplier = 2.5;
                break;
            case "WEEKEND_LOAD":
                scenarioMultiplier = 1.5;
                break;
            case "OUTAGE":
                scenarioMultiplier = 1.2;
                break;
            case "NORMAL":
            default:
                scenarioMultiplier = 1.0;
                break;
        }

        return hourlyMultiplier * scenarioMultiplier;
    }

    // Controls
    public void toggleRunning() {
        this.running = !this.running;
        logger.info("Simulation running toggled to: {}", this.running);
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    public void setSimulationSpeed(int speed) {
        this.simulationSpeed = speed;
    }

    public int getSimulationSpeed() {
        return simulationSpeed;
    }

    public void setCurrentScenario(String scenario) {
        this.currentScenario = scenario;
        logger.info("Simulation scenario changed to: {}", scenario);
        if ("OUTAGE".equals(scenario)) {
            // Simulate Saravanampatti IT Park Charger breaking down (under maintenance)
            List<ChargingStation> stations = stationRepository.findAll();
            if (stations.size() >= 3) {
                ChargingStation station = stations.get(2);
                station.setStatus("MAINTENANCE");
                station.setAvailableChargers(0);
                station.setActiveSessions(0);
                station.setQueueLength(0);
                stationRepository.save(station);
                logger.warn("Saravanampatti IT Park Charger is set to MAINTENANCE due to OUTAGE scenario!");
            }
        } else {
            // Restore all stations to ACTIVE
            List<ChargingStation> stations = stationRepository.findAll();
            for (ChargingStation station : stations) {
                if ("MAINTENANCE".equals(station.getStatus())) {
                    station.setStatus("ACTIVE");
                    station.setAvailableChargers(station.getTotalChargers());
                    stationRepository.save(station);
                }
            }
        }
    }

    public String getCurrentScenario() {
        return currentScenario;
    }

    public int getSimulatedHour() {
        return simulatedHour;
    }

    public int getSimulatedMinute() {
        return simulatedMinute;
    }

    public void setSimulatedTime(int hour, int minute) {
        this.simulatedHour = hour % 24;
        this.simulatedMinute = minute % 60;
    }
}
