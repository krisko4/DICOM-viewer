package sample;



import javafx.scene.control.TreeItem;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.imageio.plugins.dcm.*;
import org.dcm4che3.io.DicomInputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;


/**
 * Klasa odczytująca informacje dotyczące badania i pacjenta z pliku DICOM
 */

public class DicomReader {


    private final Map<BufferedImage, List<Object>> propertiesMap;
    private int maxVal;
    private String rescaleSlope, rescaleIntercept, pixelSpacing, patientName, studyDate, modality;
    private double windowCenter, windowWidth;
    private Raster raster;
    private int frameIndex;
    private final Map<TreeItem<String>, List<BufferedImage>> imgMap;
    private ImageReader reader;
    private int frames;
    private Attributes attr;
    private BufferedImage img;
    private List<BufferedImage> imgList;
    private File file;
    private static DicomReader dicomReader;

    public static DicomReader getInstance() {
        if (dicomReader == null) {
            dicomReader = new DicomReader();
        }
        return dicomReader;
    }

    private DicomReader() {
        imgMap = new HashMap<>();
        propertiesMap = new HashMap<>();
    }

    /**
     * Metoda sprawdzająca, czy plik DICOM jest wieloklatkowy
     * @return
     */
    public boolean isMultiFramed() {
        return (int) (propertiesMap.get(img)).get(12) > 1;
    }

    /**
     * Metoda sprawdzająca czy dla danego elementu drzewa jest już wczytany plik obrazowy
     * @param t1
     * @return
     */
    public boolean hasImgFor(TreeItem<String> t1) {
        if (t1.getParent() != null) {
            return imgMap.containsKey(t1) || imgMap.containsKey(t1.getParent());
        }
        return false;
    }


    /**
     * Metoda pobierająca oryginalną wysokość obrazu
     * @param t1
     * @return
     */
    public double getOriginalHeight(TreeItem<String> t1) {
        findImageForChosenFile(t1);
        return img.getRaster().getHeight();
    }

    public int getImgListSize() {
        return imgList.size();
    }


    public void readFile(File file) {
        this.file = file;
    }

    public List<BufferedImage> getImgList() {
        return this.imgList;
    }

    public BufferedImage getImg() {
        return img;
    }


    public BufferedImage getImage() {
        return imgList.get(0);
    }


