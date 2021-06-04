package sample;


import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.shape.Line;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa tworząca linię i rozmieszczająca ją w GUI
 */
public class CustomLine extends Line {

    private final CustomPoint initialPoint, endPoint;
    private double endX, endY;
    private final double startX, startY;
    private double cursorXPosition, cursorYPosition;
    private final ValueBox valueBox;
    private double valueX = 0, valueY = 0;
    private boolean marked;
    private int ala = 0;
    private final double valX, valY;
    private TreeItem<String> selectedItem;
    private double windowWidthRatio;
    private double windowHeightRatio;
    private double imageWidth;
    private double imageHeight;
    private Group geomGroup;
    private List<Double> pixelXposList, pixelYPosList;


    public void showInitialPoint() {
        initialPoint.setVisible(true);
    }

    public void setCursorPositions(double cursorX, double cursorY) {

        cursorXPosition = cursorX;
        cursorYPosition = cursorY;
    }


    public void hideValueBox() {
        valueBox.setVisible(false);
    }

    public void showValueBox() {
        valueBox.setVisible(true);
    }

    public void hideInitialPoint() {
        initialPoint.setVisible(false);
    }

    public void hideEndPoint() {
        endPoint.setVisible(false);
    }


    public CustomLine(double startX, double startY, double endX, double endY, Group geomGroup, ImageView imageView, double windowWidthRatio, double windowHeightRatio, TreeItem<String> selectedItem) {
        geomGroup.getChildren().add(this);
        pixelXposList = new ArrayList<>();
        pixelYPosList = new ArrayList<>();
        initialPoint = new CustomPoint(startX, startY, 2, Color.SKYBLUE, geomGroup);
        endPoint = new CustomPoint(startX, startY, 2, Color.SKYBLUE, geomGroup);
        valueBox = new ValueBox(geomGroup, 40, 20);
        endPoint.bindValueBox(valueBox, 30, 0);
        hideValueBox();
        hideInitialPoint();
        hideEndPoint();
        this.geomGroup = geomGroup;
        this.windowWidthRatio = windowWidthRatio;
        this.windowHeightRatio = windowHeightRatio;
        this.selectedItem = selectedItem;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.valX = Controller.getInstance().getPixelSpacing() * windowWidthRatio;
        this.valY = Controller.getInstance().getPixelSpacing() * windowHeightRatio;
        imageHeight = imageView.getImage().getHeight();
        imageWidth = imageView.getImage().getWidth();
    }


    public List<Double> calculatePixelVal() {

        List<Double> pixelValueList = new ArrayList<>();
        int realStartX = (int) (getStartX() * windowWidthRatio);
        int realStartY = (int) (getStartY() * windowHeightRatio);
        int realEndX = (int) (getEndX() * windowWidthRatio);
        int realEndY = (int) (getEndY() * windowHeightRatio);

        Line fakeLine = new Line();
        fakeLine.setStartX(realStartX);
        fakeLine.setStartY(realStartY);
        fakeLine.setEndX(realEndX);
        fakeLine.setEndY(realEndY);
        double deltaY = Math.abs(realEndY - realStartY);
        double deltaX = Math.abs(realEndX - realStartX);


        for (int i = 0; i < deltaY + 1; i++) {
            for (int j = 0; j < deltaX + 1; j++) {
                int xPos = realEndX;
                int yPos = realEndY;
                if (realEndY > realStartY) {
                    yPos = realStartY + i;
                } else if (realEndY < realStartY) {
                    yPos = realEndY + i;
                }
                if (realEndX > realStartX) {
                    xPos = realStartX + j;
                } else if (realEndX < realStartX) {
                    xPos = realEndX + j;
                }
                if (fakeLine.contains(xPos, yPos)) {
                    double pixelVal = Controller.getInstance().getPixelValue((int) (yPos * imageWidth), xPos, selectedItem);
                    pixelValueList.add(pixelVal);
                }
            }
        }

        return pixelValueList;
    }


    public void drawLine(double cursorX, double cursorY) {
        endX = cursorX;
        endY = cursorY;
        pixelXposList.add(endX);
        pixelYPosList.add(endY);
        moveEndpoint(endX, endY);
        showInitialPoint();
        showEndPoint();
        setStartX(startX);
        setStartY(startY);
        setEndX(endX);
        setEndY(endY);
        setStroke(Color.SKYBLUE);
        setStrokeWidth(1);
        if (cursorX - cursorXPosition > 0) {
            valueBox.setLayoutX(endPoint.getCenterX() + 30);
            valueBox.setLayoutY(endPoint.getCenterY());
        } else {
            valueBox.setLayoutX(initialPoint.getCenterX() + 30);
            valueBox.setLayoutY(initialPoint.getCenterY());
        }
        showEndPoint();
        showValueBox();

        // calculatePixelVal();

    }


    public void moveEndpoint(double x, double y) {
        endPoint.setCenterX(x);
        endPoint.setCenterY(y);
    }

    public void showEndPoint() {
        endPoint.setVisible(true);
    }

    public boolean isMarked() {
        return marked;
    }


