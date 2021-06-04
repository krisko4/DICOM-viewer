package sample;

import de.jensd.fx.glyphs.*;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import javafx.animation.FadeTransition;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.*;


/**
 * Klasa tworząca interfejs graficzny aplikacji
 */
public class GUICreator {


    private Shape selectedShape;
    private CobbAngle cobbAngle;
    private Label warningLabel;
    private MediaView mediaView;
    private Label negativeMenuLabel;
    private boolean negative = false;
    private MenuItem patientInfoMenuItem;
    private boolean zoomSelected = false, windowModificationSelected = false, rotationSelected = false;
    private Text zoomContainer, windowModificationContainer, rotateContainer, lineContainer, polylineContainer, polygonContainer, elipseContainer, angleContainer, cobbAngleContainer;
    private GlyphIcons zoomIcon, windowModificationIcon, rotateIcon, lineIcon, polylineIcon, polygonIcon, elipseIcon, angleIcon, cobbAngleIcon;
    private Label infoLabel;
    private CustomEllipse customEllipse;
    private CustomLine customLine;
    private Angle angle;
    private Label informationLabel;
    private TextField valueField;
    private Stage popUpStage;
    private int pixelXPos, pixelYPos;
    private double initialXPos, initialYPos;
    private final Group geomGroup;
    private Pane toolbox;
    private Circle circle;
    final ToggleGroup MaxValGroup = new ToggleGroup();
    final ToggleGroup PaletteGroup = new ToggleGroup();
    private static GUICreator guiCreator;
    private int maxValue;
    private MenuBar menuBar;
    private Menu fileMenu, zoomMenu, negativeMenu, maxValueMenu, rotationMenu, measureMenu, paletteMenu, layoutMenu, patientInfoMenu, histogramMenu;
    private MenuItem loadFileMenuItem, rotation1, rotation2, rotation3, customRotation, defaultRotation, clearFiguresMenuItem, clearSelectedFigureMenuItem, zoom1, zoom2, zoom3, zoom4, zoom5, layout1, layout2, layout3, layout4;
    private RadioMenuItem percent1, percent2, percent3, percent4, percent5, percent6, percent7, percent8, customPercent, defaultWCWW;
    private RadioMenuItem palette1MenuItem, palette2MenuItem, palette3MenuItem, palette4MenuItem, palette5MenuItem, palette6MenuItem, palette7MenuItem, palette8MenuItem;
    private MenuItem lineMenuItem, polygonMenuItem, polylineMenuItem, angleMenuItem, cobbAngleMenuItem, elipseMenuItem, bin1, bin2, bin3, bin4, customBin;
    private Stage primaryStage;
    private ScrollPane scrollPane;
    private double cursorXPosition, cursorYPosition;
    private Pane pane, root, pane1, imagePane, popUpPane;
    private CustomPolygon customPolygon;
    private Button cancelButton, submitButton;
    private Label nameLabel, dateLabel, modalityLabel, xPosition, yPosition, pixelValue, maxVal, windowWidth, windowCenter;
    private ImageView imageView;
    private File selectedFile;
    private Scene scene;
    private TreeItem<String> seriesInstance, file;
    private TreeView<String> treeView;
    private FileChooser fileChooser;
    private TreeItem<String> rootItem;
    private Map<MenuItem, List<Object>> checkMap;

    private GUICreator() {
        createPanes();
        setPane(1000, 590, 200, 34, pane1);
        setPane(200, 590, 0, 34, pane);
        createMenu();
        setTreeView();
        setImageView();
        createCursorCoordinates();
        setPatientInformationFields();
        createPixelValueLabel();
        createMaxVallabel();
        createWindowParameters();
        scrollPane.setContent(treeView);
        setQuickToolkit();
        geomGroup = new Group();
        imagePane.getChildren().add(geomGroup);
        createWarningLabel();


    }


    public boolean setSelectedShape(Shape shape) {
        selectedShape = shape;
        if (selectedShape instanceof CobbAngle) {
            ((CobbAngle) selectedShape).setSelected();
            return true;
        }
        selectedShape.setStroke(Color.VIOLET);

        return true;
    }

    public void unselectSelectedShape() {
        if (selectedShape != null) {
            if (selectedShape instanceof CustomLine) {
                ((CustomLine) selectedShape).unmark();
            }
            if (selectedShape instanceof Angle) {
                ((Angle) selectedShape).unmark();
            }
            if (selectedShape instanceof CobbAngle) {
                ((CobbAngle) selectedShape).unmark();
            }
            if (selectedShape instanceof CustomEllipse) {
                ((CustomEllipse) selectedShape).unmark();
            }
            if (selectedShape instanceof CustomPolygon) {
                ((CustomPolygon) selectedShape).unmark();
            }
        }

    }

    public void createWarningLabel() {
        warningLabel = new Label("CAUTION! The area of self-intersecting polygons will not be calculated properly!");
        root.getChildren().add(warningLabel);
        warningLabel.setLayoutY(50);
        warningLabel.setPrefWidth(500);
        warningLabel.setLayoutX(200 + (pane1.getPrefWidth() - warningLabel.getPrefWidth()) / 2);
        warningLabel.setId("warningLabel");
        warningLabel.setVisible(false);
    }

    public void showWarningLabel() {
        warningLabel.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(800), warningLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

    }

    public void hideWarningLabel() {
        warningLabel.setVisible(false);
    }


    public void hideCursorPositions() {
        xPosition.setVisible(false);
        yPosition.setVisible(false);
    }

    public void setNegative(boolean state) {
        negative = state;
    }

    public boolean isNegative() {
        return negative;
    }


    /**
     * Metoda nasłuchująca naciśniętego elementu z podręcznego Toolboxa oraz wywołująca adekwatną funkcję dla tego elementu
     *
     * @param iconContainers
     */
    public void setContainerEvents(Text... iconContainers) {
        for (Text iconContainer : iconContainers) {
            iconContainer.setOnMousePressed(mouseEvent -> {
                clearAllMenuItems();
                if (iconContainer == zoomContainer) {
                    zoomSelected = true;
                    rotationSelected = false;
                    windowModificationSelected = false;
                } else if (iconContainer == windowModificationContainer) {
                    windowModificationSelected = true;
                    rotationSelected = false;
                    zoomSelected = false;
                } else if (iconContainer == rotateContainer) {
                    windowModificationSelected = false;
                    rotationSelected = true;
                    zoomSelected = false;
                } else {
                    for (List<Object> value : checkMap.values()) {
                        Text text = (Text) value.get(1);
                        if (text.getText().equals(iconContainer.getText())) {
                            for (MenuItem key : checkMap.keySet()) {
                                if (checkMap.get(key) == value) {
                                    zoomSelected = false;
                                    windowModificationSelected = false;
                                    rotationSelected = false;
                                    setSelected(key, true);
                                    key.getGraphic().setId("menu-item-icon-checked");
                                    pane1.setCursor(Cursor.CROSSHAIR);

                                }
                            }
                        }
                    }
                }
                fadeOut(toolbox);
                mouseEvent.consume();
            });
        }
    }


    /**
     * Metoda tworząca podręczny Toolkit do wygodnej zmiany funkcji w przeglądarce pod przyciskiem MiddleButton
     */

