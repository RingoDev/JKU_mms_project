package JKU_MMS;

import JKU_MMS.Database.ConnectionFailedException;
import JKU_MMS.Database.SQLite;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


import java.io.IOException;
import java.sql.SQLException;

public class Main extends Application {

    public static Stage window;
    public static Scene mainScene;

    public static void main(String[] args) throws SQLException, ConnectionFailedException, IOException {
        SQLite.openConnection();
        SQLite.test();
        launch(args);
        SQLite.closeConnection();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.window = primaryStage;
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("mainUI.fxml"));
        final Parent root = loader.load();
        Controller controller = loader.getController();

        primaryStage.setTitle("Video Encoder");
        primaryStage.getIcons().add(new Image("file:icon.png"));
        mainScene = new Scene(root, 800, 400);
        primaryStage.setScene(mainScene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> {
            controller.close();
        });
    }
}
