package com.chargeflow.service;

import com.chargeflow.entity.ChargingStation;
import com.chargeflow.repository.ChargingStationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class QueuePredictionEngine {
    private static final Logger logger = LoggerFactory.getLogger(QueuePredictionEngine.class);
    private final ChargingStationRepository stationRepository;

    public QueuePredictionEngine(ChargingStationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    /**
     * Recomputes queue metrics for a specific station based on total chargers, arrival rate, and service rate.
     */
    public void calculateMetrics(ChargingStation station) {
        int c = station.getTotalChargers();
        
        // If average charging duration is D minutes, service rate mu = 1 / D per minute.
        // If average duration is 0, set to some default like 45 minutes
        double avgDuration = station.getAverageChargingDurationMinutes();
        if (avgDuration <= 0) {
            avgDuration = 45.0;
            station.setAverageChargingDurationMinutes(avgDuration);
        }
        double mu = 1.0 / avgDuration;
        station.setServiceRate(mu);

        // Arrival rate lambda is stored in cars per minute (simulated).
        double lambda = station.getArrivalRate();
        if (lambda < 0) {
            lambda = 0.0;
            station.setArrivalRate(lambda);
        }

        if (c <= 0) {
            station.setUtilizationPercentage(0.0);
            station.setExpectedWaitTimeMinutes(0.0);
            station.setWaitProbability(0.0);
            station.setQueueLength(0);
            return;
        }

        // Intensity u = lambda / mu
        double u = lambda / mu;
        // Utilization rho = lambda / (c * mu) = u / c
        double rho = u / c;
        station.setUtilizationPercentage(Math.min(1.0, rho) * 100.0); // as percentage

        double pw;
        double waitTime;
        int estimatedQueueLength;

        if (rho >= 0.99) {
            // Unstable or critical state
            pw = 1.0;
            // Expected wait time is high, penalized by excess load
            double excessArrival = lambda - (c * mu);
            waitTime = 60.0 + Math.max(0.0, excessArrival * 60.0); // e.g., base 60 mins + scaling
            estimatedQueueLength = (int) Math.round(lambda * waitTime);
        } else {
            // Stable state, compute Erlang-C
            double num = (Math.pow(u, c) / factorial(c)) * (1.0 / (1.0 - rho));
            double den = 0.0;
            for (int k = 0; k < c; k++) {
                den += Math.pow(u, k) / factorial(k);
            }
            den += num;

            pw = num / den;
            pw = Math.min(1.0, Math.max(0.0, pw));
            
            // Expected waiting time in queue (minutes): Wq = Pw * (1 / (c*mu - lambda))
            waitTime = pw * (1.0 / (c * mu - lambda));
            waitTime = Math.max(0.0, waitTime);
            
            // Expected queue length: Lq = lambda * Wq
            double lq = lambda * waitTime;
            estimatedQueueLength = (int) Math.round(lq);
        }

        station.setWaitProbability(pw);
        station.setExpectedWaitTimeMinutes(waitTime);
        // Queue length is either current active queue size from simulation, or predicted Erlang-C queue length
        // We will combine them or set queue length to the estimated queue length
        station.setQueueLength(Math.max(station.getQueueLength(), estimatedQueueLength));
    }

    /**
     * Scheduled trigger to recalculate metrics for all active stations
     */
    @Transactional
    public void recomputeAllStations() {
        List<ChargingStation> stations = stationRepository.findAll();
        for (ChargingStation station : stations) {
            calculateMetrics(station);
            stationRepository.save(station);
        }
        logger.debug("Recomputed M/M/c queue metrics for {} stations", stations.size());
    }

    private double factorial(int n) {
        double fact = 1.0;
        for (int i = 1; i <= n; i++) {
            fact *= i;
        }
        return fact;
    }
}
