package sample;

import javafx.scene.chart.*;


import java.util.List;


/**
 * Klasa tworzaca wykres profilu linii - zależność wartości od położenia punktu na prostej
 */
public class LineProfileChart extends ChartCreator {

    private final CategoryAxis xAxis = new CategoryAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private final LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);


    public LineProfileChart(List<Double> pixelValueList) {
        xAxis.setLabel("Length");
        chart.setTitle("Line profile");
        chart.setCreateSymbols(false);
        XYChart.Series series = new XYChart.Series();
        series.setName("Pixel value");
        for (int i = 0; i < pixelValueList.size(); i++) {
            final XYChart.Data data = new XYChart.Data(String.valueOf(i), pixelValueList.get(i));
            series.getData().add(data);
        }
        chart.getData().add(series);
        chart.setAnimated(false);
        drawWindow();
        addChart(chart);
        bindChartToScene(chart);
        setChartEvents(chart);
    }


 /*   private void setChartEvents(){
        final double SCALE_DELTA = 1.1;

        chart.setOnMouseDragged(mouseEvent -> {
            chart.setLayoutX(chart.getLayoutX() + mouseEvent.getX() - xPos);
            chart.setLayoutY(chart.getLayoutY() + mouseEvent.getY() - yPos);
        });
        pane.setOnScroll(event -> {
            event.consume();
            if (event.getDeltaY() == 0) {
                return;
            }
            double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;
            pane.setScaleX(pane.getScaleX() * scaleFactor);
            pane.setScaleY(pane.getScaleY() * scaleFactor);
        });
        chart.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                pane.setScaleX(1.0);
                pane.setScaleY(1.0);
                chart.setLayoutX(0);
                chart.setLayoutY(0);
            }
            xPos = mouseEvent.getX();
            yPos = mouseEvent.getY();
        });*/


}