    public void setQuickToolkit() {
        toolbox = new Pane();
        circle = new Circle();
        toolbox.getChildren().add(circle);
        root.getChildren().add(toolbox);
        circle.setRadius(150);
        circle.setStroke(Color.BLACK);
        circle.setFill(Color.rgb(32, 32, 32, 0.7));
        toolbox.setVisible(false);
        zoomContainer = setIcon(zoomIcon, toolbox);
        windowModificationContainer = setIcon(windowModificationIcon, toolbox);
        rotateContainer = setIcon(rotateIcon, toolbox);
        lineContainer = setIcon(lineIcon, toolbox);
        elipseContainer = setIcon(elipseIcon, toolbox);
        polylineContainer = setIcon(polylineIcon, toolbox);
        polygonContainer = setIcon(polygonIcon, toolbox);
        angleContainer = setIcon(angleIcon, toolbox);
        cobbAngleContainer = setIcon(cobbAngleIcon, toolbox);
        setContainerEvents(zoomContainer, windowModificationContainer, rotateContainer, lineContainer, elipseContainer, polylineContainer, polygonContainer, angleContainer, cobbAngleContainer);

    }

    /**
     * Metoda wyświetlająca podręczny Toolkit
     *
     * @param cursorX
     * @param cursorY
     */
    public void showQuickToolkit(double cursorX, double cursorY) {
        toolbox.setLayoutX(cursorX - circle.getRadius());
        toolbox.setLayoutY(cursorY - circle.getRadius());
        circle.setCenterX(toolbox.getWidth() / 2);
        circle.setCenterY(toolbox.getHeight() / 2);
        double littleRadius = 100;
        windowModificationContainer.setLayoutX(circle.getCenterX() - 10);
        windowModificationContainer.setLayoutY(circle.getCenterY());
        zoomContainer.setLayoutX(circle.getCenterX() + littleRadius - 10);
        zoomContainer.setLayoutY(circle.getCenterY());
        cobbAngleContainer.setLayoutX(circle.getCenterX() - littleRadius - 10);
        cobbAngleContainer.setLayoutY(circle.getCenterY());
        rotateContainer.setLayoutX(circle.getCenterX() - 10);
        rotateContainer.setLayoutY(circle.getCenterY() - littleRadius);
        lineContainer.setLayoutX(circle.getCenterX() - 10);
        lineContainer.setLayoutY(circle.getCenterY() + littleRadius);
        elipseContainer.setLayoutX(circle.getCenterX() + 70 - 10);
        elipseContainer.setLayoutY(circle.getCenterY() - 70);
        polylineContainer.setLayoutX(circle.getCenterX() + 70 - 10);
        polylineContainer.setLayoutY(circle.getCenterY() + 70);
        polygonContainer.setLayoutX(circle.getCenterX() - 70 - 10);
        polygonContainer.setLayoutY(circle.getCenterY() + 70);
        angleContainer.setLayoutX(circle.getCenterX() - 70 - 10);
        angleContainer.setLayoutY(circle.getCenterY() - 70);
        toolbox.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(200), toolbox);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();


    }


    /**
     * Metoda resetująca skalę oraz przywracająca pozycję środkową obrazu na ekranie
     *
     * @param controller
     */
    private void resetScale(Controller controller) {
        //środkowanie obrazu
        pane1.setScaleX(1);
        pane1.setScaleY(1);
        imagePane.setLayoutX((pane1.getPrefWidth() - imagePane.getPrefWidth()) / 2);
        imagePane.setLayoutY((pane1.getPrefHeight() - imagePane.getPrefHeight()) / 2);
        geomGroup.getChildren().clear();
        controller.rotate(imagePane, 0, false);
    }


    private double getImageWidth() {
        return imageView.getImage().getWidth();
    }

    public double getImageHeight() {
        return imageView.getImage().getHeight();
    }

    public static GUICreator getInstance() {
        if (guiCreator == null) {
            guiCreator = new GUICreator();
        }
        return guiCreator;
    }

    public void getCursorPosition(MouseEvent mouseEvent) {
        cursorYPosition = mouseEvent.getY();
        cursorXPosition = mouseEvent.getX();
    }

    public void lockWindow()
    {
        primaryStage.setResizable(false);
    }

    public void unlockWindow(){
        primaryStage.setResizable(true);
    }

    /**
     * Metoda tworząca menu MaxValue
     */
    public void setMaxValueMenu() {
        maxValueMenu.setText("MaxVal");
        percent1.setText("0.5%");
        percent2.setText("1%");
        percent3.setText("2.5%");
        percent4.setText("5%");
        percent5.setText("10%");
        percent6.setText("25%");
        percent7.setText("50%");
        percent8.setText("100%");
        customPercent.setText("Custom");
        defaultWCWW.setVisible(false);
    }

    /**
     * Metoda tworząca menu WC/WW
     */
    public void setWCWWMenu() {
        maxValueMenu.setText("WC/WW");
        percent1.setText("20/40");
        percent2.setText("40/80");
        percent3.setText("80/160");
        percent4.setText("160/320");
        percent5.setText("320/640");
        percent6.setText("640/1280");
        percent7.setText("1280/2560");
        percent8.setText("2560/5120");
        customPercent.setText("Custom");
        defaultWCWW.setVisible(true);
    }

    /**
     * Metoda wyświetlająca koordynaty kursora
     *
     * @param x
     * @param y
     */
    public void showCursorPosition(double x, double y) {
        xPosition.setVisible(true);
        yPosition.setVisible(true);
        xPosition.setText("X:" + x);
        yPosition.setText("Y:" + y);
    }

    /**
     * Metoda wyświetlająca wartości piksela
     *
     * @param val
     */
    public void showPixelValue(double val) {
        pixelValue.setVisible(true);
        pixelValue.setText("Val:" + val);
    }

    /**
     * Metoda wyświetlająca parametry okna
     *
     * @param windowWidthVal
     * @param windowCenterVal
     */
    private void showWindowParameters(double windowWidthVal, double windowCenterVal) {
        windowCenter.setVisible(true);
        windowWidth.setVisible(true);
        windowCenter.setText("WC: " + windowCenterVal);
        windowWidth.setText("WW: " + windowWidthVal);
    }

    /**
     * Metoda wyświetlająca maksymalną wartość piksela dla danego pliku
     *
     * @param val
     */
    public void showMaxVal(int val) {

        maxVal.setVisible(true);
        maxVal.setText("MaxVal: " + val);


    }

    public void hidePixelValue() {
        pixelValue.setVisible(false);
    }


    /**
     * Metoda tworząca informacyjne napisy o pacjencie
     */
    public void setPatientInformationFields() {
        nameLabel = new Label();
        dateLabel = new Label();
        modalityLabel = new Label();
        root.getChildren().add(nameLabel);
        root.getChildren().add(dateLabel);
        root.getChildren().add(modalityLabel);

        nameLabel.setLayoutX(215);
        dateLabel.setLayoutX(215);
        modalityLabel.setLayoutX(215);
        nameLabel.setLayoutY(40);
        dateLabel.setLayoutY(55);
        modalityLabel.setLayoutY(70);
        nameLabel.setTextFill(Color.WHITE);
        dateLabel.setTextFill(Color.WHITE);
        modalityLabel.setTextFill(Color.WHITE);




    }

    /**
     * Metoda ukrywająca informacje o pacjencie
     */
    public void clearPatientInformation() {
        nameLabel.setVisible(false);
        dateLabel.setVisible(false);
        modalityLabel.setVisible(false);
    }

    /**
     * Metoda wyświetlająca informacje o pacjencie
     */
    public void showPatientInformation() {
        nameLabel.setVisible(true);
        dateLabel.setVisible(true);
        modalityLabel.setVisible(true);
    }

    /**
     * Metoda ukrywająca koordynaty kursora
     */
    public void clearCursorCoordinates() {
        xPosition.setVisible(false);
        yPosition.setVisible(false);
    }

    public void setDateLabel(String date) {
        dateLabel.setText(date);
    }

    public void setModalityLabel(String modality) {
        modalityLabel.setText(modality);
    }

    public void setNameLabel(String name) {
        nameLabel.setText(name);
    }

    public void setFile(String filex) {
        file = new TreeItem<>();
        file.setValue(filex);
    }


    /**
     * Metoda otwierająca okno wyboru plików
     *
     * @return
     */
    public List<File> openFileChooser() {
        return fileChooser.showOpenMultipleDialog(primaryStage);
    }


    public TreeItem<String> getSeriesInstance() {
        return seriesInstance;
    }

    public void setSeriesInstance(String x) {
        seriesInstance = new TreeItem<>();
        seriesInstance.setValue(x);


    }


    public void setViewport(Rectangle2D viewport) {
        imageView.setViewport(viewport);
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
    }

    public Image getImage() {
        return imageView.getImage();
    }


    /**
     * Metoda tworząca drzewo wyboru
     */

    public void setTreeView() {
        treeView = new TreeView<>();
        treeView.getStyleClass().add("treeView");
        rootItem = new TreeItem<>("DICOM");
        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);
        treeView.setPrefHeight(pane.getPrefHeight());
        treeView.setPrefWidth(200);
        pane.getChildren().add(treeView);
        treeView.setVisible(false);


    }

    public TreeItem<String> getTreeRoot() {
        return treeView.getRoot();
    }

    public void setTreeRoot(TreeItem<String> root) {

        treeView.setRoot(root);
    }


    public void setImage(Image image) {
        imageView.setImage(image);

    }


    /**
     * Metoda tworząca kontener do wyświetlania obrazu
     */
    public void setImageView() {

        imageView = new ImageView();
        imagePane.getChildren().add(imageView);
        imageView.setFitWidth(imagePane.getPrefWidth());
        imageView.setFitHeight(imagePane.getPrefHeight());
        imageView.setPreserveRatio(true);
        imageView.setCache(true);
        imageView.setSmooth(true);


    }

    /**
     * Metoda ukrywająca obraz oraz wszystkie napisy na ekranie
     */
    public void clearImageView() {
        imageView.setVisible(false);
        clearPatientInformation();
        clearMaxVal();
        clearWindowParameters();
        maxVal.setVisible(false);
        windowCenter.setVisible(false);
        windowWidth.setVisible(false);
        geomGroup.getChildren().clear();

    }

    public void showImageView() {
        imageView.setVisible(true);
    }


    /**
     * Metoda dopasowująca pozycję ImageView
     */

    public void adjustImageView() {
        imagePane.setPrefWidth(imagePane.getPrefHeight() * getScaleFactor());
        imagePane.setLayoutX((pane1.getPrefWidth() - imagePane.getPrefWidth()) / 2);
        imageView.setFitWidth(imagePane.getPrefWidth());


    }

    /**
     * Metoda tworząca scenę
     *
     * @param stage
     */
    public void createScene(Stage stage) {

        scene = new Scene(root, 1200, 625);
        scene.getStylesheets().add("stylesheet.css");
        primaryStage = stage;
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setTitle("DICOM Browser");
        primaryStage.setMinWidth(scene.getWidth());
        primaryStage.setMinHeight(primaryStage.getHeight());
        menuBar.prefWidthProperty().bind(scene.widthProperty());
        adjustCoordinatesToPane();
    }


    public Pane getPane() {
        return pane1;
    }


    /**
     * Metoda tworząca napis na przechowywanie chwilowej wartości piksela
     */
    public void createPixelValueLabel() {
        pixelValue = new Label();
        pixelValue.setTextFill(Color.WHITE);
        pixelValue.setLayoutX(220);
        pixelValue.setLayoutY(625 - 70);
        pixelValue.setVisible(false);
        root.getChildren().add(pixelValue);
    }


    public void enableTreeView() {
        treeView.setDisable(false);
    }

    public void disableTreeView() {
        treeView.setDisable(true);
    }


    public void disableHistogramMenu() {
        histogramMenu.setDisable(true);
    }

    public void enableHistogramMenu() {
        histogramMenu.setDisable(false);
    }

    /**
     * Metoda tworząca napisy przechowujące parametry okna
     */
    public void createWindowParameters() {
        windowCenter = new Label();
        windowWidth = new Label();
        windowCenter.setTextFill(Color.WHITE);
        windowWidth.setTextFill(Color.WHITE);
        windowCenter.setLayoutX(220);
        windowWidth.setLayoutX(220);
        windowCenter.setLayoutY(625 - 55);
        windowWidth.setLayoutY(625 - 40);
        root.getChildren().add(windowCenter);
        root.getChildren().add(windowWidth);
        windowWidth.setVisible(false);
        windowCenter.setVisible(false);

    }

    /**
     * Metoda tworząca napis przechowujący maksymalną wartość piksela
     */
    public void createMaxVallabel() {
        maxVal = new Label();
        maxVal.setTextFill(Color.WHITE);
        maxVal.setLayoutX(220);
        maxVal.setLayoutY(625 - 55);
        maxVal.setVisible(false);
        root.getChildren().add(maxVal);

    }

    /**
     * Metoda tworząca napisy przechowujące koordynaty kursora
     */
    public void createCursorCoordinates() {
        xPosition = new Label();
        xPosition.setTextFill(Color.WHITE);
        yPosition = new Label();
        yPosition.setTextFill(Color.WHITE);
        xPosition.setLayoutX(550);
        xPosition.setLayoutY(45);
        yPosition.setLayoutX(550);
        yPosition.setLayoutY(55);
        root.getChildren().add(xPosition);
        root.getChildren().add(yPosition);
        xPosition.setVisible(false);
        yPosition.setVisible(false);


    }

    /**
     * Metoda tworząca plansze
     */
    public void createPanes() {
        root = new Pane();
        root.setStyle("-fx-background-color: black");
        final File file = new File("background.mp4");
        final String MEDIA_URL = file.toURI().toString();
        final Media media = new Media(MEDIA_URL);
        final MediaPlayer player = new MediaPlayer(media);
        mediaView = new MediaView(player);
        player.setAutoPlay(true);
        root.getChildren().add(mediaView);
        // root.setStyle("-fx-background-color: black");
        pane = new Pane();
        pane1 = new Pane();
        imagePane = new Pane();
        imagePane.setPrefHeight(590);
        imagePane.setStyle("-fx-background-color: black");
        pane1.setStyle("-fx-background-color: black");
        pane1.getChildren().add(imagePane);
        scrollPane = new ScrollPane();


    }


    /**
     * Metoda rozmieszczająca plansze
     *
     * @param width
     * @param height
     * @param layoutX
     * @param layoutY
     * @param pane
     */
    public void setPane(int width, int height, int layoutX, int layoutY, Pane pane) {
        pane.setPrefWidth(width);
        pane.setPrefHeight(height);
        pane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        pane.setLayoutX(layoutX);
        pane.setLayoutY(layoutY);
        root.getChildren().add(pane);
        pane.setVisible(false);


    }

    /**
     * Metoda rozmieszczająca napis MaxVal oraz WC,WW
     *
     * @param layoutY
     */
    public void adjustMaxVal(double layoutY) {
        maxVal.setLayoutY(layoutY);
        windowCenter.setLayoutY(root.getHeight() - 55);
        windowWidth.setLayoutY(root.getHeight() - 40);

    }

    /**
     * Metoda sprawdzająca czy dany element menu kontekstowego jest zaznaczony
     *
     * @param menuItem
     * @return
     */
    public boolean isSelected(MenuItem menuItem) {
        return (boolean) checkMap.get(menuItem).get(0);
    }

    /**
     * Metoda zaznaczająca/odznaczająca element menu kontekstowego
     *
     * @param menuItem
     * @param state
     */
    public void setSelected(MenuItem menuItem, boolean state) {
        List<Object> list = checkMap.get(menuItem);
        list.set(0, state);
        checkMap.replace(menuItem, list);
    }

    /**
     * Metoda tworząca ikony
     */

    public void createIcons() {

        rotateIcon = FontAwesomeIcons.ROTATE_LEFT;
        windowModificationIcon = FontAwesomeIcons.ADJUST;
        zoomIcon = FontAwesomeIcons.SEARCH_PLUS;
        lineIcon = FontAwesomeIcons.LINE_CHART;
        elipseIcon = FontAwesomeIcons.CIRCLE_ALT;
        polygonIcon = FontAwesomeIcons.SQUARE_ALT;
        polylineIcon = FontAwesomeIcons.PENCIL_SQUARE_ALT;
        angleIcon = FontAwesomeIcons.ANGLE_DOUBLE_RIGHT;
        cobbAngleIcon = FontAwesomeIcons.ANGLE_RIGHT;

    }

    /**
     * Metoda przypisująca ikonę elementowi toolkita
     *
     * @param icon
     * @param toolbox
     * @return
     */
    public Text setIcon(GlyphIcons icon, Pane toolbox) {

        Text iconContainer = GlyphsDude.createIcon(icon, "20");

        iconContainer.setId("toolbox-icon");
        toolbox.getChildren().add(iconContainer);
        return iconContainer;

    }

    /**
     * Metoda przypisująca ikonę danemu Menu
     *
     * @param data
     * @param menu
     */
    public void setIcon(GlyphIcons data, Menu menu) {
        Text icon = GlyphsDude.createIcon(data, "17");
        icon.setId("menu-icon");
        menu.setGraphic(icon);
    }


    /**
     * Metoda przypisująca ikonę danemu MenuItem
     *
     * @param data
     * @param menuItem
     */
    public void setIcon(GlyphIcons data, MenuItem menuItem) {
        Text icon = GlyphsDude.createIcon(data, "17");
        icon.setId("menu-item-icon");
        menuItem.setGraphic(icon);
        List<Object> iconList = new ArrayList<>();
        iconList.add(false);
        iconList.add(icon);
        checkMap.put(menuItem, iconList);
    }

    /**
     * Metoda odznaczająca wszystkie elementy ContextMenu
     */
    public void clearAllMenuItems() {

        for (MenuItem menuItem : checkMap.keySet()) {
            List<Object> list = checkMap.get(menuItem);
            list.set(0, false);
            checkMap.replace(menuItem, list);
            menuItem.getGraphic().setId("menu-item-icon");
        }
        hideWarningLabel();
        pane1.setCursor(Cursor.DEFAULT);
    }

    /**
     * Metoda wyświetlająca Menu
     */
    public void showMenus() {

        zoomMenu.setVisible(true);
        negativeMenu.setVisible(true);
        maxValueMenu.setVisible(true);
        measureMenu.setVisible(true);
        paletteMenu.setVisible(true);
        patientInfoMenu.setVisible(true);
        rotationMenu.setVisible(true);
        histogramMenu.setVisible(true);
        layoutMenu.setVisible(true);
    }

    /**
     * Metoda chowająca Menu
     */
    public void clearMenus() {
        maxValueMenu.setVisible(false);
        negativeMenu.setVisible(false);
        rotationMenu.setVisible(false);
        measureMenu.setVisible(false);
        paletteMenu.setVisible(false);
        patientInfoMenu.setVisible(false);
        histogramMenu.setVisible(false);
        zoomMenu.setVisible(false);
        layoutMenu.setVisible(false);
    }

    /**
     * Metoda zaznaczająca dany menuItem z menu kontekstowego i odznaczająca wszystkie pozostałe elementy
     *
     * @param menuItems
     */
    public void onAction(MenuItem... menuItems) {
        for (MenuItem item : menuItems) {
            item.setOnAction(actionEvent -> {
                //jeśli jest wybrany, odznacz
                if (isSelected(item)) {
                    setSelected(item, false);
                    item.getGraphic().setId("menu-item-icon");
                    //zmiana kursora na defaultowy
                    pane1.setCursor(Cursor.DEFAULT);
                    return;
                }
                // jeśli nie jest wybrany, wyczyść wszystkie
                // przeszukaj mapę i ustaw wszystkie elementy na odznaczone
                clearAllMenuItems();
                rotationSelected = false;
                zoomSelected = false;
                windowModificationSelected = false;
                setSelected(item, true);
                //zmiana kursora na krzyżyk
                pane1.setCursor(Cursor.CROSSHAIR);
                item.getGraphic().setId("menu-item-icon-checked");

            });
        }

    }

    /**
     * Metoda tworząca menuBar
     */
    public void createMenu() {


        menuBar = new MenuBar();
        menuBar.getStyleClass().add("menuBar");
        menuBar.setPrefWidth(800);
        menuBar.setPrefHeight(25);
        fileMenu = new Menu("File");
        patientInfoMenu = new Menu("Patient info");
        //   patientInfoMenu.setVisible(false);
        createIcons();
        fileMenu.getStyleClass().add("fileMenu");
        setIcon(FontAwesomeIcons.FILE_IMAGE_ALT, fileMenu);
        zoomMenu = new Menu("Zoom");
        setIcon(FontAwesomeIcons.SEARCH_PLUS, zoomMenu);
        negativeMenuLabel = new Label("Negative");
        negativeMenu = new Menu("", negativeMenuLabel);
        Text icon = GlyphsDude.createIcon(FontAwesomeIcons.ADJUST, "17");
        icon.setId("menu-icon");
        negativeMenuLabel.setGraphic(icon);
        //setIcon(FontAwesomeIcons.ADJUST, negativeMenu);
        maxValueMenu = new Menu("Max value");
        setIcon(FontAwesomeIcons.SORT_NUMERIC_DESC, maxValueMenu);
        rotationMenu = new Menu("Rotation");
        setIcon(FontAwesomeIcons.COMPASS, rotationMenu);
        loadFileMenuItem = new MenuItem("Load");
        percent1 = new RadioMenuItem();
        percent2 = new RadioMenuItem();
        percent3 = new RadioMenuItem();
        percent4 = new RadioMenuItem();
        percent5 = new RadioMenuItem();
        percent6 = new RadioMenuItem();
        percent7 = new RadioMenuItem();
        percent8 = new RadioMenuItem();
        customPercent = new RadioMenuItem();
        defaultWCWW = new RadioMenuItem("Default");
        patientInfoMenuItem = new MenuItem();
        patientInfoMenu.getItems().add(patientInfoMenuItem);
        lineMenuItem = new MenuItem("Line");
        checkMap = new HashMap<>();
        setIcon(lineIcon, lineMenuItem);
        elipseMenuItem = new MenuItem("Elipse");
        setIcon(elipseIcon, elipseMenuItem);
        polygonMenuItem = new MenuItem("Polygon");
        setIcon(polygonIcon, polygonMenuItem);
        polylineMenuItem = new MenuItem("Polyline");
        setIcon(polylineIcon, polylineMenuItem);
        angleMenuItem = new MenuItem("Angle");
        setIcon(angleIcon, angleMenuItem);
        cobbAngleMenuItem = new MenuItem("Cobb Angle");
        setIcon(cobbAngleIcon, cobbAngleMenuItem);
        clearFiguresMenuItem = new MenuItem("Clear all");
        clearSelectedFigureMenuItem = new MenuItem("Clear selected");
        setIcon(FontAwesomeIcons.TIMES, clearFiguresMenuItem);
        setIcon(FontAwesomeIcons.TRASH, clearSelectedFigureMenuItem);
        histogramMenu = new Menu("Histogram");
        bin1 = new MenuItem("2 bins");
        bin2 = new MenuItem("4 bins");
        bin3 = new MenuItem("8 bins");
        customBin = new MenuItem("Custom");
        bin4 = new MenuItem("Full histogram");
        histogramMenu.getItems().addAll(bin1, bin2, bin3, customBin, bin4);
        setIcon(FontAwesomeIcons.BAR_CHART, histogramMenu);
        onAction(lineMenuItem, elipseMenuItem, polygonMenuItem, polylineMenuItem, angleMenuItem, cobbAngleMenuItem);
        zoom1 = new MenuItem("100%");
        zoom2 = new MenuItem("200%");
        zoom3 = new MenuItem("400%");
        zoom4 = new MenuItem("800%");
        zoom5 = new MenuItem("Full window");
        zoomMenu.getItems().addAll(zoom1, zoom2, zoom3, zoom4, zoom5);
        rotation1 = new MenuItem("90 degrees");
        rotation2 = new MenuItem("180 degrees");
        rotation3 = new MenuItem("270 degrees");
        customRotation = new MenuItem("Custom");
        defaultRotation = new MenuItem("Default");
        //  rotationMenu.setVisible(false);
        fileMenu.getItems().add(loadFileMenuItem);
        //   negativeMenu.setVisible(false);
        maxValueMenu.getItems().addAll(percent1, percent2, percent3, percent4, percent5, percent6, percent7, percent8, customPercent, defaultWCWW);
        //   defaultWCWW.setVisible(false);
        //  maxValueMenu.setVisible(false);
        setToggleGroup(MaxValGroup, percent1, percent2, percent3, percent4, percent5, percent6, percent7, percent8, customPercent, defaultWCWW);
        //  zoomMenu.setVisible(false);
        rotationMenu.getItems().addAll(rotation1, rotation2, rotation3, customRotation, defaultRotation);
        measureMenu = new Menu("Measure");
        setIcon(FontAwesomeIcons.CALCULATOR, measureMenu);
        measureMenu.getItems().addAll(lineMenuItem, polygonMenuItem, polylineMenuItem, angleMenuItem, cobbAngleMenuItem, elipseMenuItem, clearSelectedFigureMenuItem, clearFiguresMenuItem);
        //  measureMenu.setVisible(false);
        paletteMenu = new Menu("Palettes");
        setIcon(FontAwesomeIcons.PAINT_BRUSH, paletteMenu);
        palette1MenuItem = new RadioMenuItem("1");
        palette2MenuItem = new RadioMenuItem("2");
        palette3MenuItem = new RadioMenuItem("3");
        palette4MenuItem = new RadioMenuItem("4");
        palette5MenuItem = new RadioMenuItem("5");
        palette6MenuItem = new RadioMenuItem("6");
        palette7MenuItem = new RadioMenuItem("7");
        palette8MenuItem = new RadioMenuItem("8");
        paletteMenu.getItems().addAll(palette1MenuItem, palette2MenuItem, palette3MenuItem, palette4MenuItem, palette5MenuItem, palette6MenuItem, palette7MenuItem, palette8MenuItem);
        setToggleGroup(PaletteGroup, palette1MenuItem, palette2MenuItem, palette3MenuItem, palette4MenuItem, palette5MenuItem, palette6MenuItem, palette7MenuItem, palette8MenuItem);
        // paletteMenu.setVisible(false);
        layoutMenu = new Menu("Layout");
        setIcon(FontAwesomeIcons.IMAGE, layoutMenu);
        layout1 = new MenuItem("Default");
        layout2 = new MenuItem("Matrix");
        layout3 = new MenuItem("Red");
        layout4 = new MenuItem("Yellow");
        layoutMenu.getItems().addAll(layout1, layout2, layout3, layout4);
        menuBar.getMenus().addAll(fileMenu, zoomMenu, negativeMenu, maxValueMenu, rotationMenu, paletteMenu, layoutMenu, measureMenu, histogramMenu);
        root.getChildren().add(menuBar);
        FadeTransition ft = new FadeTransition(Duration.millis(1500), menuBar);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
        clearMenus();


    }


    private void setToggleGroup(ToggleGroup group, RadioMenuItem... items) {
        for (RadioMenuItem item : items) {
            item.setToggleGroup(group);
        }
    }

    /**
     * Metoda tworząca okno wyboru pliku
     */
    public void setFileChooser() {
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }


    public void showTree() {
        FadeTransition ft = new FadeTransition(Duration.millis(800), treeView);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
        treeView.setVisible(true);

    }


    public double getScaleFactor() {
        return getImageWidth() / getImageHeight();
    }


    public void adjustCoordinatesToPane() {
        xPosition.setLayoutX((root.getWidth() - 50));
        yPosition.setLayoutX((root.getWidth() - 50));

    }

    public TreeItem<String> getItem() {
        return treeView.getSelectionModel().getSelectedItem().getParent();
    }

    /**
     * Metoda tworząca okno z komunikatem o nieudanym odczycie pliku DICOM
     */
    public void setPopUpWindow() {
        Stage stage = new Stage();
        AnchorPane popUpPane = new AnchorPane();
        popUpPane.setPrefHeight(200);
        popUpPane.setPrefWidth(200);
        Label label = new Label("Failed to read DICOM file. Please choose correct file.");
        label.setLayoutY(70);
        popUpPane.getChildren().add(label);
        Scene scene = new Scene(popUpPane);
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);
    }

    /**
     * Animacja fade-out dla toolkita
     *
     * @param node
     */
    public void fadeOut(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), node);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.play();
        ft.onFinishedProperty().set(actionEvent -> node.setVisible(false));
    }

    /**
     * Metoda tworzaca okno z daną informacją
     *
     * @param informationText
     */
    public Stage createCustomWindow(String informationText) {

        popUpStage = new Stage();
        popUpStage.setTitle("Custom window");
        popUpPane = new AnchorPane();
        popUpPane.setPrefHeight(250);
        popUpPane.setPrefWidth(250);
        valueField = new javafx.scene.control.TextField();
        informationLabel = new Label(informationText);
        cancelButton = new Button("Cancel");
        submitButton = new Button("Submit");
        popUpPane.getChildren().add(submitButton);
        popUpPane.getChildren().add(informationLabel);
        popUpPane.getChildren().add(valueField);
        popUpPane.getChildren().add(cancelButton);
        Scene scene = new Scene(popUpPane);
        popUpStage.setScene(scene);
        popUpStage.initModality(Modality.APPLICATION_MODAL);
        popUpStage.show();
        informationLabel.setLayoutX(25);
        informationLabel.setLayoutY(75);
        valueField.setLayoutX(50);
        valueField.setLayoutY(105);
        submitButton.setLayoutX(125);
        submitButton.setLayoutY(145);
        cancelButton.setLayoutX(65);
        cancelButton.setLayoutY(145);

        popUpStage.setResizable(false);
        valueField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("0-9")) {
                valueField.setText(newValue.replaceAll("[^0-9]", ""));
            }
        });
        cancelButton.setOnMousePressed(mouseEvent -> popUpStage.close());
        return popUpStage;

    }

    /**
     * Metoda tworząca okno z wyborem dowolnej liczby koszyków
     *
     * @param controller
     */
    public void setBasketChooser(Controller controller) {
        createCustomWindow("Please type in the amount of bins");
        submitButton.setOnMousePressed(mouseEvent -> {
            if (!valueField.getText().isEmpty()) {
                Histogram hist = new Histogram(controller.getProperties(), Integer.parseInt(valueField.getText()));
                hist.drawWindow();
                hist.add();
            }

            popUpStage.close();

        });

    }

    /**
     * Metoda tworząca okno z wyborem dowolnej wartości WC/WW
     *
     * @param controller
     */
    public void setWCWWChooser(Controller controller) {
        createCustomWindow("Please type in window parameters");
        informationLabel.setLayoutX(25);
        informationLabel.setLayoutY(50);
        Label label1 = new Label("Window center");
        Label label2 = new Label("Window width");
        label1.setLayoutX(10);
        label1.setLayoutY(102);
        label2.setLayoutX(10);
        label2.setLayoutY(132);
        popUpPane.getChildren().add(label1);
        popUpPane.getChildren().add(label2);
        valueField.setLayoutX(100);
        valueField.setLayoutY(100);
        valueField.setPrefWidth(130);
        TextField anotherValueField = new TextField();
        anotherValueField.setLayoutX(100);
        anotherValueField.setLayoutY(130);
        anotherValueField.setPrefWidth(130);
        popUpPane.getChildren().add(anotherValueField);
        submitButton.setLayoutX(125);
        submitButton.setLayoutY(175);
        cancelButton.setLayoutX(65);
        cancelButton.setLayoutY(175);

        anotherValueField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("0-9")) {
                anotherValueField.setText(newValue.replaceAll("[^0-9]", ""));
            }
        });

        submitButton.setOnMousePressed(mouseEvent -> {
            if (valueField.getText().isEmpty() || anotherValueField.getText().isEmpty()) {
                informationLabel.setLayoutX(40);
                informationLabel.setText("Please fill in the value fields!");
                return;
            }
            controller.setWindowParameters(Double.parseDouble(valueField.getText()), Double.parseDouble(anotherValueField.getText()), false, true);
            showWindowParameters(Double.parseDouble(anotherValueField.getText()), Double.parseDouble(valueField.getText()));
            if (isNegative()) {
                controller.setImageNegative(true);
            }
            controller.showDicom();
            popUpStage.close();

        });

    }

    /**
     * Metoda tworząca okno wyboru dowolnej maksymalnej wartości piksela
     *
     * @param controller
     */
    public void setMaxValChooser(Controller controller) {
        createCustomWindow("Please type in the maximum pixel value");

        submitButton.setOnMousePressed(mouseEvent -> {
            if (valueField.getText().isEmpty()) {
                popUpStage.close();
                MaxValGroup.getSelectedToggle().setSelected(false);
                return;
            }
            if ((Integer.parseInt(valueField.getText()) < 0)) {
                informationLabel.setText("The minimum value is 0!");
                return;
            }
            controller.setMaxVal(Double.parseDouble(valueField.getText()), false, true);
            showMaxVal(controller.getMaxVal());
            if (isNegative()) {
                controller.setImageNegative(true);
            }
            controller.showDicom();
            popUpStage.close();


        });
    }

    /**
     * Metoda dopasowująca wyświetlany obraz do wybranej opcji maxValue lub WC/WW
     *
     * @param text
     * @param maxVal
     * @param windowCenter
     * @param windowWidth
     * @param controller
     * @param item
     */
    public void setOnAction(String text, double maxVal, double windowCenter, double windowWidth, Controller controller, RadioMenuItem item) {
        item.setOnAction(actionEvent -> {

            if (item.getText().equals(text)) {
                controller.setMaxVal(maxVal, false, true);
                showMaxVal(controller.getMaxVal());
            } else {
                controller.setWindowParameters(windowCenter, windowWidth, false, true);
                showWindowParameters(windowWidth, windowCenter);

            }
            if (isNegative()) {
                controller.setImageNegative(true);
            }
            if (PaletteGroup.getSelectedToggle() == palette1MenuItem || PaletteGroup.getSelectedToggle() == null) {
                controller.showDicom();
                return;
            }
            RadioMenuItem selectedItem = (RadioMenuItem) PaletteGroup.getSelectedToggle();
            controller.setPalette(selectedItem.getText(), isNegative());

        });
    }

    /**
     * Metoda tworząca okno wyboru dowolnej wartości kąta
     *
     * @param controller
     */
    public void setDegreeChooser(Controller controller) {

        createCustomWindow("Please type in the degree value");
        informationLabel.setLayoutX(40);
        informationLabel.setLayoutY(75);
        valueField.setLayoutX(50);
        valueField.setLayoutY(105);
        submitButton.setLayoutX(125);
        submitButton.setLayoutY(145);
        cancelButton.setLayoutX(65);
        cancelButton.setLayoutY(145);
        submitButton.setOnMousePressed(mouseEvent -> {
            if (valueField.getText().isEmpty()) {
                popUpStage.close();
                return;
            }
            if (!valueField.getText().isEmpty() && (Integer.parseInt(valueField.getText()) > 360)) {
                informationLabel.setText("The available range is [0,360]!");
                return;
            }
            controller.rotate(imagePane, Integer.parseInt(valueField.getText()), true);
            popUpStage.close();


        });


    }

    private void clearMaxVal() {
        maxVal.setVisible(false);
    }

    private void clearWindowParameters() {
        windowCenter.setVisible(false);
        windowWidth.setVisible(false);
    }

    public void clearTreeSelection() {
        treeView.getSelectionModel().clearSelection();
    }

    private double getImageWidthRatio() {
        return getImageWidth() / imageView.getFitWidth();
    }

    private double getImageHeightRatio() {
        return getImageHeight() / imageView.getFitHeight();
    }

    public Pane getRoot() {
        return root;
    }


    public boolean isTreeViewVisible() {
        return treeView.isVisible();
    }

    public void prepareWindow() {
        if (!pane.isVisible() || !pane1.isVisible()) {
            pane.setVisible(true);
            pane1.setVisible(true);
            root.getChildren().remove(mediaView);
        }
        new ProcessingWindow(getPane());
        clearTreeSelection();
        clearImageView();
    }

    /**
     * Główna metoda przypisująca eventy danym kontenerom. Kontroler w reakcji na jakiś event dokonuje jego obsługi -
     * jeśli potrzebne są jakieś dane z DicomReadera, kontroler pozyskuje je z DicomReadera, przetwarza oraz zwraca do widoku
     *
     * @param controller
     */
    public void setControls(Controller controller) {

        // po naciśnięciu na "File", kontroler otwiera okno wyboru pliku, widok wyświetla drzewo i chowa imageView jeśli jest już wyświetlone
        loadFileMenuItem.setOnAction(actionEvent -> {
            prepareWindow();
            setFileChooser();
            List<File> list = openFileChooser();
            if(list != null) {
                controller.selectFile(list);
            }

        });

        treeView.getSelectionModel().selectedItemProperty().addListener((observableValue, stringTreeItem, t1) -> {
            if (t1 != null) {
                hideWarningLabel();
                boolean imageDetected = controller.readDicom(t1);
                boolean negative;
                // jeśli nie wykryto danych obrazowych, wyczyść wszystko
                if (!imageDetected) {
                    clearCursorCoordinates();
                    clearPatientInformation();
                    clearMaxVal();
                    clearWindowParameters();
                    clearMenus();
                    geomGroup.getChildren().clear();
                    return;
                }
                if (isNegative()) {
                    controller.setImageNegative(true);
                    negative = true;
                } else {
                    controller.setImageNegative(false);
                    negative = false;
                }
                if (PaletteGroup.getSelectedToggle() != null && PaletteGroup.getSelectedToggle() != palette1MenuItem) {
                    RadioMenuItem selectedItem = (RadioMenuItem) PaletteGroup.getSelectedToggle();
                    controller.setPalette(selectedItem.getText(), negative);
                } else {
                    controller.showDicom();
                }
                showPatientInformation();
                showMenus();
                if (!controller.hasWindowParametersDefined()) {
                    clearWindowParameters();
                    setMaxValueMenu();
                    showMaxVal(controller.getMaxVal());
                } else {
                    clearMaxVal();
                    setWCWWMenu();
                    showWindowParameters(controller.getWindowWidth(), controller.getWindowCenter());
                }
                if (MaxValGroup.getSelectedToggle() != null) {
                    MaxValGroup.getSelectedToggle().setSelected(false);
                }
                resetScale(controller);
                adjustImageView();
            }
        });


        scene.heightProperty().addListener((observableValue, number, t1) -> {

            double height = pane1.getPrefHeight();
            pane1.setPrefHeight((double) t1 - 35);
            imagePane.setLayoutY(imagePane.getLayoutY() + (pane1.getPrefHeight() - height) / 2);
            if ((double) t1 - (double) number > 0) {
                pane1.setScaleY(pane1.getScaleY() + pane1.getPrefHeight() / height - 1);
                pane1.setScaleX(pane1.getScaleX() + pane1.getPrefHeight() / height - 1);
            } else {
                pane1.setScaleY(pane1.getScaleY() - height / pane1.getPrefHeight() + 1);
                pane1.setScaleX(pane1.getScaleX() - height / pane1.getPrefHeight() + 1);
            }
            pane.setPrefHeight((double) t1 - 35);
            treeView.setPrefHeight(pane.getPrefHeight());
            adjustMaxVal(root.getHeight() - 55);
            pixelValue.setLayoutY(root.getHeight() - 70);
            if (infoLabel != null) {
                infoLabel.setLayoutY(scene.getHeight() / 2);
            }


        });

        treeView.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                controller.delete(treeView.getSelectionModel().getSelectedItem());
            }
        });


        scene.widthProperty().addListener((observableValue, number, t1) -> {
            double width = pane1.getPrefWidth();
            pane1.setPrefWidth((double) t1 - 200);
            imagePane.setLayoutX(imagePane.getLayoutX() + (pane1.getPrefWidth() - width) / 2);
            warningLabel.setLayoutX(200 + (pane1.getPrefWidth() - warningLabel.getPrefWidth()) / 2);

            adjustCoordinatesToPane();
        });

        xPosition.setOnMouseMoved(mouseEvent -> {
            xPosition.setVisible(true);
            mouseEvent.consume();
        });

        yPosition.setOnMouseMoved(mouseEvent -> {
            yPosition.setVisible(true);
            mouseEvent.consume();
        });

        zoom1.setOnAction(actionEvent -> controller.zoom(pane1, imagePane, 1));
        zoom2.setOnAction(actionEvent -> controller.zoom(pane1, imagePane, 2));
        zoom3.setOnAction(actionEvent -> controller.zoom(pane1, imagePane, 4));
        zoom4.setOnAction(actionEvent -> controller.zoom(pane1, imagePane, 8));
        zoom5.setOnAction(actionEvent -> controller.zoom(pane1, imagePane, 0));

        scene.setOnMouseMoved(mouseEvent -> {
            if (imageView.getImage() == null || mouseEvent.getTarget() != imageView) {
                hideCursorPositions();
                hidePixelValue();
                return;
            }
            TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
            Point2D point2D = imageView.sceneToLocal(mouseEvent.getX(), mouseEvent.getY());
            pixelXPos = (int) (point2D.getX() * getImageWidthRatio());
            pixelYPos = (int) (point2D.getY() * getImageHeightRatio());
            showCursorPosition(pixelXPos, pixelYPos);
            // kalkulacja wartości piksela za pomocą wzoru: y*height + x (ilość pełnych rzędów + współrzędna w ostatnim rzędzie)
            showPixelValue(controller.getPixelValue((int) ((pixelYPos) * (getImageWidth())), pixelXPos, selectedItem));
            if (isSelected(angleMenuItem)) {
                if (angle != null && !angle.isDrawn()) {
                    angle.drawLine(point2D.getX(), point2D.getY());
                }
            }
            if (isSelected(polylineMenuItem) || isSelected(polygonMenuItem)) {
                if (customPolygon != null) {
                    customPolygon.drawLine(point2D.getX(), point2D.getY());
                }
            }
            if (isSelected(cobbAngleMenuItem)) {
                if (cobbAngle != null && !cobbAngle.isDrawn()) {
                    cobbAngle.drawLine(point2D.getX(), point2D.getY());
                }
            }

        });

        setOnAction("0.5%", 0.005, 20, 40, controller, percent1);
        setOnAction("1%", 0.01, 40, 80, controller, percent2);
        setOnAction("2.5%", 0.025, 80, 160, controller, percent3);
        setOnAction("5%", 0.05, 160, 320, controller, percent4);
        setOnAction("10%", 0.1, 320, 640, controller, percent5);
        setOnAction("25%", 0.25, 640, 1280, controller, percent6);
        setOnAction("50%", 0.5, 1280, 2560, controller, percent7);
        setOnAction("100%", 1, 2560, 5120, controller, percent8);


        customPercent.setOnAction(actionEvent -> {
            if (controller.hasWindowParametersDefined()) {
                setWCWWChooser(controller);
            } else {
                setMaxValChooser(controller);
            }

        });

        defaultWCWW.setOnAction(actionEvent -> {
            controller.setWindowParameters(controller.getDefaultWindowCenter(), controller.getDefaultWindowWidth(), false, true);
            if (isNegative()) {
                controller.setImageNegative(true);
            }
            showWindowParameters(controller.getDefaultWindowWidth(), controller.getDefaultWindowCenter());
            controller.showDicom();
        });


        negativeMenuLabel.setOnMousePressed(mouseEvent -> {
            boolean negative;
            //jeśli naciskamy i nie ma negatywu, robimy negatyw
            if (!isNegative()) {
                negative = true;
                controller.setImageNegative(true);
                negativeMenuLabel.getGraphic().setId("menu-item-icon-checked");
                setNegative(true);

            } else {
                negative = false;
                controller.setImageNegative(false);
                negativeMenuLabel.getGraphic().setId("menu-icon");
                setNegative(false);
            }
            if (PaletteGroup.getSelectedToggle() == null || PaletteGroup.getSelectedToggle() == palette1MenuItem) {
                controller.showDicom();
                return;
            }
            RadioMenuItem selectedItem = (RadioMenuItem) PaletteGroup.getSelectedToggle();
            controller.setPalette(selectedItem.getText(), negative);

        });


        customBin.setOnAction(actionEvent -> {
            setBasketChooser(controller);
        });

        bin1.setOnAction(actionEvent -> {
            controller.setHistogram(2);
        });

        bin2.setOnAction(actionEvent -> {
            controller.setHistogram(4);
        });

        bin3.setOnAction(actionEvent -> {
            controller.setHistogram(8);
        });

        bin4.setOnAction(actionEvent -> {
            controller.setHistogram(0);
        });

        clearFiguresMenuItem.setOnAction(actionEvent -> {
            geomGroup.getChildren().clear();
            clearAllMenuItems();
            customPolygon = null;
            cobbAngle = null;
        });

        clearSelectedFigureMenuItem.setOnAction(actionEvent -> {
            if (selectedShape instanceof CustomLine) {
                ((CustomLine) selectedShape).clear();
            }
            if (selectedShape instanceof Angle) {
                ((Angle) selectedShape).clear();
            }
            if (selectedShape instanceof CobbAngle) {
                ((CobbAngle) selectedShape).clear();
            }
            if (selectedShape instanceof CustomEllipse) {
                ((CustomEllipse) selectedShape).clear();
            }
            if (selectedShape instanceof CustomPolygon) {
                ((CustomPolygon) selectedShape).clear();
            }
        });


        scene.setOnMouseReleased(mouseEvent -> {
            if (isSelected(cobbAngleMenuItem) && cobbAngle != null) {
                cobbAngle.setDrawn();
            }
            if (mouseEvent.getClickCount() == 2 && mouseEvent.getButton().toString().equals("PRIMARY")) {

                if (!isSelected(polygonMenuItem) && !isSelected(polylineMenuItem)) {
                    return;
                }
                if (isSelected(polygonMenuItem)) {
                    customPolygon.connect();
                    if (!warningLabel.isVisible()) {
                        showWarningLabel();
                    }
                }
                customPolygon.setDrawn();
                customPolygon = null;
            }
        });

        scene.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.ANY);
            }
            e.consume();
        });

        scene.setOnDragDropped(e -> {
            prepareWindow();
            List<File> list = e.getDragboard().getFiles();
            controller.selectFile(list);
            //  controller.selectFile(list);

        });


        scene.setOnMousePressed(mouseEvent -> {
                    TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
                    Point2D point2D = imagePane.sceneToLocal(mouseEvent.getX(), mouseEvent.getY());
                    if (getImage() == null) {
                        return;
                    }
                    if (mouseEvent.isPrimaryButtonDown()) {
                        if (isSelected(polylineMenuItem) || isSelected(polygonMenuItem)) {
                            if (customPolygon == null) {
                                customPolygon = new CustomPolygon(point2D.getX(), point2D.getY(), geomGroup, imageView, controller.getPixelSpacing(), getImageWidthRatio(), getImageHeightRatio());
                            } else {
                                customPolygon.drawPolyline(point2D.getX(), point2D.getY());
                            }
                        }
                        if (isSelected(angleMenuItem)) {
                            if (angle == null || angle.isDrawn()) {
                                angle = new Angle(point2D.getX(), point2D.getY(), imageView, geomGroup);
                            } else {
                                angle.drawAngle(point2D.getX(), point2D.getY());
                            }
                        }
                        if (isSelected(cobbAngleMenuItem)) {
                            if (cobbAngle == null || cobbAngle.isDrawn()) {
                                cobbAngle = new CobbAngle(point2D.getX(), point2D.getY(), geomGroup);
                            } else {
                                cobbAngle.drawCobbAngle(point2D.getX(), point2D.getY());
                            }
                        }
                        if (isSelected(lineMenuItem)) {
                            customLine = new CustomLine(point2D.getX(), point2D.getY(), point2D.getX(), point2D.getY(), geomGroup, imageView, getImageWidthRatio(), getImageHeightRatio(), selectedItem);
                            customLine.setCursorPositions(point2D.getX(), point2D.getY());
                            customLine.setLineEvents();
                        }
                        if (isSelected(elipseMenuItem)) {
                            customEllipse = new CustomEllipse(point2D.getX(), point2D.getY(), geomGroup, imageView, controller.getPixelSpacing(), getImageWidthRatio(), getImageHeightRatio(), selectedItem);
                        }

                    } else if (mouseEvent.isMiddleButtonDown()) {
                        showQuickToolkit(mouseEvent.getX(), mouseEvent.getY());
                    }
                    getCursorPosition(mouseEvent);
                    initialXPos = point2D.getX();
                    initialYPos = point2D.getY();
                    mouseEvent.consume();


                }
        );

        toolbox.setOnMouseExited(mouseEvent -> fadeOut(toolbox));
        scene.setOnMouseDragged(mouseEvent -> {
            Point2D point2D = imagePane.sceneToLocal(mouseEvent.getX(), mouseEvent.getY());
            if (getImage() != null) {
                double deltaX;
                double deltaY;
                if (windowModificationSelected && mouseEvent.isPrimaryButtonDown()) {
                    deltaX = Math.abs(point2D.getX() - initialXPos);
                    deltaY = Math.abs(point2D.getY() - initialYPos);
                    double val = 1;
                    if (deltaY > pane1.getPrefHeight() / 4 || deltaX > pane1.getPrefWidth() / 4) {
                        val = 2;
                    }
                    if (deltaY > pane1.getPrefHeight() / 2 || deltaX > pane1.getPrefWidth() / 2) {
                        val = 4;
                    }
                    if (deltaY > 3 * pane1.getPrefHeight() / 4 || deltaX > 3 * pane1.getPrefWidth() / 4) {
                        val = 8;
                    }
                    if (controller.hasWindowParametersDefined()) {

                        if (mouseEvent.getY() - cursorYPosition > 0) {
                            controller.setWindowParameters(-val, 0, false, false);
                        }
                        if (mouseEvent.getY() - cursorYPosition < 0) {
                            controller.setWindowParameters(val, 0, false, false);
                        }
                        if (mouseEvent.getX() - cursorXPosition > 0) {
                            controller.setWindowParameters(0, val, false, false);
                        }
                        if (mouseEvent.getX() - cursorXPosition < 0) {
                            controller.setWindowParameters(0, -val, false, false);
                        }
                        getCursorPosition(mouseEvent);
                        if (isNegative()) {
                            controller.setImageNegative(true);
                        }
                        showWindowParameters(controller.getWindowWidth(), controller.getWindowCenter());

                    } else {
                        if (mouseEvent.getY() - cursorYPosition > 0) {
                            maxValue = controller.setMaxVal(-val, false, false);
                        }
                        if (mouseEvent.getY() - cursorYPosition < 0) {
                            maxValue = controller.setMaxVal(val, false, false);
                        }
                        if (isNegative()) {
                            controller.setImageNegative(true);
                        }
                        showMaxVal(maxValue);
                    }
                    if (PaletteGroup.getSelectedToggle() != null && PaletteGroup.getSelectedToggle() != palette1MenuItem) {
                        RadioMenuItem selectedItem = (RadioMenuItem) PaletteGroup.getSelectedToggle();
                        controller.setPalette(selectedItem.getText(), isNegative());
                    } else {
                        controller.showDicom();
                    }
                } else {
                    if (rotationSelected && mouseEvent.isPrimaryButtonDown()) {
                        controller.rotate(imagePane, mouseEvent.getX() - cursorXPosition, false);
                    } else if (isSelected(lineMenuItem) && mouseEvent.isPrimaryButtonDown()) {
                        customLine.drawLine(point2D.getX(), point2D.getY());

                    } else if (isSelected(elipseMenuItem) && mouseEvent.isPrimaryButtonDown()) {
                        customEllipse.drawElipse(point2D.getX(), point2D.getY());
                    } else if (!isSelected(polylineMenuItem) || !isSelected(polygonMenuItem) || zoomSelected) {
                        controller.scale(mouseEvent, cursorYPosition, cursorXPosition, pane1, imagePane, zoomSelected);
                    }
                    if (MaxValGroup.getSelectedToggle() != null) {
                        MaxValGroup.getSelectedToggle().setSelected(false);
                    }
                }

                getCursorPosition(mouseEvent);

            }

        });


        rotation1.setOnAction(actionEvent -> controller.rotate(imagePane, 90, false));
        rotation2.setOnAction(actionEvent -> controller.rotate(imagePane, 180, false));
        rotation3.setOnAction(actionEvent -> controller.rotate(imagePane, 270, false));
        defaultRotation.setOnAction(actionEvent -> controller.rotate(imagePane, 0, true));
        customRotation.setOnAction(actionEvent -> setDegreeChooser(controller));
        PaletteGroup.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
            if (t1 != null) {
                RadioMenuItem selectedItem = (RadioMenuItem) t1;
                controller.setPalette(selectedItem.getText(), isNegative());
            }
        });

        layout1.setOnAction(actionEvent -> {
            scene.getStylesheets().clear();
            scene.getStylesheets().add("stylesheet.css");
        });

        layout2.setOnAction(actionEvent -> {
            scene.getStylesheets().clear();
            scene.getStylesheets().add("stylesheet2.css");
        });
        layout3.setOnAction(actionEvent -> {
            scene.getStylesheets().clear();
            scene.getStylesheets().add("stylesheet3.css");
        });

        layout4.setOnAction(actionEvent -> {
            scene.getStylesheets().clear();
            scene.getStylesheets().add("stylesheet4.css");
        });


    /*    patientInfoMenu.showingProperty().addListener(
                (observableValue, oldValue, newValue) -> {
                    if (newValue) {
                        patientInfoMenuItem.fire();
                    }
                }
        );

        patientInfoMenuItem.setOnAction(actionEvent -> {
            PatientInfoView patientInfoView = new PatientInfoView(controller.getFileInformation());
        });*/


    }


}


