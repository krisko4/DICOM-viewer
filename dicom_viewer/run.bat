@echo off
java --module-path "javafx-sdk-11.0.2\lib" --add-modules=javafx.controls,javafx.media,javafx.graphics,javafx.fxml,javafx.swing,dcm4che.audit,dcm4che.core,dcm4che.image,dcm4che.imageio,dcm4che.imageio.rle,dcm4che.net,dcm4che.soundex,jai.imageio,fontawesomefx,pixelmed -jar dicom_viewer.jar
pause