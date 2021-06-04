package sample;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

import java.util.*;

public class CustomEllipse extends Ellipse {

    private boolean marked;
    public CustomPoint point1, point2, point3, point4;
    private boolean visible = true;
    private final List<Thread> threadList;
    private double maxPixelVal, minPixelVal, sum;
    private final ValueBox valueBox;
    private final double pixelSpacing;
    private double cursorXPosition, cursorYPosition;
    private final double windowWidthRatio, windowHeightRatio;
    private CustomPoint selectedPoint;
    private final List<Double> pixelValueList;
    private final TreeItem<String> selectedItem;
    double imageWidth, imageHeight;
    private Group geomGroup;


    public CustomEllipse(double startX, double startY, Group geomGroup, ImageView imageView, double pixelSpacing, double windowWidthRatio, double windowHeightRatio, TreeItem<String> selectedItem) {
        geomGroup.getChildren().add(this);
        this.pixelSpacing = pixelSpacing;
        threadList = new ArrayList<>();
        point1 = new CustomPoint(startX, startY, 2, Color.SKYBLUE, geomGroup);
        point2 = new CustomPoint(startX, startY, 2, Color.SKYBLUE, geomGroup);
        point3 = new CustomPoint(startX, startY, 2, Color.SKYBLUE, geomGroup);
        point4 = new CustomPoint(startX, startY, 2, Color.SKYBLUE, geomGroup);
        valueBox = new ValueBox(geomGroup, 100, 30);
        pixelValueList = new ArrayList<>();
        setStroke(Color.SKYBLUE);
        setFill(null);
        setStrokeWidth(1);
        this.geomGroup = geomGroup;
        this.windowWidthRatio = windowWidthRatio;
        this.windowHeightRatio = windowHeightRatio;
        this.selectedItem = selectedItem;
        selectedPoint = point3;
        valueBox.setVisible(false);
        hidePoints(point1, point2, point3, point4);
        setEvents();
        cursorXPosition = startX;
        cursorYPosition = startY;
        imageHeight = imageView.getImage().getHeight();
        imageWidth = imageView.getImage().getWidth();
    }

    public void hidePoints(CustomPoint... points) {
        if (visible) {
            for (CustomPoint point : points) {
                point.setVisible(false);
            }
            visible = false;
        }
    }

    public void clear() {
        geomGroup.getChildren().remove(this);
        geomGroup.getChildren().remove(point1);
        geomGroup.getChildren().remove(point2);
        geomGroup.getChildren().remove(point3);
        geomGroup.getChildren().remove(point4);
        geomGroup.getChildren().remove(valueBox);
    }

    public void unmark() {
        marked = false;
        this.setStroke(Color.SKYBLUE);
        valueBox.setTextFill(Color.SKYBLUE);
    }

    public void showPoints(CustomPoint... points) {
        if (!visible) {
            for (CustomPoint point : points) {
                point.setVisible(true);
            }
            visible = true;
        }
    }

    public void setCursorPositions(MouseEvent mouseEvent) {

        cursorXPosition = mouseEvent.getX();
        cursorYPosition = mouseEvent.getY();
    }

    public void movePoints(double deltaX, double deltaY, CustomPoint... points) {
        for (CustomPoint point : points) {
            point.setCenterX(point.getCenterX() + deltaX);
            point.setCenterY(point.getCenterY() + deltaY);
        }
    }

    /**
     * Metoda sprawdzająca czy dany punkt leży w elipsie
     *
     * @param centerX współrzędna X centrum elipsy
     * @param centerY współrzędna Y centrum elipsy
     * @param pointX  współrzędna X danego punktu
     * @param pointY  współrzędna Y danego punktu
     * @param radiusX promień elipsy w osi X
     * @param radiusY promień elipsy w osi Y
     * @return wartość decydująca czy punkt leży wewnątrz, na, czy na zewnątrz elipsy
     */
    double checkpoint(double centerX, double centerY, double pointX,
                      double pointY, double radiusX, double radiusY) {
        return ((Math.pow((pointX - centerX), 2) / Math.pow(radiusX, 2))
                + (Math.pow((pointY - centerY), 2) / Math.pow(radiusY, 2)));
    }


