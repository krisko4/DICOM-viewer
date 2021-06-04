package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.TagUtils;
import org.w3c.dom.Attr;

import java.util.List;
import java.util.Map;

public class PatientInfoView {


    private TableView tableView = new TableView();
    private TableColumn idCol = new TableColumn("ID");
    private TableColumn vrCol = new TableColumn("VR");
    private TableColumn vmCol = new TableColumn("VM");
    private TableColumn lengthCol = new TableColumn("Length");
    private TableColumn descCol =new TableColumn("Description");
    private TableColumn valCol = new TableColumn("Value");
    private Pane root, tablePane;
    private List<String> tagList;
    private Attributes attr;




    PatientInfoView(Attributes attributes){
        this.attr = attributes;
        setLayout();
        setTableView();

    }


    public void setLayout(){
        Stage stage = new Stage();
        root  = new Pane();
        root.setPrefWidth(600);
        root.setPrefHeight(600);
        root.setStyle("-fx-background-color: red");
        tablePane = new Pane();
        tablePane.setPrefHeight(500);
        tablePane.setPrefWidth(500);
        tablePane.setLayoutX(50);
        tablePane.setLayoutY(50);
        root.getChildren().add(tablePane);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    public void setTableView(){
        tableView.setPrefWidth(tablePane.getPrefWidth());
        tableView.setPrefHeight(tablePane.getPrefHeight());
        tablePane.getChildren().add(tableView);
        idCol.setCellValueFactory(new PropertyValueFactory<DataElement,String>("id"));
        vrCol.setCellValueFactory(new PropertyValueFactory<DataElement,String>("vr"));
        vmCol.setCellValueFactory(new PropertyValueFactory<DataElement,String>("vm"));
        lengthCol.setCellValueFactory(new PropertyValueFactory<DataElement,String>("length"));
        descCol.setCellValueFactory(new PropertyValueFactory<DataElement,String>("desc"));
        valCol.setCellValueFactory(new PropertyValueFactory<DataElement,String>("val"));
        tableView.getColumns().addAll(idCol,vrCol,vmCol,lengthCol,descCol,valCol);
        ObservableList<DataElement> data = FXCollections.observableArrayList();
        int[] tags = attr.tags();

        for(int i = 0 ; i < tags.length ; i++) {
            data.add(new DataElement(TagUtils.toString(tags[i]),attr.getVR(tags[i]).name(), "0", "0", "0", attr.getString(tags[i])));
        }
        tableView.setItems(data);

    }


}
