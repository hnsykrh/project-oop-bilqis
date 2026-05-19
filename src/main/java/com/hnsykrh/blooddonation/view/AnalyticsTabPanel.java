package com.hnsykrh.blooddonation.view;

import com.hnsykrh.blooddonation.controller.DonationController;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.Map;

/**
 * JFreeChart bar chart: total donation volume (mL) by donor blood type.
 */
final class AnalyticsTabPanel extends JPanel implements SwingUi.ComponentSource {

    private final DonationController donationController;
    private final JPanel chartHost = new JPanel(new BorderLayout());

    AnalyticsTabPanel(DonationController donationController) {
        super(new BorderLayout());
        this.donationController = donationController;
        JButton refreshBtn = new JButton("Refresh chart");
        refreshBtn.addActionListener(e -> reload());
        add(SwingUi.toolbar(refreshBtn), BorderLayout.NORTH);
        add(chartHost, BorderLayout.CENTER);
        reload();
    }

    private void reload() {
        SwingUi.runAsync(this, null, () -> donationController.volumeByBloodType(), this::renderChart);
    }

    private void renderChart(Map<String, Integer> volumeByType) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String type : SwingUi.BLOOD_TYPES) {
            int ml = volumeByType.getOrDefault(type, 0);
            dataset.addValue(ml, "Volume (mL)", type);
        }
        for (Map.Entry<String, Integer> entry : volumeByType.entrySet()) {
            if (!java.util.Arrays.asList(SwingUi.BLOOD_TYPES).contains(entry.getKey())) {
                dataset.addValue(entry.getValue(), "Volume (mL)", entry.getKey());
            }
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Donation volume by blood type",
                "Blood type",
                "Total volume (mL) — non-voided donations",
                dataset);
        chartHost.removeAll();
        chartHost.add(new ChartPanel(chart), BorderLayout.CENTER);
        chartHost.revalidate();
        chartHost.repaint();
    }

    @Override
    public java.awt.Component component() {
        return this;
    }
}