    /**
     * Metoda odnajdująca maksymalną wartość piksela wewnątrz obszaru wyznaczonego przez elipsę.
     * Początkowo następuje przeskalowanie wymiarów obrazu wyświetlanego do rzeczywistych wymiarów obrazu.
     * Następnie cały obszar jest przeszukiwany w poszukiwaniu maksymalnej wartości.
     * Metoda sprawdza również, czy dany punkt leży wewnątrz elipsy i tylko dla takiego punktu odnajduje wartość maksymalną.
     */
    public void findMaximumPixelValue() {
        sum = 0;
        int counter = 0;
        // transformacja współrzędnych obrazu dopasowanego do okna
        // do współrzędnych rzeczywistych obrazu
        double realPoint1X = (int) ((point1.getCenterX()) * windowWidthRatio);
        double realPoint1Y = (int) ((point1.getCenterY()) * windowHeightRatio);
        double realPoint3X = (int) ((point3.getCenterX()) * windowWidthRatio);
        double realPoint3Y = (int) ((point3.getCenterY()) * windowHeightRatio);
        double centerX = (realPoint3X + realPoint1X) / 2;
        double centerY = (realPoint3Y + realPoint1Y) / 2;
        double deltaX = realPoint3X - realPoint1X;
        double deltaY = realPoint3Y - realPoint1Y;
        double radiusX = deltaX / 2;
        double radiusY = deltaY / 2;
        double xPos, yPos;
        for (int i = 0; i < Math.abs(deltaY); i++) {
            for (int j = 0; j < Math.abs(deltaX); j++) {
                double p;
                // warunki określające w którym kierunku rysowana jest elipsa
                if (realPoint1X < realPoint3X) {
                    xPos = realPoint1X + j;
                } else {
                    xPos = realPoint3X + j;
                }
                if (realPoint1Y < realPoint3Y) {
                    yPos = realPoint1Y + i;
                } else {
                    yPos = realPoint3Y + i;
                }
                // sprawdzenie położenia punktu względem elipsy
                p = checkpoint(centerX, centerY, xPos, yPos, radiusX, radiusY);
                if (p < 1) {
                    double pixelVal = 0;
                    if (xPos > 0 && xPos < imageWidth - 1 && yPos > 0 && yPos < imageHeight - 1) {
                        // pobranie aktualnego piksela z tablicy wszystkich pikseli
                        pixelVal = Controller.getInstance().getPixelValue((int) ((yPos) * imageWidth), (int) xPos, selectedItem);
                        // wyznaczenie maksymalnej i minimalnej wartości piksela w zaznaczonym obszarze
                        if (counter == 0) {
                            maxPixelVal = pixelVal;
                            minPixelVal = pixelVal;
                            counter = 1;
                        } else {
                            if (pixelVal > maxPixelVal) {
                                maxPixelVal = pixelVal;
                            }
                            if (pixelVal < minPixelVal) {
                                minPixelVal = pixelVal;
                            }
                        }
                    }
                    // dodanie piksela do listy w celu wyznaczenia odchylenia standardowego
                    pixelValueList.add(pixelVal);
                    // sumowanie wszystkich pikseli w celu wyznaczenia średniej
                    sum += pixelVal;
                }
            }
        }
    }

    /**
     * Metoda rysująca elipsę
     *
     * @param cursorX
     * @param cursorY
     */

