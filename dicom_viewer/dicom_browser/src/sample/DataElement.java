package sample;

import javafx.beans.property.SimpleStringProperty;

public class DataElement {

    private final SimpleStringProperty id;
    private final SimpleStringProperty vr;
    private final SimpleStringProperty vm;
    private final SimpleStringProperty length;
    private final SimpleStringProperty desc;
    private final SimpleStringProperty val;


    public DataElement(String id, String vr, String vm, String length, String desc, String val){
        this.id = new SimpleStringProperty(id);
        this.vr = new SimpleStringProperty(vr);
        this.vm = new SimpleStringProperty(vm);
        this.length = new SimpleStringProperty(length);
        this.desc = new SimpleStringProperty(desc);
        this.val = new SimpleStringProperty(val);
    }

    public String getId(){
        return id.get();
    }
    public String getVr(){
        return vr.get();
    }
    public String getVm(){
        return vm.get();
    }
    public String getLength(){
        return length.get();
    }
    public String getDesc(){
        return desc.get();
    }
    public String getVal(){ return val.get();
    }

}
