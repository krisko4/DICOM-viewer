package sample;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

/**
 * Klasa tworząca kąt Cobba
 */

public class CobbAngle extends Shape {
    private boolean marked;
    private Group geomGroup;
    private boolean drawn;
    private int counter = 1;
    private Line line1, line2, midLine;
    private double cursorXPosition, cursorYPosition;
    private CustomPoint point1, point2, point3, point4;
    private ValueBox valueBox;

    public CobbAngle(double startX, double startY, Group geomGroup) {
        this.geomGroup = geomGroup;
        drawCobbAngle(startX, startY);
    }


    public void setLine(Line line, double cursorX, double cursorY) {
        line.setStroke(Color.SKYBLUE);
        line.setStartX(cursorX);
        line.setStartY(cursorY);
        line.setEndX(cursorX);
        line.setEndY(cursorY);
        setLineEvents(line);
        geomGroup.getChildren().add(line);
    }

    public void unmark(){
        marked = false;
        line1.setStroke(Color.SKYBLUE);
        line2.setStroke(Color.SKYBLUE);
        valueBox.setTextFill(Color.SKYBLUE);
    }

    public void setDrawn() {
        if (counter == 5) {
            drawn = true;
        }
    }

    public void setSelected(){
        line1.setStroke(Color.VIOLET);
        line2.setStroke(Color.VIOLET);
    }

    public void clear(){
        geomGroup.getChildren().remove(midLine);
        geomGroup.getChildren().remove(line1);
        geomGroup.getChildren().remove(line2);
        geomGroup.getChildren().remove(point1);
        geomGroup.getChildren().remove(point2);
        geomGroup.getChildren().remove(point3);
        geomGroup.getChildren().remove(point4);
        geomGroup.getChildren().remove(valueBox);
    }

    public void drawCobbAngle(double cursorX, double cursorY) {
        if (counter == 4) {
            point4 = new CustomPoint(cursorX, cursorY, 2, Color.SKYBLUE, geomGroup);
            line2.setEndX(cursorX);
            line2.setEndY(cursorY);
            setPointEvents(point4);
        }
        if (counter == 1) {
            line1 = new Line();
            setLine(line1, cursorX, cursorY);
            point1 = new CustomPoint(cursorX, cursorY, 2, Color.SKYBLUE, geomGroup);
         //   setPointEvents(point1);
        }
        if (counter == 2) {
            point2 = new CustomPoint(cursorX, cursorY, 2, Color.SKYBLUE, geomGroup);
          //  setPointEvents(point2);
            line1.setEndX(cursorX);
            line1.setEndY(cursorY);
            midLine = new Line();
            midLine.getStrokeDashArray().addAll(4d);
            midLine.setStroke(Color.WHITE);
            geomGroup.getChildren().add(midLine);
            valueBox = new ValueBox(geomGroup, 40, 20);
            if (point2.getCenterX() > point1.getCenterX()) {
                valueBox.setLayoutX(point2.getCenterX() + 30);
                valueBox.setLayoutY(point2.getCenterY());
                point2.bindValueBox(valueBox,30,0);
                midLine.setStartX(point2.getCenterX());
                midLine.setStartY(point2.getCenterY());
                midLine.setEndX(point2.getCenterX());
                midLine.setEndY(point2.getCenterY());
            } else {
                valueBox.setLayoutX(point1.getCenterX() + 30);
                valueBox.setLayoutY(point1.getCenterY());
                point1.bindValueBox(valueBox,30,0);
                midLine.setStartX(point1.getCenterX());
                midLine.setStartY(point1.getCenterY());
                midLine.setEndX(point1.getCenterX());
                midLine.setEndY(point1.getCenterY());
            }

        }
        if (counter == 3) {
            line2 = new Line();
            setLine(line2, cursorX, cursorY);

            point3 = new CustomPoint(cursorX, cursorY, 2, Color.SKYBLUE, geomGroup);
            setPointEvents(point3);
        }
        counter++;

    }

    /**
     * Metoda licząca kąt między skrajnymi punktami, z wykorzystaniem Math.atan2
     *
     * @return
     */

