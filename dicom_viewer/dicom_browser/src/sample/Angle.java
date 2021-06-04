package sample;


import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;

/**
 * Klasa umożliwiająca tworzenie kąta, rysowanie go na obrazie oraz modyfikację
 */
public class Angle extends Polyline {

    private boolean marked;
    private CustomPoint initialPoint, midPoint, endPoint;
    private Point2D initialPoint2D, midPoint2D, endPoint2D;
    private final ImageView imageView;
    private Line line;
    private boolean drawn;
    private final ValueBox valueBox;
    private final Group geomGroup;
    private double cursorXPosition, cursorYPosition;

    /**
     * Konstruktor inicjalizujący kąt i przypisujący początkowe wartości
     *
     * @param startX
     * @param startY

     * @param imageView
     * @param geomGroup
     */
    public Angle(double startX, double startY, ImageView imageView, Group geomGroup) {
        geomGroup.getChildren().add(this);
        this.imageView = imageView;
        this.geomGroup = geomGroup;
        setStroke(Color.SKYBLUE);
        setStrokeWidth(1);
        drawn = false;
        valueBox = new ValueBox(geomGroup, 40, 20);
        drawAngle(startX, startY);
        hideValueBox();
        setEvents();
    }


    public void hideValueBox() {
        valueBox.setVisible(false);
    }

    public void showValueBox() {
        valueBox.setVisible(true);
    }

    /**
     * Metoda przemieszczająca wybrany punkt
     *
     * @param deltaX
     * @param deltaY
     * @param points
     */
    public void movePoint(double deltaX, double deltaY, CustomPoint... points) {
        for (CustomPoint point : points) {
            point.setCenterX(point.getCenterX() + deltaX);
            point.setCenterY(point.getCenterY() + deltaY);
            if (point == initialPoint) {
                initialPoint2D = new Point2D(point.getCenterX(), point.getCenterY());
            }
            if (point == midPoint) {
                midPoint2D = new Point2D(point.getCenterX(), point.getCenterY());
            }
            if (point == endPoint) {
                endPoint2D = new Point2D(point.getCenterX(), point.getCenterY());
            }
        }

    }

    /**
     * Metoda przypisująca eventy punktom
     *
     * @param points
     */
    public void setPointEvents(CustomPoint... points) {
        for (CustomPoint point : points) {
            point.setOnMouseDragged(mouseEvent -> {
                valueBox.setTextFill(Color.ORANGE);
                point.setCenterX(mouseEvent.getX());
                point.setCenterY(mouseEvent.getY());
                if (point == endPoint) {
                    endPoint2D = new Point2D(mouseEvent.getX(), mouseEvent.getY());
                    getPoints().set(4, point.getCenterX());
                    getPoints().set(5, point.getCenterY());
                }
                if (point == midPoint) {
                    midPoint2D = new Point2D(mouseEvent.getX(), mouseEvent.getY());
                    getPoints().set(2, point.getCenterX());
                    getPoints().set(3, point.getCenterY());
                }
                if (point == initialPoint) {
                    initialPoint2D = new Point2D(mouseEvent.getX(), mouseEvent.getY());
                    getPoints().set(0, point.getCenterX());
                    getPoints().set(1, point.getCenterY());
                }
                double roundedAngle;
                roundedAngle = calculateAngle(endPoint2D.getX(), endPoint2D.getY());
                valueBox.setVal(roundedAngle + "\u00B0");
                mouseEvent.consume();
            });
            point.setOnMousePressed(Event::consume);
            point.setOnMouseReleased(mouseEvent -> {
                valueBox.setTextFill(Color.SKYBLUE);
                mouseEvent.consume();
            });

        }

    }

    public void setCursorPositions(MouseEvent mouseEvent) {

        cursorXPosition = mouseEvent.getX();
        cursorYPosition = mouseEvent.getY();
    }


    public void setEvents() {

        this.setOnMouseMoved(Event::consume);
        this.setOnMouseEntered(mouseEvent -> {
            if(!marked) {
                setStroke(Color.ORANGE);
                setStrokeWidth(2);
                if (valueBox != null) {
                    valueBox.setTextFill(Color.ORANGE);
                }
            }
            mouseEvent.consume();
        });
        this.setOnMouseReleased(Event::consume);
        this.setOnMousePressed(mouseEvent -> {
            GUICreator.getInstance().unselectSelectedShape();
            marked = GUICreator.getInstance().setSelectedShape(this);
            cursorXPosition = mouseEvent.getX();
            cursorYPosition = mouseEvent.getY();
            mouseEvent.consume();
        });
        this.setOnMouseExited(mouseEvent -> {
            if(!marked) {
                setStroke(Color.SKYBLUE);
                setStrokeWidth(1);
                if (valueBox != null) {
                    valueBox.setTextFill(Color.SKYBLUE);
                }
            }
            mouseEvent.consume();
        });
        this.setOnMouseDragged(mouseEvent -> {
            if(!marked) {
                setStroke(Color.ORANGE);
                valueBox.setTextFill(Color.ORANGE);
            }
            double deltaX = mouseEvent.getX() - cursorXPosition;
            double deltaY = mouseEvent.getY() - cursorYPosition;
            getPoints().set(0, initialPoint2D.getX() + deltaX);
            getPoints().set(1, initialPoint2D.getY() + deltaY);
            getPoints().set(2, midPoint2D.getX() + deltaX);
            getPoints().set(3, midPoint2D.getY() + deltaY);
            getPoints().set(4, endPoint2D.getX() + deltaX);
            getPoints().set(5, endPoint2D.getY() + deltaY);
            setCursorPositions(mouseEvent);
            movePoint(deltaX, deltaY, initialPoint, midPoint, endPoint);
            mouseEvent.consume();
        });


    }

