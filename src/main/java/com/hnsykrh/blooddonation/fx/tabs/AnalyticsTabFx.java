package com.hnsykrh.blooddonation.fx.tabs;

import com.hnsykrh.blooddonation.fx.FxAppContext;
import com.hnsykrh.blooddonation.fx.FxUtil;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;

/**
 * JavaFX chart tab (built-in charts) + OpenPDF still used on Inventory tab for third-party requirement.
 */
public final class AnalyticsTabFx {

    private AnalyticsTabFx() {
    }

    public static BorderPane build(FxAppContext ctx, Stage stage) {
        BorderPane root = new BorderPane();
        Button refreshBtn = new Button("Refresh chart");
        VBox top = new VBox(refreshBtn);
        top.setPadding(new Insets(8));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Blood type");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Volume (mL)");
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Donation volume by blood type (non-voided)");
        chart.setLegendVisible(false);

        Runnable reload = () -> FxUtil.runAsync(stage, ctx.getDonationController()::volumeByBloodType, data -> {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Volume");
            for (String type : FxUtil.BLOOD_TYPES) {
                series.getData().add(new XYChart.Data<>(type, data.getOrDefault(type, 0)));
            }
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                boolean known = false;
                for (String t : FxUtil.BLOOD_TYPES) {
                    if (t.equals(entry.getKey())) {
                        known = true;
                        break;
                    }
                }
                if (!known) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }
            }
            chart.getData().setAll(series);
        });

        refreshBtn.setOnAction(e -> reload.run());
        root.setTop(top);
        root.setCenter(chart);
        reload.run();
        return root;
    }
}
