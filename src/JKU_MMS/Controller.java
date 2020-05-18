package JKU_MMS;


import JKU_MMS.Model.Model;
import JKU_MMS.Model.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Controller {

    private final Model model;
    public TextField inputFile;
    public Button fileChooser;
    public Button process;
    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;
    private final FFmpegExecutor fFmpegExecutor;
    /* .env must contain Path to ffmpeg install
     * Windows example: C:\ProgramData\chocolatey\lib\ffmpeg\tools\ffmpeg\bin
     * Linux example: /usr/bin/ffmpeg
     */
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
        }

        try {
            this.ffprobe = new FFprobe(ffprobe_path);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not find ffprobe required for controller");
        }

        if (ffmpeg != null && ffprobe != null) {
            fFmpegExecutor = new FFmpegExecutor(ffmpeg, ffprobe);
        } else {
            throw new IllegalStateException("Controller could not be constructed because ffmpeg or ffprobe could not be found");
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

        process.setOnAction(actionEvent -> {
            FFmpegBuilder builder = new FFmpegBuilder().addInput(inputFile.getText());

            // TODO: add settings etc...

            Task newTask = new Task(builder);
            this.model.tasks.add(newTask);
        });
    }

    public void close() {
        // TODO
    }

}