    public void unmark(){
        marked = false;
        this.setStroke(Color.SKYBLUE);
        valueBox.setTextFill(Color.SKYBLUE);
    }

    public boolean isDrawn() {
        return drawn;
    }

    public void clear(){
        geomGroup.getChildren().remove(this);
        geomGroup.getChildren().remove(initialPoint);
        geomGroup.getChildren().remove(midPoint);
        geomGroup.getChildren().remove(endPoint);
        geomGroup.getChildren().remove(valueBox);
    }

    public void setValueBoxEvents() {
        valueBox.setOnMouseEntered(mouseEvent -> {
            if(!marked) {
                setStroke(Color.ORANGE);
                valueBox.setTextFill(Color.ORANGE);
            }
            mouseEvent.consume();
        });
        valueBox.setOnMouseExited(mouseEvent -> {
            if(!marked) {
                setStroke(Color.SKYBLUE);
                valueBox.setTextFill(Color.SKYBLUE);
            }
            mouseEvent.consume();
        });
    }

    /**
     * Metoda licząca kąt między skrajnymi punktami, z wykorzystaniem Math.atan2
     *
     * @param x
     * @param y
     * @return
     */

    public double calculateAngle(double x, double y) {
        double b,c;
        double a = Math.sqrt(Math.pow(midPoint.getCenterX() - initialPoint.getCenterX(), 2) + Math.pow(midPoint.getCenterY() - initialPoint.getCenterY(), 2));
        if(endPoint != null) {

            b = Math.sqrt(Math.pow(endPoint.getCenterX() - midPoint.getCenterX(), 2) + Math.pow(endPoint.getCenterY() - midPoint.getCenterY(), 2));
            c = Math.sqrt(Math.pow(initialPoint.getCenterX() - endPoint.getCenterX(), 2) + Math.pow(initialPoint.getCenterY() - endPoint.getCenterY(), 2));
        }
        else{
            b = Math.sqrt(Math.pow(line.getEndX() - midPoint.getCenterX(), 2) + Math.pow(line.getEndY() - midPoint.getCenterY(), 2));
            c = Math.sqrt(Math.pow(initialPoint.getCenterX() - line.getEndX(), 2) + Math.pow(initialPoint.getCenterY() - line.getEndY(), 2));
        }
        double cosangle = (Math.pow(a,2) + Math.pow(b,2) - Math.pow(c,2))/(2*a*b);
        double angle = Math.toDegrees(Math.acos(cosangle));
        return Math.round(angle * 100.0) / 100.0;
    }

    /**
     * Metoda rysująca prostokąt z wartościami
     *
     * @param x
     * @param y
     */
    public void drawValueBox(double x, double y) {
        showValueBox();
        valueBox.setLayoutX(midPoint.getCenterX() + 30);
        valueBox.setLayoutY(midPoint.getCenterY());
        setValueBoxEvents();
        double roundedAngle = calculateAngle(x, y);
        valueBox.setVal(roundedAngle + "\u00B0");

    }

    /**
     * Metoda rysująca linię podczas tworzenia kąta
     *
     * @param cursorX
     * @param cursorY
     */
    public void drawLine(double cursorX, double cursorY) {

        if ((initialPoint2D != null && midPoint2D == null) || midPoint2D != null && endPoint == null) {
            if (initialPoint2D != null && midPoint2D == null) {
                line.setStartX(initialPoint2D.getX());
                line.setStartY(initialPoint2D.getY());

            } else {
                line.setStartX(midPoint2D.getX());
                line.setStartY(midPoint2D.getY());
                drawValueBox(cursorX, cursorY);
            }
            line.setEndX(cursorX);
            line.setEndY(cursorY);
            line.setStroke(Color.ORANGE);
            line.getStrokeDashArray().addAll(25d, 10d);
            line.setOnMouseDragged(Event::consume);

        }
    }

    /**
     * Główna metoda rysująca kąt
     * @param cursorX
     * @param cursorY
     */

    public void drawAngle(double cursorX, double cursorY) {

        if (initialPoint2D == null) {
            initialPoint2D = new Point2D(cursorX, cursorY);
            line = new Line();
            geomGroup.getChildren().add(line);
        } else if (midPoint2D == null) {
            midPoint2D = new Point2D(cursorX, cursorY);
        } else if (endPoint == null) {
            endPoint2D = new Point2D(cursorX, cursorY);
            geomGroup.getChildren().remove(line);
        }

        if (this.getPoints().isEmpty()) {
            initialPoint = new CustomPoint(cursorX, cursorY, 2, Color.SKYBLUE, geomGroup);
            getPoints().addAll(initialPoint2D.getX(), initialPoint2D.getY());
            return;
        }
        if (!this.contains(midPoint2D)) {
            midPoint = new CustomPoint(cursorX, cursorY, 2, Color.SKYBLUE, geomGroup);
            getPoints().addAll(midPoint2D.getX(), midPoint2D.getY());
            return;
        }
        if (!this.contains(endPoint2D)) {
            endPoint = new CustomPoint(cursorX, cursorY, 2, Color.SKYBLUE, geomGroup);
            getPoints().addAll(endPoint2D.getX(), endPoint2D.getY());
            midPoint.bindValueBox(valueBox, 30, 0);
            drawn = true;
            setPointEvents(initialPoint, endPoint, midPoint);
        }


    }

}
