import Controller.Controller;
import FileHandler.FileHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println(getClass().getResource("/codeEditor.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/codeEditor.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        FileHandler fh = new FileHandler(controller.fileTree, controller);

        Scene scene = new Scene(root ,1280, 720);
        scene.getStylesheets().add(getClass().getResource("/css/ideTheme.css").toExternalForm());

        stage.setTitle("FX-IDE");
        stage.setScene(scene);
        stage.show();

        fh.addAllToOpenRecentMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}