    public void unmark(){
        this.setStroke(Color.SKYBLUE);
        valueBox.setTextFill(Color.SKYBLUE);
        marked = false;
    }


    public void showValue() {
        double value = valueX + valueY;
        double roundedValue = valueBox.calculateValue(value, 10);
        if (value >= 10) {
            valueBox.setVal(roundedValue + "cm");
        } else {
            valueBox.setVal(roundedValue + "mm");
        }
    }


    public void clear() {
        geomGroup.getChildren().remove(this);
        geomGroup.getChildren().remove(initialPoint);
        geomGroup.getChildren().remove(endPoint);
        geomGroup.getChildren().remove(valueBox);
    }

    public void setLineEvents() {
        setPointEvents(initialPoint);
        setPointEvents(endPoint);
        this.startXProperty().addListener((observableValue, number, t1) -> {
            // warunek, aby wartość nie została przypisana przy tworzeniu punktu początkowego, a jedynie przy jego zmianach
            if ((double) number != 0) {
                double deltaX = Math.abs((double) t1 - getEndX());
                valueX = deltaX * valX;

            }

        });

        this.startYProperty().addListener((observableValue, number, t1) -> {
            // warunek, aby wartość nie została przypisana przy tworzeniu punktu początkowego, a jedynie przy jego zmianach
            if ((double) number != 0) {
                double deltaY = Math.abs((double) t1 - getEndY());
                valueY = deltaY * valY;
                showValue();

            }

        });

        this.endXProperty().addListener((observableValue, number, t1) -> {
            double deltaX = Math.abs((double) t1 - getStartX());
            valueX = deltaX * valX;
            showValue();

        });

        this.endYProperty().addListener((observableValue, number, t1) -> {
            double deltaY = Math.abs((double) t1 - getStartY());
            valueY = deltaY * valY;
            showValue();
        });

        this.setOnMouseDragged(mouseEvent -> {
            if(!marked) {
                setStroke(Color.ORANGE);
                valueBox.setTextFill(Color.ORANGE);
            }
            setStartX(getStartX() + mouseEvent.getX() - cursorXPosition);
            setStartY(getStartY() + mouseEvent.getY() - cursorYPosition);
            if (valueBox.getLayoutX() == initialPoint.getCenterX() + 30) {
                initialPoint.bindValueBox(valueBox, 30, 0);
            } else if (valueBox.getLayoutX() == endPoint.getCenterX() + 30) {
                endPoint.bindValueBox(valueBox, 30, 0);
            }
            initialPoint.setCenterX(getStartX());
            initialPoint.setCenterY(getStartY());
            setEndX(getEndX() + mouseEvent.getX() - cursorXPosition);
            setEndY(getEndY() + mouseEvent.getY() - cursorYPosition);
            endPoint.setCenterX(getEndX());
            endPoint.setCenterY(getEndY());
            cursorXPosition = mouseEvent.getX();
            cursorYPosition = mouseEvent.getY();
            mouseEvent.consume();

        });

        this.setOnMouseMoved(Event::consume);
        this.setOnMousePressed(mouseEvent -> {
                    GUICreator.getInstance().unselectSelectedShape();
                    marked = GUICreator.getInstance().setSelectedShape(this);
                    cursorXPosition = mouseEvent.getX();
                    cursorYPosition = mouseEvent.getY();
                    if (mouseEvent.isSecondaryButtonDown()) {
                        new LineProfileChart(calculatePixelVal());
                    }
                    mouseEvent.consume();
                }
        );

        this.setOnMouseReleased(Event::consume);
        this.setOnMouseEntered(mouseEvent -> {
            if(!marked) {
                setStroke(Color.ORANGE);
                setStrokeWidth(2);
                valueBox.setTextFill(Color.ORANGE);
            }


        });
        this.setOnMouseExited(mouseEvent -> {
            if(!marked) {
                setStroke(Color.SKYBLUE);
                setStrokeWidth(1);
                valueBox.setTextFill(Color.SKYBLUE);
            }

        });

        valueBox.setOnMouseEntered(mouseEvent -> {
            if(!marked) {
                setStroke(Color.ORANGE);
                valueBox.setTextFill(Color.ORANGE);
            }
        });
        valueBox.setOnMouseExited(mouseEvent -> {
            if(!marked) {
                setStroke(Color.SKYBLUE);
                valueBox.setTextFill(Color.SKYBLUE);
            }
        });

    }


    private void setPointEvents(CustomPoint point) {

        point.setOnMouseReleased(mouseEvent -> valueBox.setTextFill(Color.SKYBLUE));
        point.setOnMouseDragged(mouseEvent -> {
            valueBox.setTextFill(Color.ORANGE);
            point.setCenterX(mouseEvent.getX());
            point.setCenterY(mouseEvent.getY());
            if (point == endPoint) {
                setEndX(mouseEvent.getX());
                setEndY(mouseEvent.getY());
                endPoint.bindValueBox(valueBox, 30, 0);
            }
            if (point == initialPoint) {
                setStartX(mouseEvent.getX());
                setStartY(mouseEvent.getY());
                initialPoint.bindValueBox(valueBox, 30, 0);
            }
            mouseEvent.consume();
        });


    }


}