    /**
     * Metoda konwertująca tablicę bajtów do tablicy int
     * @param array
     * @return
     */
    public int[] convertArrayToInteger(byte[] array) {
        int[] rawPixels = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            rawPixels[i] = array[i];
        }
        return rawPixels;
    }


    /**
     * Metoda konwertująca tablicę short do tablicy int
     * @param array
     * @return
     */
    public int[] convertArrayToInteger(short[] array) {
        int[] rawPixels = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            rawPixels[i] = array[i];
        }
        return rawPixels;

    }

    /**
     * Metoda ucinająca piksele do określonej wartości maksymalnej
     * @param pixels
     * @param maxVal
     * @return
     */
    public int[] cutDataToMaxVal(int[] pixels, int maxVal) {

        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] > maxVal) {
                pixels[i] = maxVal;
            }
        }
        return pixels;
    }

    /**
     * Metoda sprawdzająca czy dany obraz ma zdefiniowane parametry okna WC/WW
     * @param propertiesList
     * @return
     */
    public boolean windowParametersDefined(List<Object> propertiesList) {

        return (double) propertiesList.get(5) != 0 || (double) propertiesList.get(6) != 0;
    }

    /**
     * Metoda zwracająca właściwości dla danego obrazu
     * @param t1
     * @return
     */
    public List<Object> getProperties(TreeItem<String> t1) {
        findImageForChosenFile(t1);
        return propertiesMap.get(img);
    }

    /**
     * Metoda zwracająca imię pacjenta
     * @param propertiesList
     * @return
     */
    public String getPatientName(List<Object> propertiesList) {
        return (String) propertiesList.get(9);
    }

    /**
     * Metoda zwracająca datę badania
     * @param propertiesList
     * @return
     */
    public String getStudyDate(List<Object> propertiesList) {
        return (String) propertiesList.get(10);
    }

    /**
     * Metoda zwracająca modalność badania
     * @param propertiesList
     * @return
     */
    public String getModality(List<Object> propertiesList) {
        return (String) propertiesList.get(11);
    }

    /**
     * Metoda zwracająca wartość WindowWidth
     * @param propertiesList
     * @return
     */
    public double getWindowWidth(List<Object> propertiesList) {
        return (double) propertiesList.get(6);
    }

    /**
     * Metoda zwracająca wartość WindowCenter
     * @param propertiesList
     * @return
     */
    public double getWindowCenter(List<Object> propertiesList) {
        return (double) propertiesList.get(5);
    }

    /**
     * Metoda modyfikująca parametry okna.  Zwraca nowy obraz o zmienionych parametrach.
     * @param val1 wartość windowCenter pobrana od użytkownika
     * @param val2 wartość windowWidth pobrana od użtykownika
     * @param t1 element drzewa wyboru(plik)
     * @param negativeOn wartość mówiąca czy obraz ma być wyświetlony w negatywie
     * @param customVal informacja czy pobrana wartość jest dowolna czy jest wybrana z menu kontekstowego
     */
    public void changeWindowParameters(double val1, double val2, TreeItem<String> t1, boolean negativeOn, boolean customVal) {
        List<Object> propertiesList = getProperties(t1);
        raster = img.getRaster();
        int[] pixels = (int[]) propertiesList.get(0);
        int[] rawPixels = new int[pixels.length];
        System.arraycopy(pixels, 0, rawPixels, 0, pixels.length);
        maxVal = (int) propertiesList.get(2);
        double windowCenter;
        double windowWidth;
        if (customVal) {
            windowCenter = val1;
            windowWidth = val2;
        } else {
            double windowCenterVal = (double) propertiesList.get(5);
            double windowWidthVal = (double) propertiesList.get(6);
            windowCenterVal = windowCenterVal + val1;
            windowWidthVal = windowWidthVal + val2;
            if (windowWidthVal < 1) {
                windowWidthVal = 1;
            }
            windowCenter = windowCenterVal;
            windowWidth = windowWidthVal;
        }
        byte[] outPixels = setWindowParameters(rawPixels, maxVal, windowCenter, windowWidth);
        propertiesMap.remove(img);
        drawImage(raster.getWidth(), raster.getHeight(), outPixels);
        propertiesList.set(8, maxVal);
        propertiesList.set(5, windowCenter);
        propertiesList.set(6, windowWidth);
        if (!negativeOn) {
            propertiesList.set(1, false);
        }
        propertiesMap.put(img, propertiesList);
        replaceImage(t1);
    }

    /**
     * Metoda modyfikująca maksymalną wartość piksela. Zwraca nowy obraz o zmienionej wartości maksymalnej.
     * @param t1 element drzewa wyboru(plik)
     * @param val maksymalna wartość uzyskana od użytkownika
     * @param negativeOn wartość mówiąca czy obraz ma być wyświetlony w negatywie
     * @param customVal informacja czy pobrana wartość jest dowolna czy jest wybrana z menu kontekstowego
     * @return nowy obraz o zmienionej wartości maxValue
     */
    public int changeMaxValue(TreeItem<String> t1, double val, boolean negativeOn, boolean customVal) {
        List<Object> propertiesList = getProperties(t1);
        raster = img.getRaster();
        int[] pixels = (int[]) propertiesList.get(0);
        getMaximumPixelValue(1, pixels);
        double originalMaxVal = (int) propertiesList.get(2);
        maxVal = (int) propertiesList.get(8);
        int[] rawPixels = new int[pixels.length];
        // trzeba zrobić kopiowanie tablicy, ponieważ przypisując przypisalibyśmy referencję, czyli każda zmiana tablicy rawPixels zmieniałaby też []pixels
        System.arraycopy(pixels, 0, rawPixels, 0, pixels.length);
        propertiesMap.remove(img);
        // jeśli wybierzemy opcję maxValue z Menu, odzyskujemy pierwotną wartość maxValue i modyfikujemy ją o daną wartość
        if (customVal) {
            if (val <= 1) {
                maxVal = (int) (val * originalMaxVal);
            } else {
                maxVal = (int) val;
            }
        } else {
            //jeśli maxVal = 0, możemy je tylko zwiększyć
            // if (maxVal > 0 || maxVal == 0 && val == 1) {
            maxVal = (int) (maxVal + val);
            if (maxVal <= 0) {
                maxVal = 0;
            }

        }
        // ograniczenie maksymalnej wartości piksela
        propertiesList.set(8, maxVal);
        byte[] outPixels = convertTo8Bit(cutDataToMaxVal(rawPixels, maxVal));
        drawImage(raster.getWidth(), raster.getHeight(), outPixels);
        // ten warunek jest po to, żeby w przypadku gdy mamy wybraną opcję negatywu nastawiać negatyw ponownie przy każdej zmianie maxValue,
        // ponieważ metoda maxValue bazuje na surowych, oryginalnych pikselach, niezmienionych przez opcję negatywu
        if (!negativeOn) {
            propertiesList.set(1, false);
        }

        propertiesMap.put(img, propertiesList);
        replaceImage(t1);
        return maxVal;
    }

    /**
     * Metoda pobierająca pixelSpacing dla danego obrazu
     * @return
     */
    public double getPixelSpacing() {
        return (double) propertiesMap.get(img).get(7);
    }

    /**
     * Metoda zmieniająca paletę kolorów - przypisuje pikselowi o danym indeksie wartości r,g,b o tym samym indeksie z palety kolorów.
     * @param t1 element drzewa wyboru(plik)
     * @param r
     * @param g
     * @param b
     * @param negative
     * @return
     */
    public BufferedImage changeColorPalette(TreeItem<String> t1, int[] r, int[] g, int[] b, boolean negative) {

        List<Object> propertiesList = getProperties(t1);
        int[] pixels = (int[]) propertiesList.get(0);
        //trzeba skopiowac zeby nie modyfikowac tablicy pixels
        int[] rawPixels = new int[pixels.length];
        System.arraycopy(pixels, 0, rawPixels, 0, pixels.length);
        byte[] convertedPixels;
        maxVal = (int) propertiesList.get(8);
        double windowCenter = (double) propertiesList.get(5);
        double windowWidth = (double) propertiesList.get(6);
        convertedPixels = setWindowParameters(rawPixels, maxVal, windowCenter, windowWidth);
        if (negative) {
            modifyPixelsToNegative(convertedPixels);
        }
        BufferedImage image = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        // Modyfikacja wartości dla każdego piksela
        for (int y = 0; y < img.getHeight() - 1; y++) {
            for (int x = 0; x < img.getWidth() - 1; x++) {
                for (int i = -128; i < 129; i++) {
                    if (convertedPixels[y * img.getWidth() + x] == i) {
                        Color color;
                        if (i < 0) {
                            color = new Color(r[i + 255], g[i + 255], b[i + 255]);
                        } else {
                            color = new Color(r[i], g[i], b[i]);
                        }
                        image.setRGB(x, y, color.getRGB());
                        break;
                    }
                }
            }
        }
        return image;
    }

    /**
     * Metoda odwracająca wartości pikseli do negatywu
     * @param pixels
     */
    private void modifyPixelsToNegative(byte[] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = (byte) (-1 - pixels[i]);
        }
    }


    /**
     * Metoda odpowiadająca za negatyw
     * @param t1 wybrany element drzewa(plik)
     * @param negativeRequested informacja czy chcemy wyświetlić obraz w negatywie, czy przywrócić normalny stan
     */
    public void setNegative(TreeItem<String> t1, boolean negativeRequested) {
        findImageForChosenFile(t1);
        if (img == null) {
            return;
        }
        raster = img.getRaster();
        byte[] outPixels = ((DataBufferByte) raster.getDataBuffer()).getData();
        List<Object> propertiesList = propertiesMap.get(img);
        boolean isNegative = (boolean) propertiesList.get(1);
        // dwie opcje - chcemy negatyw i jeszcze go nie mamy albo nie chcemy negatywu i już go mamy - wtedy dokonujemy zmian
        if (negativeRequested && !isNegative || !negativeRequested && isNegative) {
            propertiesMap.remove(img);
            modifyPixelsToNegative(outPixels);
            drawImage(raster.getWidth(), raster.getHeight(), outPixels);
            propertiesList.set(1, !isNegative);
            propertiesMap.put(img, propertiesList);
            replaceImage(t1);

        }
    }


    /**
     * Metoda przyporządkowująca odpowiedni typ danych pozyskanych z surowych informacji zawartych w pliku DICOM.
     * Po dopasowaniu odpowiedniego rodzaju tablicy do pikseli następuje konwersja do tablicy int, w celu ułatwienia dalszych działań.
     *
     * @param raster
     */
    public int[] getPixelDataType(Raster raster) {

        DataBuffer dataBuffer = raster.getDataBuffer();
        int[] pixels = null;
        switch (dataBuffer.getDataType()) {
            case 0: {
                byte[] bytePixels = ((DataBufferByte) dataBuffer).getData();
                pixels = convertArrayToInteger(bytePixels);
                break;

            }
            case 1: {
                short[] uShortPixels = ((DataBufferUShort) dataBuffer).getData();
                pixels = convertArrayToInteger(uShortPixels);
                break;

            }
            case 2: {
                short[] shortPixels = ((DataBufferShort) dataBuffer).getData();
                pixels = convertArrayToInteger(shortPixels);
                break;
            }
        }

        return pixels;

    }

    /**
     * Metoda usuwająca plik obrazowy
     * @param t1
     */
    public void removeImage(TreeItem<String> t1) {
        imgMap.remove(t1);
        propertiesMap.remove(img);

    }


    public void setWindowValues() {
        maxVal = (int) propertiesMap.get(img).get(2);

    }


    /**
     * Metoda znajdująca obraz i listę obrazów dla naciśniętego elementu drzewa.
     *
     * @param t1
     */

    public boolean findImageForChosenFile(TreeItem<String> t1) {
        boolean imageDetected;
        if (imgMap.size() == 0) {
            return false;
        }
        imgList = imgMap.get(t1);
        // naciśnięty element to frame lub uszkodzony plik
        if (imgList == null) {
            imgList = imgMap.get(t1.getParent());
            //sprawdzenie czy plik nie jest uszkodzony
            if (imgList == null) {
                imageDetected = false;
            } else {
                frameIndex = t1.getParent().getChildren().indexOf(t1);
                img = imgList.get(frameIndex);
                setWindowValues();
                imageDetected = true;
            }
        } else {
            imageDetected = true;
            img = imgList.get(0);
            frameIndex = -1;
            setWindowValues();
        }
        return imageDetected;
    }

    /**
     * Metoda podmieniająca zmodyfikowany obraz z poprzednim
     * @param t1
     */
    public void replaceImage(TreeItem<String> t1) {
        // jeśli mamy singleFramed file możemy wyczyścić całą listę, bo ma tylko jeden element
        if (frameIndex == -1) {
            imgList.clear();
            imgList.add(img);
            // jeśli mamy multiFramed file musimy wymienić konkretny indeks w liście
        } else {
            imgList.set(frameIndex, img);
        }
        imgMap.replace(t1, imgList);

    }

    /**
     * Metoda zwracająca maksymalną wartość piksela dla danego obrazu
     * @return
     */
    public int getMaxVal() {
        return (int) propertiesMap.get(img).get(8);
    }

    /**
     * Metoda zwracająca wartości pikseli z tablicy pikseli dla podanych współrzednych kursora
     *
     * @param cursorX
     * @param cursorY
     * @param t1
     * @return
     */
    public double getPixelValue(int cursorY, int cursorX, TreeItem<String> t1) {
        findImageForChosenFile(t1);
        int[] pixels = (int[]) propertiesMap.get(img).get(0);
        return pixels[cursorX + cursorY];
    }

    /**
     * Metoda odczytująca plik DICOM.
     */
    public void getImageReader() {
        try {
            Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
            reader = (ImageReader) iter.next();
            ImageInputStream iis = ImageIO.createImageInputStream(file);
            reader.setInput(iis, false);
            frames = reader.getNumImages(true);
        } catch (IOException ie) {
            ie.printStackTrace();
        }

    }

    public int getFrames() {
        return this.frames;
    }

    public int getFrameIndex() {
        return this.frameIndex;
    }

    public Map getMap() {
        return this.imgMap;
    }

    public double getDefaultWindowWidth(TreeItem<String> t1) {
        findImageForChosenFile(t1);
        return (double) propertiesMap.get(img).get(4);
    }

    public double getDefaultWindowCenter(TreeItem<String> t1) {
        findImageForChosenFile(t1);
        return (double) propertiesMap.get(img).get(3);
    }

    /**
     * Metoda przypisująca surowym pikselom wartości w jednostce HU na podstawie parametrów rescaleSlope, rescaleIntercept
     *
     * @param pixels
     */
    private void setRescaleParameters(int[] pixels) {
        if (rescaleSlope != null && rescaleIntercept != null) {
            double rescaleIntercept = Double.parseDouble(attr.getString(Tag.RescaleIntercept));
            double rescaleSlope = Double.parseDouble(attr.getString(Tag.RescaleSlope));
            for (int j = 0; j < pixels.length; j++) {
                pixels[j] = (int) Math.round(pixels[j] * rescaleSlope + rescaleIntercept);
            }
        }
    }

    /**
     * Metoda obliczająca wartość pikseli w zależności od parametru windowCenter, windowWidth
     *
     * @param pixels
     * @param maxVal
     * @param windowCenter
     * @param windowWidth
     * @return
     */
    private byte[] setWindowParameters(int[] pixels, int maxVal, double windowCenter, double windowWidth) {
        byte[] outPixels = new byte[pixels.length];
        if (windowWidth == 0 && windowCenter == 0) {
            outPixels = convertTo8Bit(cutDataToMaxVal(pixels, maxVal));
            return outPixels;
        }
        // wzory na obliczanie wartości pikseli zgodne z dokumentacją DICOM
        for (int j = 0; j < pixels.length; j++) {
            if (pixels[j] <= windowCenter - 0.5 - (windowWidth - 1) / 2) {
                outPixels[j] = 0;
            } else if (pixels[j] > windowCenter - 0.5 + (windowWidth - 1) / 2) {
                outPixels[j] = (byte) 255;
            } else {
                outPixels[j] = (byte) (((pixels[j] - (windowCenter - 0.5)) / (windowWidth - 1) + 0.5) * 255);
            }
        }
        return outPixels;
    }


    /**
     * Metoda pozyskująca maksymalną wartość piksela z tablicy
     *
     * @param percentVal
     */

    public int getMaximumPixelValue(double percentVal, int[] pixels) {
        int[] newPixels = new int[pixels.length];
        System.arraycopy(pixels, 0, newPixels, 0, pixels.length);
        Arrays.sort(newPixels);
        int maxVal = (int) (percentVal * newPixels[newPixels.length - 1]);
        return maxVal;

    }


    /**
     * Metoda konwertująca dane do finałowej formy - 8bitowej
     */
    public byte[] convertTo8Bit(int[] pixels) {
        int max = 255;
        byte[] outPixels = new byte[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            outPixels[i] = (byte) Math.round(((float) pixels[i] / maxVal) * max);
        }
        return outPixels;
    }

    /**
     * Metoda dopasowująca model kolorów do pikseli oraz tworząca obraz
     *
     * @param width
     * @param height
     */
    public void drawImage(int width, int height, byte[] outPixels) {

        ColorModel colorModel = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_GRAY),
                false,
                false,
                Transparency.OPAQUE,
                DataBuffer.TYPE_BYTE
        );
        DataBuffer buffer = new DataBufferByte(outPixels, outPixels.length);
        SampleModel sm = colorModel.createCompatibleSampleModel(width, height);
        WritableRaster oR = Raster.createWritableRaster(sm, buffer, null);
        img = new BufferedImage(colorModel, oR, false, null);
    }


    /**
     * Metoda odczytująca dane z pliku DICOM oraz rysująca na ich podstawie obraz gotowy do wyświetlenia
     */
    public boolean read(TreeItem<String> t1) {
        try {
            imgList = new ArrayList<>();
            DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();
            //jeśli w mapie nie ma jeszcze naciśniętego pliku
            if (imgMap.containsKey(t1) || frames == 0) {
                return false;
            }
            if (frames == 1) {
                frameIndex = -1;
            }
            for (int i = 0; i < frames; i++) {
                int width = reader.getWidth(i);
                int height = reader.getHeight(i);
                Raster raster = reader.readRaster(i, param);
                int[] rawPixels = getPixelDataType(raster);
                List<Object> propertiesList = new ArrayList<>();
                setRescaleParameters(rawPixels);
                maxVal = getMaximumPixelValue(1, rawPixels);
                propertiesList.add(rawPixels);
                propertiesList.add(false);
                propertiesList.add(maxVal);
                propertiesList.add(windowCenter);
                propertiesList.add(windowWidth);
                //dodajemy drugi raz WC, WW oraz maxVal, aby zachować oryginalne wartości - te pierwsze będziemy modyfikować
                propertiesList.add(windowCenter);
                propertiesList.add(windowWidth);
                propertiesList.add(Double.parseDouble(pixelSpacing));
                propertiesList.add(maxVal);
                propertiesList.add(patientName);
                propertiesList.add(studyDate);
                propertiesList.add(modality);
                propertiesList.add(frames);
                byte[] outPixels = setWindowParameters(rawPixels, maxVal, windowCenter, windowWidth);
                drawImage(width, height, outPixels);
                //img = reader.read(i);
                imgList.add(img);
                propertiesMap.put(img, propertiesList);
            }
            imgMap.put(t1, imgList);

        } catch (IOException ie) {
            GUICreator.getInstance().setPopUpWindow();
        }
        return true;

    }

    /**
     * Metoda odczytująca informacje z pliku DICOM
     */
    public void readTags() {
        DicomInputStream dis = null;
        img = null;
        attr = null;
        try {
            dis = new DicomInputStream(file);
            attr = dis.readDataset(-1, -1);
            String windowCenterString = attr.getString(Tag.WindowCenter);
            String windowWidthString = attr.getString(Tag.WindowWidth);
            if (windowCenterString != null) {
                windowCenter = Double.parseDouble(windowCenterString);
                windowWidth = Double.parseDouble(windowWidthString);
            } else {
                windowCenter = windowWidth = 0;
            }
            rescaleIntercept = attr.getString(Tag.RescaleIntercept);
            rescaleSlope = attr.getString(Tag.RescaleSlope);
            pixelSpacing = attr.getString(Tag.PixelSpacing);
            patientName = attr.getString(Tag.PatientName);
            studyDate = attr.getString(Tag.StudyDate);
            modality = attr.getString(Tag.Modality);
            getImageReader();

        } catch (IOException ie) {
            GUICreator.getInstance().setPopUpWindow();
        } finally {
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public Attributes getAttributes() {
        return attr;
    }


}