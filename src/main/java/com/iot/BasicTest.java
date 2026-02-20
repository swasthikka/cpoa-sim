package com.iot;

import org.cloudbus.cloudsim.core.CloudSim;

public class BasicTest {

    public static void main(String[] args) {

        CloudSim simulation = new CloudSim();

        System.out.println("CloudSim initialized successfully!");

        simulation.start();

        System.out.println("Simulation finished.");
    }
}