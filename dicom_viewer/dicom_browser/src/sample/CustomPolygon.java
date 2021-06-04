package sample;

import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Klasa tworząca wielokąt otwarty bądź zamknięty
 */
public class CustomPolygon extends Polyline {
    private boolean marked;
    private CustomPoint point, initialPoint;
    private final ImageView imageView;
    private final List<CustomPoint> pointList;
    private Line line;
    private ValueBox valueBox;
    private final Map<CustomPoint, Double> valueMap;
    private double cursorXPosition, cursorYPosition;
    private boolean connected = false;
    private boolean drawn = false;
    private final double  pixelSpacing, windowWidthRatio, windowHeightRatio;
    private final double valX, valY;
    private double value = 0;
    private final Group geomGroup;

    public CustomPolygon(double startX, double startY, Group geomGroup, ImageView imageView, double pixelSpacing, double windowWidthRatio, double windowHeightRatio) {

        this.imageView = imageView;
        this.geomGroup = geomGroup;
        this.valX = Controller.getInstance().getPixelSpacing()*windowWidthRatio;
        this.valY = Controller.getInstance().getPixelSpacing()*windowHeightRatio;
        this.pixelSpacing = pixelSpacing;
        this.windowWidthRatio = windowWidthRatio;
        this.windowHeightRatio = windowHeightRatio;
        pointList = new ArrayList<>();
        valueMap = new HashMap<>();
        geomGroup.getChildren().add(this);
        drawPolyline(startX, startY);
        customize();
        setLine(startX, startY);
        setEvents();


    }

    public void clear(){
        geomGroup.getChildren().removeAll(pointList);
        geomGroup.getChildren().remove(valueBox);
        geomGroup.getChildren().remove(this);
    }

    public void unmark(){
        this.setStroke(Color.SKYBLUE);
        valueBox.setTextFill(Color.SKYBLUE);
        marked = false;
    }


    /**
     * Metoda obliczająca pole wielokąta zamkniętego
     *
     * @param x
     * @param y
     * @param n
     * @return
     */
    public static double polygonArea(double[] x, double[] y, int n) {

        double area = 0.0;
        int j = n - 1;
        for (int i = 0; i < n; i++) {
            area += (x[j] + x[i]) * (y[j] - y[i]);
            j = i;
        }
        return Math.abs(area / 2.0);

    }


    public void setValueBox() {
        valueBox = new ValueBox(geomGroup, 40, 20);
        valueBox.setLayoutX(initialPoint.getCenterX() - 50);
        valueBox.setLayoutY(initialPoint.getCenterY());
        setValueBoxEvents();


    }

    public void setLine(double startX, double startY) {
        line = new Line();
        line.setStartX(startX);
        line.setStartY(startY);
        line.setEndX(startX);
        line.setEndY(startY);
        line.setStroke(Color.SKYBLUE);
        line.setOnMouseDragged(Event::consume);
        geomGroup.getChildren().add(line);
    }


    public void drawLine(double cursorX, double cursorY) {
        line.setEndX(cursorX);
        line.setEndY(cursorY);
    }


    public void setDrawn() {
        drawn = true;
    }


    public void customize() {
        setStroke(Color.SKYBLUE);
        setStrokeWidth(1);
    }

    public void setValueBoxEvents() {
        valueBox.setOnMouseEntered(mouseEvent -> {
            valueBox.setTextFill(Color.ORANGE);
            this.setStroke(Color.ORANGE);
        });

        valueBox.setOnMouseExited(mouseEvent -> {
            valueBox.setTextFill(Color.SKYBLUE);
            this.setStroke(Color.SKYBLUE);
        });
    }

    public double calcArea() {



        double[] xCoordsArray = new double[this.getPoints().size() / 2];
        double[] yCoordsArray = new double[this.getPoints().size() / 2];
        int a = 0;
        int b = 1;
        for (int i = 0; i < xCoordsArray.length; i++) {
            xCoordsArray[i] = (this.getPoints().get(a) * windowWidthRatio);
            yCoordsArray[i] = (this.getPoints().get(b) * windowHeightRatio);
            a += 2;
            b += 2;
        }
        return polygonArea(xCoordsArray, yCoordsArray, xCoordsArray.length);
    }

    public void setPointEvents(CustomPoint point) {

        point.setOnMouseDragged(mouseEvent -> {
            mouseEvent.consume();
            if (drawn) {
                valueBox.setTextFill(Color.ORANGE);
                //zmiana współrzędnych punktu dragowanego
                point.setCenterX(mouseEvent.getX());
                point.setCenterY(mouseEvent.getY());
                int index = pointList.indexOf(point);
                pointList.set(index, point);
                int xIndex = pointList.indexOf(point) * 2;
                int yIndex = xIndex + 1;
                this.getPoints().set(xIndex, point.getCenterX());
                this.getPoints().set(yIndex, point.getCenterY());
                // jeśli wielokąt jest zamknięty, przesuń też ostatni punkt
                if (connected) {
                    if (point == initialPoint) {
                        this.getPoints().set(this.getPoints().size() - 1, point.getCenterY());
                        this.getPoints().set(this.getPoints().size() - 2, point.getCenterX());
                    }
                }
                double firstVal = 0;
                double secondVal;
                //jeśli przesuwamy punkt początkowy
                if (index == 0) {
                    //jeśli wielokąt zamknięty, liczymy dla ostatniego punktu i dla drugiego
                    if (connected) {
                        firstVal = calculateValue(pointList.get(pointList.size() - 1));
                    }
                    //jeśli otwarty, liczymy dla pierwszego i dla drugiego
                    else {
                        firstVal = calculateValue(pointList.get(index + 1));
                    }
                    CustomPoint nextPoint = pointList.get(index + 1);
                    value = value - valueMap.get(nextPoint);
                    secondVal = calculateValue(nextPoint);
                    value = value + secondVal;
                    valueMap.replace(nextPoint, secondVal);
                }
                // jeśli nie bierzemy skrajnego punktu
                else if (index != pointList.size() - 1) {
                    firstVal = calculateValue(point);
                    CustomPoint nextPoint = pointList.get(index + 1);
                    value = value - valueMap.get(point) - valueMap.get(nextPoint);
                    secondVal = calculateValue(nextPoint);
                    value = value + firstVal + secondVal;
                    valueMap.replace(nextPoint, secondVal);

                }
                // jeśli bierzemy ostatni punkt(wykluczamy wielokąt zamknięty, bo w nim ostatni jest pierwszym)
                else if (!connected) {
                    value = value - valueMap.get(point);
                    firstVal = calculateValue(point);
                    value = value + firstVal;
                }
                valueMap.replace(point, firstVal);
            }
            double roundedValue = valueBox.calculateValue(value, 10);
            if (connected) {
                showValue(value, roundedValue, calcArea());
                return;
            }
            showValue(value, roundedValue);


        });

        point.setOnMousePressed(Event::consume);

        point.setOnMouseReleased(mouseEvent -> valueBox.setTextFill(Color.SKYBLUE));


    }

