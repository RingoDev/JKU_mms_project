package JKU_MMS;

import JKU_MMS.Model.Model;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.File;
import java.io.IOException;

public class Controller {

    private final Model model;
    public TextField inputFile;
    public Button fileChooser;
    public Button process;
    private FFmpeg ffmpeg;
    private FFprobe fFprobe;

    public Controller() {
        this.model = new Model();
        try {
            this.ffmpeg = new FFmpeg("/usr/bin/ffmpeg"); // TODO: set with value from a menu
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to find ffmpeg installation");
        }

        try {
            this.fFprobe = new FFprobe("/usr/bin/ffprobe"); // TODO: set with value from a menu
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Unable to find ffprobe installation");
        }
    }

    @FXML
    private void initialize() {
        fileChooser.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Video File");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File file = fileChooser.showOpenDialog(Main.primaryStage);

            if (file != null) {
                inputFile.setText(file.getAbsolutePath());
            }
        });
    }

    public void close() {
        // TODO
    }
}