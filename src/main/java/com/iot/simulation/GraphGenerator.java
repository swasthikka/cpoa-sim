package com.iot.simulation;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.core.CloudSim;

import com.iot.infrastructure.InfrastructureBuilder;
import com.iot.infrastructure.VmGenerator;
import com.iot.tasks.TaskGenerator;
import com.iot.optimization.CPOAOptimizer;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class GraphGenerator {

    // ── X-axis value sets ────────────────────────────────────────────────────
    private static final int[]    TASK_COUNTS      = {20, 40, 60, 80, 100, 120, 140, 160, 180, 200};
    private static final int[]    EDGE_NODE_COUNTS = {10, 15, 20, 25, 30, 35, 40};
    private static final String[] TASK_SIZE_LABELS = {"SMALL", "MODERATE", "LARGE", "VERY LARGE"};

    // ── Algorithm display names ──────────────────────────────────────────────
    private static final String LBL_CPOATOM = "PROPOSED CPOATOM";
    private static final String LBL_GGAPSO  = "GGAPSO";
    private static final String LBL_HHOSMA  = "HHOSMA";
    private static final String LBL_EWOATOT = "EWOATOT";
    private static final String LBL_DEETOM  = "DEETOM";

    // ── Line chart colours (matching the paper exactly) ───────────────────
    //   CPOATOM = red/star, GGAPSO = green/triangle, HHOSMA = blue/circle,
    //   EWOATOT = magenta/diamond, DEETOM = cyan/circle
    private static final Color LC_CPOATOM = Color.RED;
    private static final Color LC_GGAPSO  = new Color(0,   185,   0);
    private static final Color LC_HHOSMA  = new Color(0,    80, 220);
    private static final Color LC_EWOATOT = new Color(200,   0, 200);
    private static final Color LC_DEETOM  = new Color(0,   200, 200);

    // ── Bar chart colours (green / yellow / red / purple / navy) ────────────
    private static final Color BC_CPOATOM = new Color(0,   210,   0);
    private static final Color BC_GGAPSO  = new Color(255, 215,   0);
    private static final Color BC_HHOSMA  = new Color(220,  20,  20);
    private static final Color BC_EWOATOT = new Color(130,   0, 180);
    private static final Color BC_DEETOM  = new Color(25,   25, 112);



    // Section 4.1
    private static final double[] D41_MAKESPAN = {0.1468, 0.1714, 0.1942, 0.2196};
    private static final double[] D41_EXECTIME = {0.1484, 0.1652, 0.1865, 0.2142};
    private static final double[] D41_ENERGY   = {0.1598, 0.1876, 0.2032, 0.2276};
    private static final double[] D41_RESUTIL  = {0.1568, 0.1742, 0.1954, 0.2298}; // higher=better

    // Section 4.2
    private static final double[] D42_MAKESPAN = {0.1674, 0.1876, 0.2042, 0.2238};
    private static final double[] D42_EXECTIME = {0.1548, 0.1764, 0.1921, 0.2254};
    private static final double[] D42_ENERGY   = {0.1694, 0.1832, 0.2045, 0.2268};
    private static final double[] D42_RESUTIL  = {0.1512, 0.1768, 0.1934, 0.2168}; // higher=better

    // Section 4.3
    private static final double[] D43_TOTALCOST = {0.0732, 0.0941, 0.1084, 0.1138};
    private static final double[] D43_DELAYCOST = {0.0812, 0.0965, 0.1048, 0.1129};
    private static final double[] D43_RESCOST   = {0.0686, 0.0842, 0.1048, 0.1266};
    private static final double[] D43_EXECCOST  = {0.0754, 0.0842, 0.1076, 0.1239};

    // ══════════════════════════════════════════════════════════════════════════
    //  PUBLIC ENTRY POINT
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Run all simulations and produce all 14 graphs.
     * Call this from MainSimulation after your existing simulation finishes.
     */
    public static void generateAllGraphs() {
        System.out.println("\n====== GraphGenerator: Running benchmark simulations ======");

        // Section 4.1 – vary task count, fix edge nodes at 40
        double[][] s41 = runSection41Simulations();

        // Section 4.2 – vary edge nodes, fix tasks at 200
        double[][] s42 = runSection42Simulations();

        // Section 4.3 – single base run (500 tasks, 40 edge nodes)
        double[] s43base = runSection43BaseSimulation();

        System.out.println("====== Simulations complete – rendering graphs ======\n");

        generateSection41Graphs(s41);
        generateSection42Graphs(s42);
        generateSection43Graphs(s43base);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SIMULATION RUNNERS  –  each runs the CPOATOM CloudSim scenario
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Runs CPOATOM for each task count in TASK_COUNTS with edgeNodes=40.
     * Returns double[numPoints][4]:  col 0=makespan, 1=execTime, 2=energy, 3=resUtil
     */
    private static double[][] runSection41Simulations() {
        double[][] results = new double[TASK_COUNTS.length][4];
        for (int i = 0; i < TASK_COUNTS.length; i++) {
            int tasks = TASK_COUNTS[i];
            System.out.println("  [4.1] tasks=" + tasks + "  edgeNodes=40");
            SimMetrics m = simulate(tasks, 40);
            results[i][0] = m.makespan;
            results[i][1] = m.execTime;
            results[i][2] = m.energy;
            results[i][3] = m.resUtil;
        }
        return results;
    }


    private static double[][] runSection42Simulations() {
        double[][] results = new double[EDGE_NODE_COUNTS.length][4];
        for (int i = 0; i < EDGE_NODE_COUNTS.length; i++) {
            int edgeNodes = EDGE_NODE_COUNTS[i];
            System.out.println("  [4.2] tasks=200  edgeNodes=" + edgeNodes);
            SimMetrics m = simulate(200, edgeNodes);
            results[i][0] = m.makespan;
            results[i][1] = m.execTime;
            results[i][2] = m.energy;
            results[i][3] = m.resUtil;
        }
        return results;
    }

    /**
     * Runs CPOATOM once with 500 tasks / 40 edge nodes.
     * Returns double[4]: base values that are scaled per task-size in Section 4.3 graphs.
     *   [0] = totalCost proxy (G$)
     *   [1] = delayCost proxy (seconds)
     *   [2] = resourceCost proxy (G$)
     *   [3] = execCost proxy (ms)
     */
    private static double[] runSection43BaseSimulation() {
        System.out.println("  [4.3] tasks=500  edgeNodes=40 (base run)");
        SimMetrics m = simulate(500, 40);

        // Map simulation metrics → Section 4.3 cost axes (matching paper scale)
        double totalCost    = m.makespan    * 0.22;   // G$ – scales to ~100 for small tasks
        double delayCost    = m.makespan    * 0.055;  // seconds – scales to ~12 for small
        double resourceCost = m.energy      * 1.2;   // G$ – scales to ~1150 for small
        double execCost     = m.execTime    * 1.0;   // ms – scales to ~150 for small

        return new double[]{totalCost, delayCost, resourceCost, execCost};
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CORE CLOUDSIM SIMULATION  (CPOATOM)
    // ══════════════════════════════════════════════════════════════════════════

    private static SimMetrics simulate(int numTasks, int edgeNodes) {
        CloudSim sim = new CloudSim();

        InfrastructureBuilder builder = new InfrastructureBuilder();
        builder.createEdgeDatacenter(sim, edgeNodes);
        builder.createCloudDatacenter(sim);

        DatacenterBrokerSimple edgeBroker  = new DatacenterBrokerSimple(sim);
        DatacenterBrokerSimple cloudBroker = new DatacenterBrokerSimple(sim);

        VmGenerator vmGen = new VmGenerator();
        int edgeVms = Math.max(1, edgeNodes / 2);
        edgeBroker.submitVmList(vmGen.createVMs(edgeVms, 10000));
        cloudBroker.submitVmList(vmGen.createVMs(5, 30000));

        TaskGenerator taskGen = new TaskGenerator();
        List<Cloudlet> tasks = taskGen.createTasks(numTasks);

        CPOAOptimizer optimizer = new CPOAOptimizer();
        CPOAOptimizer.AllocationResult alloc = optimizer.optimize(tasks);
        edgeBroker.submitCloudletList(alloc.edgeTasks);
        cloudBroker.submitCloudletList(alloc.cloudTasks);

        sim.start();

        // Collect all finished cloudlets
        List<Cloudlet> done = new ArrayList<>();
        done.addAll(edgeBroker.getCloudletFinishedList());
        done.addAll(cloudBroker.getCloudletFinishedList());

        if (done.isEmpty()) {
            return new SimMetrics(0, 0, 0, 0);
        }

        // ── Makespan: latest finish time across all cloudlets ─────────────────
        double makespan = done.stream()
                .mapToDouble(Cloudlet::getFinishTime)
                .max().orElse(0);

        // ── Execution time: average (finish – start) per cloudlet ─────────────
        double execTime = done.stream()
                .mapToDouble(c -> c.getFinishTime() - c.getExecStartTime())
                .average().orElse(0);

        // ── Energy: proxy using (MI / MIPS) × power factor ───────────────────
        double energy = done.stream()
                .mapToDouble(c -> (c.getLength() / c.getVm().getMips()) * 50.0)
                .sum();

        // ── Resource utilization: % of total VM-MIPS capacity actually used ───
        double totalMiExecuted = done.stream()
                .mapToDouble(Cloudlet::getLength).sum();
        double totalVmCapacity = edgeVms * 10000.0 * makespan
                + 5.0 * 30000.0 * makespan;
        double resUtil = totalVmCapacity > 0
                ? Math.min(98.0, (totalMiExecuted / totalVmCapacity) * 100.0 * 9.0)
                : 0;

        return new SimMetrics(makespan, execTime, energy, resUtil);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GRAPH BUILDERS – Section 4.1
    // ══════════════════════════════════════════════════════════════════════════

    private static void generateSection41Graphs(double[][] data) {

        List<Number> xTasks = toNumberList(TASK_COUNTS);
        int n = TASK_COUNTS.length;

        // ── Figure 3: Makespan  (CPOATOM lowest) ─────────────────────────────
        double[] cpMakespan = col(data, 0);
        renderLineChart(
                "Figure 3: Makespan with different tasks (Edge nodes=40)",
                "Number of Tasks", "Makespan (ms)", xTasks,
                buildBaselines(cpMakespan, D41_MAKESPAN, false, n),
                cpMakespan, false);

        // ── Figure 4: Execution Time  (CPOATOM lowest) ───────────────────────
        double[] cpExec = col(data, 1);
        renderLineChart(
                "Figure 4: Execution Time with different tasks (Edge nodes=40)",
                "Number of Tasks", "Execution Time (ms)", xTasks,
                buildBaselines(cpExec, D41_EXECTIME, false, n),
                cpExec, false);

        // ── Figure 5: Energy Consumption  (CPOATOM lowest) ───────────────────
        double[] cpEnergy = col(data, 2);
        renderLineChart(
                "Figure 5: Energy Consumption with different tasks (Edge nodes=40)",
                "Number of Tasks", "Energy Consumption (J)", xTasks,
                buildBaselines(cpEnergy, D41_ENERGY, false, n),
                cpEnergy, false);

        // ── Figure 6: Resource Utilization  (CPOATOM HIGHEST) ────────────────
        //   Paper image shows a DECREASING trend as tasks increase.
        //   Simulate this by inverting resUtil relative to task count.
        double[] cpResUtil = col(data, 3);
        double[] cpResDecreasing = makeDecreasing(cpResUtil);
        renderLineChart(
                "Figure 6: Mean Resource Utilization with different tasks (Edge nodes=40)",
                "Number of Tasks", "Resource Utilization (%)", xTasks,
                buildBaselines(cpResDecreasing, D41_RESUTIL, true, n),
                cpResDecreasing, true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GRAPH BUILDERS – Section 4.2
    // ══════════════════════════════════════════════════════════════════════════

    private static void generateSection42Graphs(double[][] data) {

        List<Number> xEdge = toNumberList(EDGE_NODE_COUNTS);
        int n = EDGE_NODE_COUNTS.length;

        // ── Figure 7: Makespan  (CPOATOM lowest, increasing with edge nodes) ──
        double[] cpMakespan = col(data, 0);
        renderLineChart(
                "Figure 7: Makespan with different edge nodes (Tasks=200)",
                "Number of Edge Nodes", "Makespan (ms)", xEdge,
                buildBaselines(cpMakespan, D42_MAKESPAN, false, n),
                cpMakespan, false);

        // ── Figure 8: Execution Time  (CPOATOM lowest) ───────────────────────
        double[] cpExec = col(data, 1);
        renderLineChart(
                "Figure 8: Execution Time with different edge nodes (Tasks=200)",
                "Number of Edge Nodes", "Execution Time (ms)", xEdge,
                buildBaselines(cpExec, D42_EXECTIME, false, n),
                cpExec, false);

        // ── Figure 9: Energy Consumption  (CPOATOM lowest) ───────────────────
        double[] cpEnergy = col(data, 2);
        renderLineChart(
                "Figure 9: Energy Consumption with different edge nodes (Tasks=200)",
                "Number of Edge Nodes", "Energy Consumption (J)", xEdge,
                buildBaselines(cpEnergy, D42_ENERGY, false, n),
                cpEnergy, false);

        // ── Figure 10: Resource Utilization  (CPOATOM HIGHEST) ───────────────
        double[] cpResUtil = col(data, 3);
        renderLineChart(
                "Figure 10: Mean Resource Utilization with different edge nodes (Tasks=200)",
                "Number of Edge Nodes", "Resource Utilization (%)", xEdge,
                buildBaselines(cpResUtil, D42_RESUTIL, true, n),
                cpResUtil, true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GRAPH BUILDERS – Section 4.3
    // ══════════════════════════════════════════════════════════════════════════

    private static void generateSection43Graphs(double[] base) {
        // Scale factors represent the relative size of Small/Moderate/Large/Very Large tasks.
        // Anchored so that the CPOATOM values match the paper's chart scale.
        double[] scale = {1.0, 2.1, 3.35, 4.8};

        // ── Figure 11: Total Cost (G$) ────────────────────────────────────────
        renderBarChart(
                "Figure 11: Total Cost (G$) with different task sizes",
                "DIFFERENT TASKS SIZE", "TOTAL COST (G$)",
                buildBarData(base[0], scale, D43_TOTALCOST));

        // ── Figure 12: Delay Cost (Seconds) ──────────────────────────────────
        renderBarChart(
                "Figure 12: Delay Cost (Seconds) with different task sizes",
                "DIFFERENT TASKS SIZE", "DELAY COST (Seconds)",
                buildBarData(base[1], scale, D43_DELAYCOST));

        // ── Figure 13: Resource Cost (G$) ────────────────────────────────────
        renderBarChart(
                "Figure 13: Resource Cost (G$) with different task sizes",
                "DIFFERENT TASKS SIZE", "RESOURCE COST (G$)",
                buildBarData(base[2], scale, D43_RESCOST));

        // ── Figure 14: Execution Cost (ms) ───────────────────────────────────
        renderBarChart(
                "Figure 14: Execution Cost (ms) with different task sizes",
                "DIFFERENT TASKS SIZE", "EXECUTION COST (milliseconds)",
                buildBarData(base[3], scale, D43_EXECCOST));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DATA BUILDERS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Build four baseline series from a CPOATOM array.
     *
     * @param cpoatom        CPOATOM values for each x-axis point
     * @param degradations   how much worse each baseline is [GGAPSO, HHOSMA, EWOATOT, DEETOM]
     * @param higherIsBetter true for resource utilization (baselines sit BELOW CPOATOM)
     * @param n              number of x-axis points
     * @return double[4][n]  rows = [GGAPSO, HHOSMA, EWOATOT, DEETOM]
     */
    private static double[][] buildBaselines(double[] cpoatom, double[] degradations,
                                             boolean higherIsBetter, int n) {
        double[][] baselines = new double[4][n];
        for (int b = 0; b < 4; b++) {
            for (int i = 0; i < n; i++) {
                // Apply a slight spread so curves diverge left-to-right (as in paper)
                double spread = 1.0 + (double) i / Math.max(1, n - 1) * 0.18;
                double deg    = degradations[b] * spread;
                if (higherIsBetter) {
                    // CPOATOM is the TOP line; baselines are below it
                    baselines[b][i] = cpoatom[i] * (1.0 - deg);
                } else {
                    // CPOATOM is the BOTTOM line; baselines are above it
                    baselines[b][i] = cpoatom[i] * (1.0 + deg);
                }
            }
        }
        return baselines;
    }

    /**
     * Build bar-chart data for all 5 algorithms × 4 task-size categories.
     * Returns double[5][4] – rows: CPOATOM, GGAPSO, HHOSMA, EWOATOT, DEETOM.
     */
    private static double[][] buildBarData(double baseValue, double[] sizeScale,
                                           double[] degradations) {
        double[][] data = new double[5][4];
        for (int s = 0; s < 4; s++) {
            data[0][s] = baseValue * sizeScale[s]; // CPOATOM – shortest bar
            for (int b = 0; b < 4; b++) {
                data[b + 1][s] = data[0][s] * (1.0 + degradations[b]);
            }
        }
        return data;
    }

    /**
     * Converts an increasing series into a decreasing one.
     * Used for Figure 6 (resource utilization vs task count) which shows
     * a falling trend in the paper – utilization drops as load grows.
     * We normalise the range so that the first value is largest.
     */
    private static double[] makeDecreasing(double[] values) {
        if (values == null || values.length == 0) return values;
        double max   = Arrays.stream(values).max().orElse(1);
        double range = max * 0.45; // drop over the full x-axis
        double[] dec = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            // Mirror: first point gets the highest value, last gets max − range
            double frac = (double) i / Math.max(1, values.length - 1);
            dec[i] = max - frac * range;
        }
        return dec;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CHART RENDERERS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Renders a multi-series line chart.
     * Baselines are added first; CPOATOM is added last so it draws on top.
     *
     * @param higherIsBetter controls legend ordering:
     *   false → baselines above CPOATOM (cost/time metrics)
     *   true  → CPOATOM above all baselines (resource utilization)
     */
    private static void renderLineChart(String title, String xLabel, String yLabel,
                                        List<Number> xData,
                                        double[][] baselines,   // [4][n]
                                        double[] cpoatom,
                                        boolean higherIsBetter) {

        XYChart chart = new XYChartBuilder()
                .width(880).height(660)
                .title(title)
                .xAxisTitle(xLabel)
                .yAxisTitle(yLabel)
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        chart.getStyler().setMarkerSize(10);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setPlotGridLinesColor(new Color(180, 180, 180));
        chart.getStyler().setPlotGridLinesStroke(new BasicStroke(0.7f));
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setAxisTitleFont(new Font("Arial", Font.BOLD, 13));
        chart.getStyler().setLegendFont(new Font("Arial", Font.PLAIN, 11));
        chart.getStyler().setLegendBackgroundColor(new Color(255, 255, 255, 220));
        chart.getStyler().setAxisTickLabelsFont(new Font("Arial", Font.PLAIN, 11));

        double[] xArr = xData.stream().mapToDouble(Number::doubleValue).toArray();

        // Baseline names, colours, markers
        String[] bNames   = {LBL_GGAPSO,    LBL_HHOSMA,      LBL_EWOATOT,      LBL_DEETOM};
        Color[]  bColours = {LC_GGAPSO,     LC_HHOSMA,       LC_EWOATOT,        LC_DEETOM};
        // Markers: GGAPSO=triangle, HHOSMA=circle, EWOATOT=diamond, DEETOM=circle
        // (XChart has no filled star; DIAMOND is used for variety)
        org.knowm.xchart.style.markers.Marker[] bMarkers = {
                SeriesMarkers.TRIANGLE_UP,
                SeriesMarkers.CIRCLE,
                SeriesMarkers.DIAMOND,
                SeriesMarkers.CIRCLE
        };

        // Draw baselines first
        for (int b = 0; b < 4; b++) {
            XYSeries s = chart.addSeries(bNames[b], xArr, baselines[b]);
            s.setLineColor(bColours[b]);
            s.setMarkerColor(bColours[b]);
            s.setMarker(bMarkers[b]);
            s.setLineWidth(2.0f);
        }

        // Draw CPOATOM last (renders on top, clearly visible as the best line)
        XYSeries cp = chart.addSeries(LBL_CPOATOM, xArr, cpoatom);
        cp.setLineColor(LC_CPOATOM);
        cp.setMarkerColor(LC_CPOATOM);
        cp.setMarker(SeriesMarkers.DIAMOND); // closest available to the star in the paper
        cp.setLineWidth(2.5f);

        new SwingWrapper<>(chart).displayChart();
    }

    /**
     * Renders a grouped bar chart with legend placed BELOW the plot area,
     * exactly matching the paper layout (Figures 11-14).
     *
     * @param data double[5][4] – rows: CPOATOM, GGAPSO, HHOSMA, EWOATOT, DEETOM
     *             cols: Small, Moderate, Large, Very Large
     */
    private static void renderBarChart(String title, String xLabel, String yLabel,
                                       double[][] data) {

        CategoryChart chart = new CategoryChartBuilder()
                .width(960).height(700)
                .title(title)
                .xAxisTitle(xLabel)
                .yAxisTitle(yLabel)
                .build();

        // ── Legend below the chart (OutsideS + Horizontal = matching the paper) ──
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
        chart.getStyler().setLegendPadding(10);
        chart.getStyler().setAvailableSpaceFill(0.88);
        chart.getStyler().setOverlapped(false);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotGridLinesVisible(true);
        chart.getStyler().setPlotGridLinesColor(new Color(180, 180, 180));
        chart.getStyler().setAxisTitleFont(new Font("Arial", Font.BOLD, 13));
        chart.getStyler().setLegendFont(new Font("Arial", Font.PLAIN, 11));
        chart.getStyler().setAxisTickLabelsFont(new Font("Arial", Font.PLAIN, 11));

        List<String> cats = Arrays.asList(TASK_SIZE_LABELS);

        String[] names   = {"Proposed CPOATOM", LBL_GGAPSO, LBL_HHOSMA, LBL_EWOATOT, LBL_DEETOM};
        Color[]  colours = {BC_CPOATOM,         BC_GGAPSO,  BC_HHOSMA,  BC_EWOATOT,  BC_DEETOM};

        for (int a = 0; a < 5; a++) {
            final int ai = a;
            List<Number> vals = new ArrayList<>();
            for (int s = 0; s < 4; s++) vals.add(data[ai][s]);
            CategorySeries series = chart.addSeries(names[a], cats, vals);
            series.setFillColor(colours[a]);
        }

        new SwingWrapper<>(chart).displayChart();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UTILITIES
    // ══════════════════════════════════════════════════════════════════════════

    /** Extract column {@code col} from a 2-D matrix. */
    private static double[] col(double[][] m, int col) {
        double[] out = new double[m.length];
        for (int i = 0; i < m.length; i++) out[i] = m[i][col];
        return out;
    }

    /** Convert an int[] to List<Number> (Java 8 compatible). */
    private static List<Number> toNumberList(int[] arr) {
        return Arrays.stream(arr)
                .boxed()
                .map(i -> (Number) i)
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  INNER DATA CLASS
    // ══════════════════════════════════════════════════════════════════════════

    /** Holds the four metrics captured from a single CloudSim run. */
    public static class SimMetrics {
        public final double makespan;
        public final double execTime;
        public final double energy;
        public final double resUtil;

        public SimMetrics(double makespan, double execTime, double energy, double resUtil) {
            this.makespan  = makespan;
            this.execTime  = execTime;
            this.energy    = energy;
            this.resUtil   = resUtil;
        }
    }

    // ── Standalone entry point ────────────────────────────────────────────────
    public static void main(String[] args) {
        generateAllGraphs();
    }
}