    public void setEvents() {

        this.setOnMouseMoved(Event::consume);
        this.setOnMousePressed(mouseEvent -> {
            GUICreator.getInstance().unselectSelectedShape();
            marked = GUICreator.getInstance().setSelectedShape(this);
            cursorXPosition = mouseEvent.getX();
            cursorYPosition = mouseEvent.getY();
            mouseEvent.consume();
        });

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
            if (drawn) {
                if(!marked) {
                    setStroke(Color.ORANGE);
                    valueBox.setTextFill(Color.ORANGE);
                }
                for (int i = 0; i < pointList.size(); i++) {
                    point = pointList.get(i);
                    point.setCenterX(point.getCenterX() + mouseEvent.getX() - cursorXPosition);
                    point.setCenterY(point.getCenterY() + mouseEvent.getY() - cursorYPosition);
                    int xIndex = pointList.indexOf(point) * 2;
                    int yIndex = xIndex + 1;
                    if (connected && i == pointList.size() - 1) {
                        this.getPoints().set(this.getPoints().size() - 2, initialPoint.getCenterX());
                        this.getPoints().set(this.getPoints().size() - 1, initialPoint.getCenterY());
                    } else {
                        this.getPoints().set(xIndex, point.getCenterX());
                        this.getPoints().set(yIndex, point.getCenterY());
                    }
                    pointList.set(i, point);
                }
                cursorXPosition = mouseEvent.getX();
                cursorYPosition = mouseEvent.getY();
            }
            mouseEvent.consume();
        });


    }

    /**
     * Metoda zamykająca wielokąt
     */
    public void connect() {
        CustomPoint endPoint = new CustomPoint(initialPoint.getCenterX(), initialPoint.getCenterY(), 2, Color.SKYBLUE, geomGroup);
        geomGroup.getChildren().remove(endPoint);
        geomGroup.getChildren().remove(line);
        pointList.add(endPoint);
        this.getPoints().addAll((initialPoint.getCenterX()), initialPoint.getCenterY());
        double val = calculateValue(endPoint);
        valueMap.put(endPoint, val);
        value = value + val;
        double roundedValue = valueBox.calculateValue(value, 10);
        showValue(value, roundedValue, calcArea());
        connected = true;

    }

    public void drawPolyline(double cursorX, double cursorY) {
        point = new CustomPoint(cursorX, cursorY, 2, Color.SKYBLUE, geomGroup);
        setPointEvents(point);
        this.getPoints().addAll(point.getCenterX(), point.getCenterY());
        pointList.add(point);
        if (this.getPoints().size() == 2) {
            initialPoint = point;
            setValueBox();
            initialPoint.bindValueBox(valueBox, -50, 0);
            return;
        }
        line.setStartX(point.getCenterX());
        line.setStartY(point.getCenterY());
        double val = calculateValue(point);
        valueMap.put(point, val);
        value = value + val;
        double roundedValue = valueBox.calculateValue(value, 10);
        if (connected) {
            showValue(value, roundedValue, 0);
            return;
        }
        showValue(value, roundedValue);


    }

    public void showValue(double value, double roundedValue) {
        if (value >= 10) {
            valueBox.setVal(roundedValue + "cm");
            return;
        }
        valueBox.setVal(roundedValue + "mm");

    }

    public void showValue(double value, double roundedValue, double area) {

        double realArea = area * Math.pow(pixelSpacing, 2);
        double roundedArea = valueBox.calculateValue(realArea, 100);
        if (value >= 10 && realArea >= 100) {
            valueBox.setVal(roundedValue + "cm" + "\nAREA= " + roundedArea + "cm²");
            return;
        }
        if(value >=10 && realArea <100){
            valueBox.setVal(roundedValue + "cm" + "\nAREA= " + roundedArea + "mm²");
            return;
        }
        valueBox.setVal(roundedValue + "mm" + "\nAREA= " + roundedArea + "mm²");


    }

    public double calculateValue(CustomPoint point) {
        int index = pointList.indexOf(point);
        CustomPoint previousPoint = pointList.get(index - 1);
        double deltaX = Math.abs(point.getCenterX() - previousPoint.getCenterX());
        double deltaY = Math.abs(point.getCenterY() - previousPoint.getCenterY());
        return Math.sqrt(Math.pow(deltaX * valX, 2) + Math.pow(deltaY * valY, 2));

    }
}