    public void drawElipse(double cursorX, double cursorY) {

        valueBox.setVisible(true);
        double deltaX, deltaY;
        CustomPoint oppositePoint;
        selectedPoint.setCenterX(cursorX);
        selectedPoint.setCenterY(cursorY);
        if (selectedPoint == point1) {
            point2.setCenterY(cursorY);
            point4.setCenterX(cursorX);
            oppositePoint = point3;

        } else if (selectedPoint == point2) {
            point1.setCenterY(cursorY);
            point3.setCenterX(cursorX);
            oppositePoint = point4;

        } else if (selectedPoint == point3) {
            point2.setCenterX(cursorX);
            point4.setCenterY(cursorY);
            oppositePoint = point1;

        } else {
            point1.setCenterX(cursorX);
            point3.setCenterY(cursorY);
            oppositePoint = point2;

        }
        deltaX = Math.abs(cursorX - oppositePoint.getCenterX());
        deltaY = Math.abs(cursorY - oppositePoint.getCenterY());
        setCenterX((cursorX + oppositePoint.getCenterX()) / 2);
        setCenterY((cursorY + oppositePoint.getCenterY()) / 2);

        if ((point1.getCenterX() > point3.getCenterX() && point1.getCenterY() > point3.getCenterY())) {
            oppositePoint = point1;
        } else if (point3.getCenterX() < point1.getCenterX() && point3.getCenterY() > point1.getCenterY()) {
            oppositePoint = point4;
        } else if (point3.getCenterX() < point1.getCenterX()) {
            oppositePoint = point2;
        } else {
            oppositePoint = point3;

        }
        valueBox.setLayoutX(oppositePoint.getCenterX() + 85);
        valueBox.setLayoutY(oppositePoint.getCenterY());
        oppositePoint.bindValueBox(valueBox, 85, 0);
        showPoints(point1, point2, point3, point4);
        setRadiusX(deltaX / 2);
        setRadiusY(deltaY / 2);
        cursorXPosition = cursorX;
        cursorYPosition = cursorY;
        setValues();

    }


    /**
     * Metoda licząca odchylenie standardowe
     *
     * @param pixelValueList
     * @return
     */

    public double calculateStandardDeviation(List<Double> pixelValueList) {
        double sum = 0.0, standardDeviation = 0.0;
        int length = pixelValueList.size();

        for (double value : pixelValueList) {
            sum += value;
        }
        double mean = sum / length;

        for (double num : pixelValueList) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }

    /**
     * Metoda umieszczająca proces liczenia wartości w osobnym wątku, aby nie zakłócać płynności interfejsu graficznego.
     * Każdy kolejny wątek czega na zakończenie wszystkich wątków na liście.
     */
    public void setValues() {
        if (threadList.isEmpty()) {
            Thread thread = new Thread(this::calculateBoxValues);
            threadList.add(thread);
            thread.start();
        } else {
            boolean alive = false;
            for (Thread thread : threadList) {
                if (thread.isAlive()) {
                    alive = true;
                    break;
                }
            }
            if (!alive) {
                Thread thread = new Thread(this::calculateBoxValues);
                threadList.add(thread);
                thread.start();
            }
        }
    }


