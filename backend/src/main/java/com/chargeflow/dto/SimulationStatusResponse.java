package com.chargeflow.dto;

public class SimulationStatusResponse {
    private boolean running;
    private int speed;
    private String scenario;
    private String simulatedTime;

    public SimulationStatusResponse() {}

    public SimulationStatusResponse(boolean running, int speed, String scenario, String simulatedTime) {
        this.running = running;
        this.speed = speed;
        this.scenario = scenario;
        this.simulatedTime = simulatedTime;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getSimulatedTime() {
        return simulatedTime;
    }

    public void setSimulatedTime(String simulatedTime) {
        this.simulatedTime = simulatedTime;
    }

    public static SimulationStatusResponseBuilder builder() {
        return new SimulationStatusResponseBuilder();
    }

    public static class SimulationStatusResponseBuilder {
        private boolean running;
        private int speed;
        private String scenario;
        private String simulatedTime;

        public SimulationStatusResponseBuilder running(boolean running) {
            this.running = running;
            return this;
        }

        public SimulationStatusResponseBuilder speed(int speed) {
            this.speed = speed;
            return this;
        }

        public SimulationStatusResponseBuilder scenario(String scenario) {
            this.scenario = scenario;
            return this;
        }

        public SimulationStatusResponseBuilder simulatedTime(String simulatedTime) {
            this.simulatedTime = simulatedTime;
            return this;
        }

        public SimulationStatusResponse build() {
            return new SimulationStatusResponse(running, speed, scenario, simulatedTime);
        }
    }
}