    public double calculateAngle(CustomPoint pointA, CustomPoint pointB, int option) {

        double a = Math.sqrt(Math.pow(midLine.getEndX() - pointB.getCenterX(), 2) + Math.pow(midLine.getEndY() - pointB.getCenterY(), 2));
        double b = Math.sqrt(Math.pow(pointA.getCenterX() - pointB.getCenterX(), 2) + Math.pow(pointA.getCenterY() - pointB.getCenterY(), 2));
        double c = Math.sqrt(Math.pow(pointA.getCenterX() - midLine.getEndX(), 2) + Math.pow(pointA.getCenterY() - midLine.getEndY(), 2));
        double cosangle = (Math.pow(a, 2) + Math.pow(b, 2) - Math.pow(c, 2)) / (2 * a * b);
        double angle = Math.toDegrees(Math.acos(cosangle));
        if (angle > 90) {
            double deltaX, deltaY;

                deltaX = line2.getEndX() - line2.getStartX();
                deltaY = line2.getEndY() - line2.getStartY();
                if (option == 1) {
                    setMidLine(pointA, deltaX, deltaY);
                    angle = calculateAngle(pointB, pointA, 2);
                }
                if (option == 2) {
                    setMidLine(pointB, deltaX, deltaY);
                    angle = calculateAngle(pointA, pointB, 1);
                }
            }


        return Math.round(angle * 100.0) / 100.0;
    }

    private void setMidLine(CustomPoint pointA, double deltaX, double deltaY) {
        midLine.setStartX(pointA.getCenterX());
        midLine.setStartY(pointA.getCenterY());
        midLine.setEndX(pointA.getCenterX());
        midLine.setEndY(pointA.getCenterY());
        midLine.setEndX(midLine.getEndX() - deltaX);
        midLine.setEndY(midLine.getEndY() - deltaY);
    }

    public void drawLine(double cursorX, double cursorY) {
        if (counter == 2) {
            line1.setEndX(cursorX);
            line1.setEndY(cursorY);
        }
        if (counter == 4) {
            line2.setEndX(cursorX);
            line2.setEndY(cursorY);
            double deltaX = line2.getEndX() - line2.getStartX();
            double deltaY = line2.getEndY() - line2.getStartY();
            setMidLine(point2, point1);
            midLine.setEndX(midLine.getEndX() - deltaX);
            midLine.setEndY(midLine.getEndY() - deltaY);
            double roundedAngle;
            if (point2.getCenterX() > point1.getCenterX()) {
                roundedAngle = calculateAngle(point1, point2, 1);
            } else {
                roundedAngle = calculateAngle(point2, point1, 1);
            }
            valueBox.setVal(roundedAngle + "\u00B0");
        }

    }

    private void setMidLine(CustomPoint point1, CustomPoint point2) {
        if (point2.getCenterX() > point1.getCenterX()) {
            midLine.setStartX(point2.getCenterX());
            midLine.setStartY(point2.getCenterY());
            midLine.setEndX(point2.getCenterX());
            midLine.setEndY(point2.getCenterY());
        } else {
            midLine.setStartX(point1.getCenterX());
            midLine.setStartY(point1.getCenterY());
            midLine.setEndX(point1.getCenterX());
            midLine.setEndY(point1.getCenterY());
        }


    }

