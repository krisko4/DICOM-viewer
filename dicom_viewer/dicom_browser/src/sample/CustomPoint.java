package sample;


import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;



public class CustomPoint extends Circle {


    public CustomPoint(double centerX, double centerY, double radius, Color color, Group geomGroup){
        super(centerX,centerY,radius,color);

        setEvents();
        geomGroup.getChildren().add(this);
    }

    public void bindValueBox(ValueBox valueBox, double valX, double valY){

        centerXProperty().addListener((observableValue, oldVal, t1) -> {
            if (valueBox.getLayoutX() == (double) oldVal + valX && valueBox.getLayoutY() == getCenterY() + valY) {
                valueBox.setLayoutX(getCenterX() + valX);
            }
        });

       centerYProperty().addListener((observableValue, oldVal, t1) -> {

           if (valueBox.getLayoutY() == (double) oldVal + valY && valueBox.getLayoutX() == getCenterX() + valX) {
               valueBox.setLayoutY(getCenterY() - valY);
           }

       });
    }

    public void setEvents(){

        double radius = getRadius();

        setOnMouseMoved(Event::consume);

        setOnMousePressed(Event::consume);

        setOnMouseEntered(mouseEvent -> {
            setRadius(radius + 4);
            setFill(Color.TRANSPARENT);
            setStroke(Color.ORANGE);
        });
        setOnMouseExited(mouseEvent -> {
            setRadius(radius);
            setFill(Color.SKYBLUE);
            setStroke(Color.TRANSPARENT);
            //setStroke(Color.SKYBLUE);
        });


    }


}
