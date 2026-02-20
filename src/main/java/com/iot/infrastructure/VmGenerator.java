package com.iot.infrastructure;

import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;

public class VmGenerator {

    public List<Vm> createVMs(int numberOfVms, int mips) {

        List<Vm> list = new ArrayList<>();

        for(int i = 0; i < numberOfVms; i++) {

            Vm vm = new VmSimple(mips, 1)
                    .setRam(2000)
                    .setBw(1000)
                    .setSize(10000);

            list.add(vm);
        }

        return list;
    }
}