    /**
     * Metoda licząca wartości max,min,sd,avg dla danego położenia elipsy i wyświetlająca je w interfejsie graficznym
     */
    public synchronized void calculateBoxValues() {
        double roundedAverageValue = 0;
        double standardDeviation = 0;
        findMaximumPixelValue();
        if (!pixelValueList.isEmpty()) {
            double averageValue = sum / pixelValueList.size();
            standardDeviation = calculateStandardDeviation(pixelValueList);
            roundedAverageValue = Math.round(averageValue * 100.0) / 100.0;
            pixelValueList.clear();
        }
        double pixelArea = Math.PI * getRadiusX() * windowWidthRatio * getRadiusY() * windowHeightRatio;
        double roundedPixelArea = Math.round(pixelArea);
        double realArea = pixelArea * Math.pow(pixelSpacing, 2);
        double roundedArea = valueBox.calculateValue(realArea, 100);
        if (realArea >= 100) {
            double finalRoundedAverageValue = roundedAverageValue;
            double finalStandardDeviation = standardDeviation;
            Platform.runLater(() -> valueBox.setVal("AREA= " + roundedArea + "cm²(" + roundedPixelArea + "px)\nMAX= " + maxPixelVal + "\nAVG= " + finalRoundedAverageValue + "\nMIN= " + minPixelVal + "\nSD= " + finalStandardDeviation));

        } else {
            double finalRoundedAverageValue1 = roundedAverageValue;
            double finalStandardDeviation1 = standardDeviation;
            Platform.runLater(() -> valueBox.setVal("AREA= " + roundedArea + "mm²(" + roundedPixelArea + "px)\nMAX= " + maxPixelVal + "\nAVG= " + finalRoundedAverageValue1 + "\nMIN= " + minPixelVal + "\nSD= " + finalStandardDeviation1));

        }

    }


    public void setValueBoxEvents() {
        valueBox.setOnMouseEntered(mouseEvent -> {
            if (!marked) {
                this.setStroke(Color.ORANGE);
                valueBox.setTextFill(Color.ORANGE);
            }
            mouseEvent.consume();
        });

        valueBox.setOnMouseExited(mouseEvent -> {
            if (!marked) {
                this.setStroke(Color.SKYBLUE);
                valueBox.setTextFill(Color.SKYBLUE);
            }
            mouseEvent.consume();
        });
    }

    public void setPointEvents(CustomPoint... points) {
        for (CustomPoint point : points) {
            point.setOnMousePressed(mouseEvent -> {
                setCursorPositions(mouseEvent);
                mouseEvent.consume();
            });

            point.setOnMouseReleased(mouseEvent -> valueBox.setTextFill(Color.SKYBLUE));

            point.setOnMouseDragged(mouseEvent -> {
                selectedPoint = point;
                valueBox.setTextFill(Color.ORANGE);
                drawElipse(mouseEvent.getX(), mouseEvent.getY());
                setCursorPositions(mouseEvent);
                mouseEvent.consume();
            });
        }
    }

    public void setEvents() {
        setValueBoxEvents();
        this.setOnMouseMoved(Event::consume);
        setPointEvents(point1, point2, point3, point4);
        this.setOnMouseEntered(mouseEvent -> {
            setStroke(Color.ORANGE);
            setStrokeWidth(2);
            valueBox.setTextFill(Color.ORANGE);
            mouseEvent.consume();
        });
        this.setOnMouseExited(mouseEvent -> {
            if (!marked) {
                setStroke(Color.SKYBLUE);
                valueBox.setTextFill(Color.SKYBLUE);
            } else {
                setStroke(Color.VIOLET);
                valueBox.setTextFill(Color.ORANGE);
            }
            setStrokeWidth(1);
            mouseEvent.consume();
        });
        this.setOnMousePressed(mouseEvent -> {
            GUICreator.getInstance().unselectSelectedShape();
            marked = GUICreator.getInstance().setSelectedShape(this);
            valueBox.setTextFill(Color.ORANGE);
            setCursorPositions(mouseEvent);
            mouseEvent.consume();
        });

        this.setOnMouseDragged(mouseEvent -> {
            if(!marked) {
                setStroke(Color.ORANGE);
            }
            else{
                setStroke(Color.VIOLET);
            }
            valueBox.setTextFill(Color.ORANGE);
            double deltaX = mouseEvent.getX() - cursorXPosition;
            double deltaY = mouseEvent.getY() - cursorYPosition;
            setCenterX(getCenterX() + deltaX);
            setCenterY(getCenterY() + deltaY);
            movePoints(deltaX, deltaY, point1, point2, point3, point4);
            setCursorPositions(mouseEvent);
            setValues();


            mouseEvent.consume();
        });

        this.setOnMouseMoved(Event::consume);
    }
}
