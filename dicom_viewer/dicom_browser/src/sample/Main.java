package sample;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Klasa uruchamiająca główne okno aplikacji, tworząca kontroler i interfejs graficzny
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        GUICreator guiCreator = GUICreator.getInstance();
        Controller controller = Controller.getInstance();
        guiCreator.createScene(primaryStage);
        guiCreator.setControls(controller);

    }


    public static void main(String[] args) {
        launch(args);
    }
}
