package com.iot.simulation;

public class SimulationResult {
    public double makespan;
    public double throughput;
    public double waitingTime;

    public SimulationResult(double makespan, double throughput, double waitingTime) {
        this.makespan = makespan;
        this.throughput = throughput;
        this.waitingTime = waitingTime;
    }
}