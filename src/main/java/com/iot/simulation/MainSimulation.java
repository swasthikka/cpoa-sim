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

        GraphGenerator.generateAllGraphs();
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
}