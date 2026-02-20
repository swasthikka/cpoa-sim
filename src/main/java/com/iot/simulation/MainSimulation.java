package com.iot.simulation;

import com.iot.infrastructure.InfrastructureBuilder;
import com.iot.infrastructure.VmGenerator;
import com.iot.tasks.TaskGenerator;
import com.iot.optimization.CPOAOptimizer;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.SwingWrapper;

import java.util.ArrayList;
import java.util.List;

public class MainSimulation {

    public static void main(String[] args) {

        SimulationResult edge = runSimulation("EDGE");
        SimulationResult cloud = runSimulation("CLOUD");
        SimulationResult cpoa = runSimulation("CPOA");

        generateGraphs(edge, cloud, cpoa);
    }

    private static SimulationResult runSimulation(String mode) {

        CloudSim simulation = new CloudSim();

        InfrastructureBuilder builder = new InfrastructureBuilder();
        var edgeDC = builder.createEdgeDatacenter(simulation, 40);
        var cloudDC = builder.createCloudDatacenter(simulation);

        DatacenterBrokerSimple edgeBroker = new DatacenterBrokerSimple(simulation);
        DatacenterBrokerSimple cloudBroker = new DatacenterBrokerSimple(simulation);

        VmGenerator vmGenerator = new VmGenerator();

        edgeBroker.submitVmList(vmGenerator.createVMs(20, 10000));
        cloudBroker.submitVmList(vmGenerator.createVMs(10, 30000));

        TaskGenerator generator = new TaskGenerator();
        List<Cloudlet> tasks = generator.createTasks(100);

        if (mode.equals("EDGE")) {
            edgeBroker.submitCloudletList(tasks);
        }
        else if (mode.equals("CLOUD")) {
            cloudBroker.submitCloudletList(tasks);
        }
        else {
            CPOAOptimizer optimizer = new CPOAOptimizer();
            var result = optimizer.optimize(tasks);

            edgeBroker.submitCloudletList(result.edgeTasks);
            cloudBroker.submitCloudletList(result.cloudTasks);
        }

        simulation.start();

        List<Cloudlet> finished = new ArrayList<>();
        finished.addAll(edgeBroker.getCloudletFinishedList());
        finished.addAll(cloudBroker.getCloudletFinishedList());

        double makespan = finished.stream()
                .mapToDouble(Cloudlet::getFinishTime)
                .max().orElse(0);

        double waiting = finished.stream()
                .mapToDouble(Cloudlet::getExecStartTime)
                .average().orElse(0);

        double throughput = finished.size() / makespan;

        System.out.println("\n===== " + mode + " RESULTS =====");
        System.out.println("Makespan: " + makespan);
        System.out.println("Throughput: " + throughput);
        System.out.println("Avg Waiting Time: " + waiting);

        return new SimulationResult(makespan, throughput, waiting);
    }

    private static void generateGraphs(SimulationResult edge,
                                       SimulationResult cloud,
                                       SimulationResult cpoa) {

        List<String> strategies = List.of("Edge", "Cloud", "CPOA");

        // ---------------- MAKESPAN ----------------
        var makespanChart = new org.knowm.xchart.CategoryChartBuilder()
                .width(800)
                .height(600)
                .title("Makespan Comparison")
                .xAxisTitle("Strategy")
                .yAxisTitle("Makespan")
                .build();

        makespanChart.addSeries("Makespan",
                strategies,
                List.of(edge.makespan, cloud.makespan, cpoa.makespan));

        new org.knowm.xchart.SwingWrapper<>(makespanChart).displayChart();


        // ---------------- THROUGHPUT ----------------
        var throughputChart = new org.knowm.xchart.CategoryChartBuilder()
                .width(800)
                .height(600)
                .title("Throughput Comparison")
                .xAxisTitle("Strategy")
                .yAxisTitle("Throughput")
                .build();

        throughputChart.addSeries("Throughput",
                strategies,
                List.of(edge.throughput, cloud.throughput, cpoa.throughput));

        new org.knowm.xchart.SwingWrapper<>(throughputChart).displayChart();


        // ---------------- WAITING TIME ----------------
        var waitingChart = new org.knowm.xchart.CategoryChartBuilder()
                .width(800)
                .height(600)
                .title("Average Waiting Time Comparison")
                .xAxisTitle("Strategy")
                .yAxisTitle("Waiting Time")
                .build();

        waitingChart.addSeries("Waiting Time",
                strategies,
                List.of(edge.waitingTime, cloud.waitingTime, cpoa.waitingTime));

        new org.knowm.xchart.SwingWrapper<>(waitingChart).displayChart();
    }
}