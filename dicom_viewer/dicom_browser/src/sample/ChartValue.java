package sample;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.List;

public class ChartValue extends StackPane {

    private double layoutX, layoutY;
    public ChartValue( double pixelValue) {
        setPrefSize(5, 5);
        final Label label = createDataThresholdLabel(pixelValue);

        setOnMouseEntered(mouseEvent -> {
            getChildren().setAll(label);
            setCursor(Cursor.NONE);
            toFront();
            mouseEvent.consume();
        });
        setOnMouseExited(mouseEvent -> {
            getChildren().clear();
            mouseEvent.consume();
        });
    }

    private Label createDataThresholdLabel(double pixelValue) {
        final Label label = new Label("" + pixelValue);
        label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
        label.setStyle("-fx-font-size: 2; -fx-font-weight: bold;");
        label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        return label;
    }
}