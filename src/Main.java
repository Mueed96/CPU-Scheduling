
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        Controller controller = new Controller();
        Scene scene = new Scene(controller.getRoot(), 800, 600);

        primaryStage.setTitle("Advanced CPU Scheduling Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
