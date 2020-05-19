package JKU_MMS;

import JKU_MMS.Database.ConnectionFailedException;
import JKU_MMS.Database.SQLite;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.SQLException;

public class Main extends Application {

    public static Stage primaryStage;

    public static void main(String[] args) throws SQLException, ConnectionFailedException {
        SQLite.openConnection();
        SQLite.test();
        launch(args);
        SQLite.closeConnection();

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.primaryStage = primaryStage;
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("mainUI.fxml"));
        final Parent root = loader.load();
        Controller controller = loader.getController();

        primaryStage.setTitle("Video Encoder");
        primaryStage.setScene(new Scene(root, 500, 250));
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            controller.close();
        });
    }
}
