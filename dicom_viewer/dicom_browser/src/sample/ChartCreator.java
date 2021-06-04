package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Klasa abstrakcyjna, służąca do rysowania sceny na której wyświetlony będzie profil linii bądź histogram.
 */
public class ChartCreator {
    private Scene scene;
    private Pane pane;
    protected Stage stage;
    private double xPos,yPos;
    private Label label;

    public ChartCreator() {
    }

    /**
     * Metoda rysująca okno
     */
    public void drawWindow(){
        stage = new Stage();
        pane = new Pane();
        scene = new Scene(pane,500,400);
        pane.setStyle("-fx-background-color:white");
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
        label = new Label("Preparing histogram...");
        pane.getChildren().add(label);
        label.setLayoutX(200);
        label.setLayoutY(190);

    }


    /**
     * Metoda dopasowująca wymiary wykresu do wymiarów okna
     * @param chart
     */
    public void bindChartToScene(XYChart chart){
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                chart.setPrefHeight(scene.getHeight());
            }
        });
        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                chart.setPrefWidth(scene.getWidth());
            }
        });
    }


    /**
     * Metoda dodająca wykres do okna
     * @param chart
     */
    public void addChart(XYChart chart){
        chart.setPrefWidth(pane.getPrefWidth());
        chart.setPrefHeight(pane.getPrefHeight());
        pane.getChildren().add(chart);
    }

    /**
     * Metoda umożliwiająca przybliżanie i przesuwanie wykresu
     * @param chart
     */
    public void setChartEvents(XYChart chart){
        final double SCALE_DELTA = 1.01;


        scene.setOnMouseDragged(mouseEvent -> {
            double scaleFactor;
            double deltaX = mouseEvent.getX() - xPos;
            double deltaY = mouseEvent.getY() - yPos;
            if(mouseEvent.isPrimaryButtonDown()){
                if (mouseEvent.getY() - yPos < 0) {
                    if(pane.getScaleX() < 7) {
                        scaleFactor = SCALE_DELTA;
                        pane.setScaleY(pane.getScaleY() * scaleFactor);
                        pane.setScaleX(pane.getScaleX() * scaleFactor);
                    }

                }
                if (mouseEvent.getY() - yPos > 0) {
                    scaleFactor = 1/SCALE_DELTA;
                    pane.setScaleY(pane.getScaleY()*scaleFactor);
                    pane.setScaleX(pane.getScaleX()*scaleFactor);
                    if (pane.getScaleX() <= 0.25) {
                        pane.setScaleX(0.25);
                        pane.setScaleY(0.25);
                    }

                }
            }
            if(mouseEvent.isSecondaryButtonDown()) {
                chart.setLayoutX(chart.getLayoutX() + deltaX / pane.getScaleX());
                chart.setLayoutY(chart.getLayoutY() + deltaY / pane.getScaleY());
            }
            xPos = mouseEvent.getX();
            yPos = mouseEvent.getY();
        });

        scene.setOnMousePressed(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                pane.setScaleX(1.0);
                pane.setScaleY(1.0);
                chart.setLayoutX(0);
                chart.setLayoutY(0);
            }
            xPos = mouseEvent.getX();
            yPos = mouseEvent.getY();
        });
    }
}
