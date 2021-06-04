package sample;


import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import java.io.File;


/**
 * Klasa tworząca pacjenta i przypisująca mu wartości odczytane z pliku DICOM
 */
public class Patient {

    public void setFile(File file){this.file=file;}
    public File getFile(){return this.file;}
    public String getPatientID() {
        return this.patientID;
    }
    public String getStudyInstanceUID() {
        return this.studyInstanceUID;
    }
    public String getSeriesInstanceUID() {
        return this.seriesInstanceUID;
    }
    private String patientID;
    private String studyInstanceUID;
    private String seriesInstanceUID;
    private File file;
    public String getSeriesInstance() {
        return this.seriesInstance;
    }
    private String seriesInstance;
    public String getStudyInstance() {
        return this.studyInstance;
    }
    public String getPatientName() {
        return this.patientName;
    }
    private String studyInstance;
    private String patientName;
    private String instanceNumber;
    public String getFileName() {
        return this.fileName;
    }
    public int getInstanceNumber(){
        return Integer.parseInt(this.instanceNumber);
    }
    private String fileName;

    public Patient(File file) {
        this.file = file;
        DicomReader dicomReader = DicomReader.getInstance();
        dicomReader.readFile(file);
        dicomReader.readTags();
        Attributes attributes = dicomReader.getAttributes();
        if (attributes != null) {
            patientID = attributes.getString(Tag.PatientID);
            studyInstanceUID = attributes.getString(Tag.StudyInstanceUID);
            seriesInstanceUID = attributes.getString(Tag.SeriesInstanceUID);
            seriesInstance = attributes.getString(Tag.SeriesDescription);
            studyInstance = attributes.getString(Tag.StudyDescription);
            patientName = attributes.getString(Tag.PatientName);
            instanceNumber = attributes.getString(Tag.InstanceNumber);
            fileName = file.getName();
            if(seriesInstance==null){
                seriesInstance="unspecified";
            }

        }
    }



}
