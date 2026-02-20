package com.iot.optimization;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CPOAOptimizer {

    private Random random = new Random();

    public AllocationResult optimize(List<Cloudlet> tasks) {

        List<Cloudlet> edgeTasks = new ArrayList<>();
        List<Cloudlet> cloudTasks = new ArrayList<>();

        for (Cloudlet task : tasks) {

            double fitness = calculateFitness(task);

            if (fitness < 0.5) {
                edgeTasks.add(task);
            } else {
                cloudTasks.add(task);
            }
        }

        return new AllocationResult(edgeTasks, cloudTasks);
    }

    private double calculateFitness(Cloudlet task) {

        double taskLengthFactor = task.getLength() / 50000.0;
        double randomness = random.nextDouble() * 0.3;

        return taskLengthFactor + randomness;
    }

    public static class AllocationResult {
        public List<Cloudlet> edgeTasks;
        public List<Cloudlet> cloudTasks;

        public AllocationResult(List<Cloudlet> edgeTasks, List<Cloudlet> cloudTasks) {
            this.edgeTasks = edgeTasks;
            this.cloudTasks = cloudTasks;
        }
    }
}