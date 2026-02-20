package com.iot.tasks;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TaskGenerator {

    public List<Cloudlet> createTasks(int numberOfTasks) {

        List<Cloudlet> list = new ArrayList<>();
        Random random = new Random();

        for(int i = 0; i < numberOfTasks; i++) {

            long length = 10000 + random.nextInt(40000); // Task size

            Cloudlet cloudlet = new CloudletSimple(length, 1)
                    .setFileSize(300)
                    .setOutputSize(300);

            list.add(cloudlet);
        }

        return list;
    }
}