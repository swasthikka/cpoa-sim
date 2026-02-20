package com.iot.infrastructure;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;

import java.util.ArrayList;
import java.util.List;

public class InfrastructureBuilder {

    public Datacenter createEdgeDatacenter(CloudSim simulation, int numberOfHosts) {

        List<Host> hostList = new ArrayList<>();

        for(int i = 0; i < numberOfHosts; i++) {

            List<Pe> peList = List.of(new PeSimple(10000)); // 10,000 MIPS

            Host host = new HostSimple(
                    16000,    // RAM
                    1000000,  // Bandwidth
                    1000000,  // Storage
                    peList
            );

            hostList.add(host);
        }

        return new DatacenterSimple(simulation, hostList);
    }

    public Datacenter createCloudDatacenter(CloudSim simulation) {

        List<Host> hostList = new ArrayList<>();

        for(int i = 0; i < 5; i++) {

            List<Pe> peList = List.of(new PeSimple(30000)); // 30,000 MIPS

            Host host = new HostSimple(
                    32000,
                    2000000,
                    2000000,
                    peList
            );

            hostList.add(host);
        }

        return new DatacenterSimple(simulation, hostList);
    }
}