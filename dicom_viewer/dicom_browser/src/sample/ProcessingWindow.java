package sample;

import javafx.animation.FadeTransition;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class ProcessingWindow {
    private Scene scene;
    private StackPane stackPane;
    private Circle circle;
    private double xPos,yPos;
    private Pane pane;
    private Label label;

    public ProcessingWindow(Pane pane) {
        this.pane = pane;
        stackPane = new StackPane();
        label = new Label("Processing...");
        label.setFont(Font.font("Verdana",20));
        label.setId("processingLabel");
        circle = new Circle();
        circle.setId("processingCircle");
        pane.getChildren().add(stackPane);
        stackPane.getChildren().addAll(circle,label);
        circle.setRadius(150);
        stackPane.setLayoutX(pane.getWidth()/2 - circle.getRadius());
        stackPane.setLayoutY(pane.getHeight()/2 - circle.getRadius());
        circle.setFill(Color.rgb(32, 32, 32, 0.7));
        stackPane.setVisible(false);


    }

    public void hideWindow(){
        FadeTransition ft = new FadeTransition(Duration.millis(800), stackPane);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.play();
        ft.onFinishedProperty().set(actionEvent -> pane.getChildren().remove(stackPane));

    }



    public void showWindow(){
        stackPane.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(800), stackPane);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setAutoReverse(true);
        ft.setCycleCount(1000);
        ft.play();

        //ft.onFinishedProperty().set(actionEvent -> stackPane.setVisible(true));

    }

    public void repaint(){
        label.setText("Processing completed!");
        //label.setTextFill(Color.GREEN);
        label.setId("processingLabelFinished");
       // circle.setStroke(Color.GREEN);
        circle.setId("processingCircleFinished");

    }

}