    public void setPointEvents(CustomPoint point) {
        point.setOnMouseDragged(mouseEvent -> {
            if (drawn) {
                Line line;
                double deltaX = mouseEvent.getX() - cursorXPosition;
                double deltaY = mouseEvent.getY() - cursorYPosition;
                point.setCenterX(point.getCenterX() + deltaX);
                point.setCenterY(point.getCenterY() + deltaY);
                if (point == point1 || point == point2) {
                    line = line1;
                } else {
                    line = line2;
                }
                if (point == point1 || point == point3) {
                    line.setStartX(line.getStartX() + deltaX);
                    line.setStartY(line.getStartY() + deltaY);
                    if (point == point3) {
                        midLine.setEndX(midLine.getEndX() + deltaX);
                        midLine.setEndY(midLine.getEndY() + deltaY);
                        double roundedAngle;
                        if (midLine.getStartX() == point2.getCenterX()) {
                            roundedAngle = calculateAngle(point1, point2, 1);
                        } else {
                            roundedAngle = calculateAngle(point2, point1, 2);
                        }
                        valueBox.setVal(roundedAngle + "\u00B0");
                    }
                } else {
                    line.setEndX(line.getEndX() + deltaX);
                    line.setEndY(line.getEndY() + deltaY);

                    if (point == point4) {
                        midLine.setEndX(midLine.getEndX() - deltaX);
                        midLine.setEndY(midLine.getEndY() - deltaY);
                        double roundedAngle;
                        if (midLine.getStartX() == point2.getCenterX()) {
                            roundedAngle = calculateAngle(point1, point2, 1);
                        } else {
                            roundedAngle = calculateAngle(point2, point1, 2);
                        }
                        valueBox.setVal(roundedAngle + "\u00B0");
                    }

                }


                cursorXPosition = mouseEvent.getX();
                cursorYPosition = mouseEvent.getY();
                mouseEvent.consume();
            }
        });

        point.setOnMousePressed(mouseEvent -> {
            cursorXPosition = mouseEvent.getX();
            cursorYPosition = mouseEvent.getY();
            mouseEvent.consume();

        });
    }

    public void setLineEvents(Line line) {
        line.setOnMouseEntered(mouseEvent -> {
            if(!marked) {
                line1.setStroke(Color.ORANGE);
                line1.setStrokeWidth(2);
                if (line2 != null) {
                    line2.setStroke(Color.ORANGE);
                    line2.setStrokeWidth(2);
                }
                if (valueBox != null) {
                    valueBox.setTextFill(Color.ORANGE);
                }
            }
        });
        line.setOnMouseExited(mouseEvent -> {
            if(!marked) {
                line1.setStroke(Color.SKYBLUE);
                line1.setStrokeWidth(1);
                if (line2 != null) {
                    line2.setStroke(Color.SKYBLUE);
                    line2.setStrokeWidth(1);
                }
                if (valueBox != null) {
                    valueBox.setTextFill(Color.SKYBLUE);
                }
            }
        });

        line.setOnMousePressed(mouseEvent -> {
            if (drawn) {
                GUICreator.getInstance().unselectSelectedShape();
                marked = GUICreator.getInstance().setSelectedShape(this);
                cursorXPosition = mouseEvent.getX();
                cursorYPosition = mouseEvent.getY();
                mouseEvent.consume();
            }
        });

        line.setOnMouseDragged(mouseEvent -> {
            if (drawn) {
                if(!marked) {
                    line1.setStroke(Color.ORANGE);
                    line2.setStroke(Color.ORANGE);
                    valueBox.setTextFill(Color.ORANGE);
                }
                double deltaX = mouseEvent.getX() - cursorXPosition;
                double deltaY = mouseEvent.getY() - cursorYPosition;
                line.setStartX(line.getStartX() + deltaX);
                line.setStartY(line.getStartY() + deltaY);
                line.setEndX(line.getEndX() + deltaX);
                line.setEndY(line.getEndY() + deltaY);
                if (line == line1) {
                    point1.setCenterX(point1.getCenterX() + deltaX);
                    point1.setCenterY(point1.getCenterY() + deltaY);
                    point2.setCenterX(point2.getCenterX() + deltaX);
                    point2.setCenterY(point2.getCenterY() + deltaY);
                    midLine.setStartX(midLine.getStartX() + deltaX);
                    midLine.setStartY(midLine.getStartY() + deltaY);
                    midLine.setEndX(midLine.getEndX() + deltaX);
                    midLine.setEndY(midLine.getEndY() + deltaY);
                }
                if (line == line2) {
                    point3.setCenterX(point3.getCenterX() + deltaX);
                    point3.setCenterY(point3.getCenterY() + deltaY);
                    point4.setCenterX(point4.getCenterX() + deltaX);
                    point4.setCenterY(point4.getCenterY() + deltaY);

                }
                cursorXPosition = mouseEvent.getX();
                cursorYPosition = mouseEvent.getY();

            }
            mouseEvent.consume();
        });
    }

    public boolean isDrawn() {
        return drawn;
    }

}
