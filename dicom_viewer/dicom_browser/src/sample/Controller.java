package sample;


import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.dcm4che3.data.Attributes;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Klasa odpowiadająca za interakcję interfejsu graficznego z działaniami użytkownika
 */
public class Controller {

    private final GUICreator guiCreator;
    private static Controller controller;
    private DicomReader dicomReader;
    private final List<File> fileList;
    private TreeItem<String>  filePointer,  frame;
    private TreeItem<String>  secondRoot;
    private List<TreeItem<String>> fileItemList;

    private Map<String, Map<String, Map<String, Map<String,Integer>>>> patientIDMap;
    private Map<String, Map<String, Map<String,Integer>>> studyInstanceMap;
    private Map<String,Map<String,Integer>> seriesInstanceMap;
    private Map<String,Integer> fileMap;


    /**
     * Konstruktor tworzący listę plików i pacjentów oraz wywołujący instancję GUI - dzięki temu może korzystać z metod i pól interfejsu graficznego
     */
    private Controller() {

        guiCreator = GUICreator.getInstance();
        fileList = new ArrayList<>();
        fileItemList = new ArrayList<>();
        secondRoot = new TreeItem<>("DICOM");
        patientIDMap = new HashMap<>();

    }

    /**
     * Klasyczna metoda identyfikująca Controller jako Singleton
     *
     * @return
     */

    public static Controller getInstance() {
        if (controller == null) {
            controller = new Controller();
        }
        return controller;
    }


    /**
     * Metoda tworząca drzewo wyboru na podstawie wcześniej utworzonej mapy
     */
    public void createTree() {
        secondRoot = new TreeItem<>();
        fileItemList = new ArrayList<>();
        for (String patientName : patientIDMap.keySet()) {
            TreeItem<String> patientNameTreeItem = new TreeItem<>(patientName);
            secondRoot.getChildren().add(patientNameTreeItem);
            studyInstanceMap = patientIDMap.get(patientName);
            for (String studyInstance : studyInstanceMap.keySet()) {
                TreeItem<String> studyInstanceTreeItem = new TreeItem<>(studyInstance);
                patientNameTreeItem.getChildren().add(studyInstanceTreeItem);
                seriesInstanceMap = studyInstanceMap.get(studyInstance);
                for(String seriesInstance : seriesInstanceMap.keySet()){
                    TreeItem<String> seriesInstanceTreeItem = new TreeItem<>(seriesInstance);
                    studyInstanceTreeItem.getChildren().add(seriesInstanceTreeItem);
                    fileMap = seriesInstanceMap.get(seriesInstance);
                    for(String file : fileMap.keySet()){
                        filePointer = new TreeItem<>(file);
                        seriesInstanceTreeItem.getChildren().add(filePointer);
                        fileItemList.add(filePointer);
                    }
                    sortFiles(seriesInstanceTreeItem,fileMap);
                }
            }

        }
        guiCreator.setTreeRoot(secondRoot);
        guiCreator.showTree();
    }

    public void selectFile(List<File> list) {
        ProcessingWindow window;
        if(guiCreator.isTreeViewVisible()) {
            window = new ProcessingWindow(guiCreator.getPane());
        }
        else{
            window = new ProcessingWindow(guiCreator.getRoot());
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                createFileMap(list);
                return null;
            }
        };
        task.setOnRunning(e -> {
                    guiCreator.lockWindow();
                    window.showWindow();
                    guiCreator.clearMenus();
                    guiCreator.disableTreeView();
                });

