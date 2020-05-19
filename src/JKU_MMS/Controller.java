package JKU_MMS;


import JKU_MMS.Database.SQLite;
import JKU_MMS.Model.Model;
import JKU_MMS.Model.Profile;
import JKU_MMS.Model.Task;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.SQLOutput;

public class Controller {

    private final Model model;
    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;
    private final FFmpegExecutor fFmpegExecutor;
    public TextField inputFile;
    // opens a file chooser and lets the user choose a video file
    public Button fileChooser;
    // adds a new tasks to the queue
    public Button addTask;
    // starts processing all tasks enqueued in mode.tasks
    public Button process;
    // dropdown menu which lets user select profile for task
    public ChoiceBox<String> chooseProfile = new ChoiceBox<>();
    // opens a new window to let the user customize a profile
    public Button createProfile;
    public String ffmpeg_path;
    public String ffprobe_path;

    public Controller() throws IOException {
        if (SystemUtils.IS_OS_LINUX) {
            // TODO: set with whereis command
            //ProcessBuilder ffmpegWh = new ProcessBuilder("whereis", "ffmpeg");
            //ProcessBuilder ffprobeWh = new ProcessBuilder("whereis", "ffmpeg");

            ffmpeg_path = "/usr/bin/ffmpeg";
            ffprobe_path = "/usr/bin/ffprobe";
        } else {
            BufferedReader reader = new BufferedReader(new FileReader(
                    Paths.get(".env").toFile()));
            ffmpeg_path = reader.readLine();
            ffprobe_path = reader.readLine();
            reader.close();
        }

        this.model = new Model();
        try {
            this.ffmpeg = new FFmpeg(ffmpeg_path);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not find ffmpeg required for controller");
            // TODO: handle exception and open a popup for the user to set a path
        }

        try {
            this.ffprobe = new FFprobe(ffprobe_path);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not find ffprobe required for controller");
            // TODO: handle exception and open a popup for the user to set a path
        }

        if (ffmpeg != null && ffprobe != null) {
            fFmpegExecutor = new FFmpegExecutor(ffmpeg, ffprobe);
        } else {
            throw new IllegalStateException("Controller could not be constructed because ffmpeg or ffprobe could not be found");
        }
    }

    @FXML
    private void initialize() throws SQLException {
        fileChooser.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Video File");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File file = fileChooser.showOpenDialog(Main.window);

            if (file != null) {
                inputFile.setText(file.getAbsolutePath());
            }
        });

        addTask.setOnAction(actionEvent -> {
            FFmpegBuilder builder = new FFmpegBuilder().addInput(inputFile.getText());

            // TODO: add settings etc...
            // TODO: read current settings from model and apply to builder

            Task newTask = new Task(builder);
            this.model.tasks.add(newTask);
        });

        process.setOnAction(actionEvent -> {
            // TODO: start processing all Tasks in model.tasks
        });

        createProfile.setOnAction(actionEvent -> {
            //TODO set new scene to choose profile
            System.out.print("Pressed");
        });

        chooseProfile.getItems().addAll(SQLite.getProfileNames());
        chooseProfile.getSelectionModel().select(0);
    }

    public void close() {
        // TODO
    }

}