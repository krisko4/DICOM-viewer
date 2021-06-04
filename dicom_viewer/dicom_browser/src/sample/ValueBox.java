package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Klasa tworząca okno przechowujące wartości odpowiadające danym pomiarom
 */
public class ValueBox extends StackPane {
    private Rectangle rect;
    private Text valueText;
    private double cursorXPosition, cursorYPosition;



    public ValueBox(Group geomGroup, double width, double height){
        drawValueBox(geomGroup, width, height);
        setEvents();
    }

    public void setHeight(double val){
        rect.setHeight(val);
    }

    public void setWidth(double val){
        rect.setWidth(val);
    }



    public void drawValueBox(Group geomGroup, double width, double height){
        geomGroup.getChildren().add(this);
        valueText = new Text();
        valueText.setFill(Color.SKYBLUE);
        rect = new Rectangle();
        rect.setWidth(width);
        rect.setHeight(height);
        rect.setFill(Color.BLACK);
        this.getChildren().addAll(rect, valueText);
    }

    public void setTextFill(Color color){
        valueText.setFill(color);
    }


    public double calculateValue(double value, int treshold){
        if(value>=treshold) {
           value = value / treshold;
       }
        return Math.round(value * 100.0) / 100.0;
    }


    public void setVal(String val){
        valueText.setText(val);
    }

    private void setEvents(){

        this.setOnMouseEntered(mouseEvent -> {
            valueText.setFill(Color.ORANGE);

        });
        this.setOnMouseExited(mouseEvent -> {
            valueText.setFill(Color.SKYBLUE);

        });

        this.setOnMouseMoved(Event::consume);

        this.setOnMousePressed(mouseEvent -> {
            cursorXPosition = mouseEvent.getX();
            cursorYPosition = mouseEvent.getY();
            mouseEvent.consume();
        });

        this.setOnMouseReleased(Event::consume);


        this.setOnMouseDragged(mouseEvent -> {

            Point2D point2D = this.localToParent(mouseEvent.getX(), mouseEvent.getY());
            setLayoutX(point2D.getX() - cursorXPosition);
            setLayoutY(point2D.getY() - cursorYPosition);
            mouseEvent.consume();
    });







    }


}