        task.setOnSucceeded(e -> {

            guiCreator.enableTreeView();
            window.repaint();
            window.hideWindow();
            guiCreator.unlockWindow();
            PauseTransition p = new PauseTransition(Duration.millis(800));
            p.setOnFinished(ee->{
                createTree();
            });
            p.play();
        });
        new Thread(task).start();
    }


    /**
     * Metoda tworząca mapę porządkującą wczytane pliki według parametrów PatientName,StudyInstance,SeriesInstance
     * @param list
     */
    public void createFileMap(List<File> list) {
        if (list == null) {
            return;
        }
        for (File file : list) {
            if(fileList.contains(file)){
                continue;
            }
            fileList.add(file);
            Patient patient = new Patient(file);
            if (patient.getPatientID() == null) {
                continue;
            }
            if (patientIDMap.isEmpty()) {
                add(file, patient);
                patientIDMap = new HashMap<>();
                patientIDMap.put(patient.getPatientName(), studyInstanceMap);
                continue;
            }
            if(!patientIDMap.containsKey(patient.getPatientName())){
                add(file, patient);
                patientIDMap.put(patient.getPatientName(), studyInstanceMap);
                continue;
            }
            studyInstanceMap = patientIDMap.get(patient.getPatientName());
            if(!studyInstanceMap.containsKey(patient.getStudyInstance())){
                fileMap = new HashMap<>();
                fileMap.put(file.getName(),patient.getInstanceNumber());
                seriesInstanceMap = new HashMap<>();
                seriesInstanceMap.put(patient.getSeriesInstance(), fileMap);
                studyInstanceMap.put(patient.getStudyInstance(),seriesInstanceMap);
                continue;
            }
            seriesInstanceMap = studyInstanceMap.get(patient.getStudyInstance());
            if(!seriesInstanceMap.containsKey(patient.getSeriesInstance())){
                fileMap = new HashMap<>();
                fileMap.put(file.getName(),patient.getInstanceNumber());
                seriesInstanceMap.put(patient.getSeriesInstance(), fileMap);
                continue;
            }
            fileMap = seriesInstanceMap.get(patient.getSeriesInstance());
            if(!fileMap.containsKey(file.getName())){
                fileMap.put(file.getName(),patient.getInstanceNumber());
            }
        }

    }

    private void add(File file, Patient patient) {
        fileMap = new HashMap<>();
        fileMap.put(file.getName(),patient.getInstanceNumber());
        seriesInstanceMap = new HashMap<>();
        seriesInstanceMap.put(patient.getSeriesInstance(), fileMap);
        studyInstanceMap = new HashMap<>();
        studyInstanceMap.put(patient.getStudyInstance(), seriesInstanceMap);
    }


    /**
     * Metoda tworząca histogramy i wyświetlająca je na planszy
     * Jeśli baskets=0, tworzony jest pełny histogram
     *
     * @param baskets
     */
    public void setHistogram(int baskets) {
        if (baskets == 0) {
            ProcessingWindow window = new ProcessingWindow(guiCreator.getPane());
            Task<Histogram> task = new Task<>() {
                @Override
                protected Histogram call() {
                    return new Histogram(getProperties(), 0);
                }
            };
            task.setOnRunning(e -> {
                window.showWindow();
                guiCreator.disableHistogramMenu();
            });
            task.setOnSucceeded(e -> {
                window.repaint();
                window.hideWindow();
                Histogram hist = task.getValue();
                PauseTransition p = new PauseTransition(Duration.millis(800));
                p.setOnFinished(ee -> {
                    hist.drawWindow();
                    PauseTransition p1 = new PauseTransition(Duration.millis(800));
                    p1.setOnFinished(eee ->
                            hist.add());
                            guiCreator.enableHistogramMenu();
                    p1.play();
                });
                p.play();
            });
            new Thread(task).start();
            return;
        }
        Histogram hist = new Histogram(getProperties(), baskets);
        hist.drawWindow();
        hist.add();
    }


    /**
     * Metoda sortująca pliki dla danej serii według Tagu InstanceNumber
     */
    public void sortFiles(TreeItem<String> seriesInstanceTreeItem,Map<String,Integer> fileMap) {



        List<Integer> indexList = new ArrayList<>();
        List<Integer> orderedIndexList = new ArrayList();
        ObservableList<TreeItem<String>> fileList = FXCollections.observableArrayList();
        ObservableList<TreeItem<String>> orderedFileList = FXCollections.observableArrayList();
        fileList.addAll(seriesInstanceTreeItem.getChildren());
        for(TreeItem<String> file : fileList){
            indexList.add(fileMap.get(file.getValue()));
        }
        while (!fileList.isEmpty()) {
            // znajdujemy indeks elementu o najmniejszej wartości
            int minIndex = indexList.indexOf(Collections.min(indexList));
            // dodajemy do nowej listy element ze starej listy o tym indeksie
            orderedFileList.add(fileList.get(minIndex));
            // dodajemy do nowej listy indeksów uporządkowane indeksy
            orderedIndexList.add(indexList.get(minIndex));
            // wyrzucamy element żeby w nastepnej iteracji porządkować kolejne
            fileList.remove(minIndex);
            indexList.remove(minIndex);
        }
        // podmieniamy w mapie seriesInstance nieuporządkowane na nowe wraz z nową listą indeksów

       // instanceNumberMap1.remove(seriesInstanceTreeItem.getValue());
        seriesInstanceTreeItem.getChildren().setAll(orderedFileList);
       // instanceNumberMap1.put(seriesInstanceTreeItem.getValue(), orderedIndexList);


    }




    public double getPixelSpacing() {
        return dicomReader.getPixelSpacing();
    }






    public void setFrame(int index) {
        frame = new TreeItem<>();
        frame.setValue(String.valueOf(index));

    }


    public void addFrameToTree() {
        filePointer.getChildren().add(frame);
    }












    public double getDefaultWindowWidth() {
        return dicomReader.getDefaultWindowWidth(filePointer);
    }

    public double getDefaultWindowCenter() {
        return dicomReader.getDefaultWindowCenter(filePointer);
    }

    public void setWindowParameters(double windowCenter, double windowWidth, boolean negativeOn, boolean customVal) {
        dicomReader.changeWindowParameters(windowCenter, windowWidth, filePointer, negativeOn, customVal);
    }



    /**
     * Metoda pokazująca podstawowe dane pacjenta i badania na ekranie
     */
    public void setPatientInformation() {
        guiCreator.setNameLabel(dicomReader.getPatientName(dicomReader.getProperties(filePointer)));
        guiCreator.setDateLabel(dicomReader.getStudyDate(dicomReader.getProperties(filePointer)));
        guiCreator.setModalityLabel(dicomReader.getModality(dicomReader.getProperties(filePointer)));

    }

    /**
     * Metoda usuwająca plik obrazowy
     *
     * @param t1
     */
    public void delete(TreeItem<String> t1) {
        boolean imageDetected = dicomReader.findImageForChosenFile(t1);
        if (imageDetected) {
            if (dicomReader.isMultiFramed() && t1.getChildren().size() == 0) {
                return;
            }
            guiCreator.clearTreeSelection();
            guiCreator.clearImageView();
            dicomReader.removeImage(t1);
            fileMap.remove(t1.getValue());
            fileItemList.remove(t1);
            guiCreator.clearMenus();
            // przeszukiwanie listy plików w poszukiwaniu zaznaczonego elementu
            for (int i = 0; i < fileList.size(); i++) {
                if (t1.getValue().equals(fileList.get(i).getName())) {
                    // usunięcie pliku z listy plików
                    fileList.remove(i);
                    break;
                }
            }
            t1.getParent().getChildren().remove(t1);
        }

    }

    public List<Object> getProperties() {
        return dicomReader.getProperties(filePointer);
    }

    public double getWindowWidth() {
        return dicomReader.getWindowWidth(dicomReader.getProperties(filePointer));
    }

    public double getWindowCenter() {
        return dicomReader.getWindowCenter(dicomReader.getProperties(filePointer));
    }

    /**
     * Metoda sprawdzająca, czy dany plik ma zdefiniowane parametry okna
     *
     * @return
     */
    public boolean hasWindowParametersDefined() {
        return dicomReader.windowParametersDefined(dicomReader.getProperties(filePointer));
    }

    /**
     * Metoda pobierająca z danego pliku maksymalną wartość
     *
     * @return
     */
    public int getMaxVal() {
        return dicomReader.getMaxVal();
    }


    /**
     * Metoda wyświetlająca wszystkie składowe pliku multiFramed w drzewie wyboru
     */
    public void presentMultiframedDICOMS() {
        if (dicomReader.isMultiFramed()) {
            // Jeśli zaznaczony element nie ma jeszcze wyświetlonych frame'ów
            if (filePointer.getChildren().size() < 2) {
                for (int i = 0; i < dicomReader.getFrames(); i++) {
                    setFrame(i);
                    addFrameToTree();
                }
            }
        }
    }


    public Attributes getFileInformation() {
        int index = fileItemList.indexOf(filePointer);
        dicomReader.readFile(fileList.get(index));
        return dicomReader.getAttributes();
    }


    /**
     * Metoda pobierająca chwilową wartość piksela
     *
     * @param cursorY
     * @param cursorX
     * @param t1
     * @return
     */
    public double getPixelValue(int cursorY, int cursorX, TreeItem<String> t1) {
        return dicomReader.getPixelValue(cursorY, cursorX, t1);
    }

    /**
     * Metoda zoomująca obraz na podstawie wybranego elementu z menu kontekstowego "Zoom"
     *
     * @param pane
     * @param imagePane
     * @param val
     */
    public void zoom(Pane pane, Pane imagePane, double val) {

        //dopełnianie do wysokości okna
        if (val == 0) {
            pane.setScaleX(pane.getPrefHeight() / imagePane.getPrefHeight());
            pane.setScaleY(pane.getScaleX());
        }
        //wyznaczanie skali na stosunku oryginalny obraz/obraz wyświetlany
        else {
            double height = getOriginalImageHeight() * val;
            pane.setScaleY(height / imagePane.getPrefHeight());
            pane.setScaleX(pane.getScaleY());
        }

    }


    /**
     * Metoda pozyskująca informacje obrazowe z pliku DICOM i wyświetlająca go.
     * Gdy plik jest wczytywany po raz pierwszy, następuje pełna procedura wczytania.
     * Z każdym następnym razem wczytany już plik jest wyświetlany, bez ponownego wczytywania.
     *
     * @param t1
     */

    public boolean readDicom(TreeItem<String> t1) {
        boolean imageDetected = false;
        boolean isImageElement = true;

        guiCreator.clearImageView();
        dicomReader = DicomReader.getInstance();
        filePointer = t1;
        // sprawdzamy czy dicomReader ma już załadowany obraz dla elementu drzewa
        if (!dicomReader.hasImgFor(t1)) {
            // jeśli nie ma, sprawdzamy czy jest to plik multiframed
            if (fileItemList.contains(t1)) {
                int index = fileItemList.indexOf(t1);
                String name = t1.getValue();
                File selectedFile = null;
                for(File file:fileList){
                    if(file.getName().equals(name)){
                        selectedFile = file;
                        break;
                    }
                }
                dicomReader.readFile(selectedFile);
                dicomReader.readTags();
                imageDetected = dicomReader.read(filePointer);
                if (imageDetected) {
                    presentMultiframedDICOMS();
                }
            } else {
                isImageElement = false;
            }
        }
        // wyświetl plik
        if (t1.getChildren().size() == 0 && isImageElement) {
            imageDetected = dicomReader.findImageForChosenFile(t1);
        }
        return imageDetected;

    }


    public double getOriginalImageHeight() {
        return dicomReader.getOriginalHeight(filePointer);

    }

    /**
     * Metoda rotująca obraz o wybraną ilość stopni
     *
     * @param imagePane
     * @param degrees
     * @param custom
     */
    public void rotate(Pane imagePane, double degrees, boolean custom) {
        if (custom) {
            imagePane.setRotate(degrees);
        } else {
            imagePane.setRotate(imagePane.getRotate() + degrees);
        }
    }

    public int setMaxVal(double val, boolean negativeOn, boolean customVal) {
        return dicomReader.changeMaxValue(filePointer, val, negativeOn, customVal);
    }

    /**
     * Metoda przybliżająca/oddalająca obraz w zależności od ruchu kursora oraz bindująca przemieszczanie pod prawy przycisk myszki
     *
     * @param mouseEvent
     * @param cursorYPosition
     * @param cursorXPosition
     * @param pane1
     * @param imagePane
     * @param zoom
     */
    public void scale(MouseEvent mouseEvent, double cursorYPosition, double cursorXPosition, Pane pane1, Pane imagePane, boolean zoom) {
        final double SCALE_DELTA = 1.01;
        double scaleFactor;
        double deltaX = mouseEvent.getX() - cursorXPosition;
        double deltaY = mouseEvent.getY() - cursorYPosition;
        if (zoom && mouseEvent.isPrimaryButtonDown()) {
            if (mouseEvent.getY() - cursorYPosition < 0) {
                if (pane1.getScaleX() < 7) {
                    scaleFactor = SCALE_DELTA;
                    pane1.setScaleY(pane1.getScaleY() * scaleFactor);
                    pane1.setScaleX(pane1.getScaleX() * scaleFactor);
                }

            }
            if (mouseEvent.getY() - cursorYPosition > 0) {
                scaleFactor = 1 / SCALE_DELTA;
                pane1.setScaleY(pane1.getScaleY() * scaleFactor);
                pane1.setScaleX(pane1.getScaleX() * scaleFactor);
                if (pane1.getScaleX() <= 0.25) {
                    pane1.setScaleX(0.25);
                    pane1.setScaleY(0.25);
                }
            }
        }
        if (mouseEvent.isSecondaryButtonDown()) {
            imagePane.setLayoutX((imagePane.getLayoutX() + deltaX / pane1.getScaleX()));
            imagePane.setLayoutY(imagePane.getLayoutY() + deltaY / pane1.getScaleY());
        }

    }


    /**
     * Metoda wyświetlająca obraz na ekranie oraz informacje o pacjencie
     */

    public void showDicom() {

        if (dicomReader.getFrameIndex() == -1) {
            List<BufferedImage> list = (List<BufferedImage>) dicomReader.getMap().get(filePointer);
            guiCreator.setImage(SwingFXUtils.toFXImage(list.get(0), null));

        } else {
            List<BufferedImage> list = (List<BufferedImage>) dicomReader.getMap().get(filePointer.getParent());
            guiCreator.setImage(SwingFXUtils.toFXImage(list.get(dicomReader.getFrameIndex()), null));
        }
        guiCreator.showImageView();
        setPatientInformation();

    }

    public void setImageNegative(boolean negativeRequested) {
        dicomReader.setNegative(filePointer, negativeRequested);
    }

    /**
     * Metoda wyświetlająca obraz w wybranej palecie kolorów
     *
     * @param paletteNumber
     * @param negative
     */
    public void setPalette(String paletteNumber, boolean negative) {
        int[] r = new int[256];
        int[] g = new int[256];
        int[] b = new int[256];
        int rIndex = 0;
        int gIndex = 0;
        int bIndex = 0;
        int counter = 1;
        try {
            Scanner sc = new Scanner(new File("palettes/PAL_0" + paletteNumber + ".txt"));
            while (sc.hasNext()) {
                String word = sc.next();
                int value = Integer.parseInt(word.substring(0, word.length() - 1));
                if (counter % 3 == 1) {
                    r[rIndex] = value;
                    rIndex++;
                } else if (counter % 3 == 2) {
                    g[gIndex] = value;
                    gIndex++;
                } else {
                    b[bIndex] = value;
                    bIndex++;
                }
                counter++;

            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        BufferedImage image = dicomReader.changeColorPalette(filePointer, r, g, b, negative);
        guiCreator.setImage(SwingFXUtils.toFXImage(image, null));
        guiCreator.showImageView();
    }


}

