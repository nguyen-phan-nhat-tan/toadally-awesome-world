package Release;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        CritterWorldController controller = new CritterWorldController();
        controller.setLaunchArguments(getParameters().getRaw());

        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/critterworld.fxml")));
        loader.setController(controller);

        Parent root = loader.load();
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        primaryStage.setTitle("Toadally Awesome World Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        controller.attachScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}