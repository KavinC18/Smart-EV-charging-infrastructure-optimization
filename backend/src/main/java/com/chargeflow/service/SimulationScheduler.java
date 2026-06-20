package com.chargeflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SimulationScheduler implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(SimulationScheduler.class);

    private final SimulationEngine simulationEngine;

    public SimulationScheduler(SimulationEngine simulationEngine) {
        this.simulationEngine = simulationEngine;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing ChargeFlow simulation data on application startup...");
        try {
            simulationEngine.initializeSimulationData();
        } catch (Exception e) {
            logger.error("Error initializing simulation database data: ", e);
        }
    }

    @Scheduled(fixedRate = 5000)
    public void runPeriodicSimulationStep() {
        logger.debug("Ticking grid simulation...");
        try {
            simulationEngine.stepSimulation();
        } catch (Exception e) {
            logger.error("Exception occurred during simulation step: ", e);
        }
    }
}
