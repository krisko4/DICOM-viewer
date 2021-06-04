package sample;

import javafx.scene.chart.*;

import java.util.Arrays;
import java.util.List;

/**
 * Klasa tworząca histogram dla danego obrazu. Istnieje możliwość podziału na dowolną liczbę koszyków.
 */
public class Histogram extends ChartCreator {

    private final CategoryAxis xAxis = new CategoryAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private final BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);

    public Histogram(List<Object> propertiesList, int baskets) {
        xAxis.setLabel("Range");
        chart.setTitle("Histogram");
        chart.setAnimated(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        XYChart.Series series = new XYChart.Series();
        series.setName("Pixel count");
        int maxVal = (int) propertiesList.get(2);
        int[] pixels = (int[]) propertiesList.get(0);
        int[] rawPixels = new int[pixels.length];
        System.arraycopy(pixels, 0, rawPixels, 0, pixels.length);
        Arrays.sort(rawPixels);
        int minVal = rawPixels[0];
        int range = maxVal - minVal + 1;
        int[] counts;
        if (baskets == 0) {
            counts = new int[range + 1];
            for (int val : rawPixels) {
                counts[val - minVal]++;
            }
            for (int i = 0; i < counts.length; i++) {
                final XYChart.Data data = new XYChart.Data(String.valueOf(i + minVal), counts[i]);
                series.getData().add(data);
            }
        } else {
            counts = new int[baskets];
            for (int val : rawPixels) {
                // count(counts, val, range, i, baskets,minVal);
                double basketTreshold = (double) range / baskets;
                int basket;
                basket = (int) ((val + Math.abs(minVal)) / basketTreshold);
                counts[basket]++;

            }
            for (int j = 0; j < counts.length; j++) {
                final XYChart.Data data;
                int basketMinVal = minVal + 1 + j * range / baskets;
                if (j == 0) {
                    data = new XYChart.Data("[" + minVal + "," + (minVal + (range / baskets)) + "]", counts[j]);
                } else if (j == counts.length - 1) {
                    data = new XYChart.Data("[" + basketMinVal + "," + maxVal + "]", counts[j]);
                } else {
                    data = new XYChart.Data("[" + basketMinVal + "," + (minVal + (j + 1) * (range / baskets)) + "]", counts[j]);
                }
                series.getData().add(data);
            }


        }
        chart.getData().add(series);
        chart.setPrefWidth(500);
        chart.setPrefHeight(500);

    }

    /**
     *
     */
    public void add() {
        addChart(chart);
        bindChartToScene(chart);
        setChartEvents(chart);

    }

    public void showStage() {
        stage.show();
    }


}
