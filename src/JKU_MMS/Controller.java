package JKU_MMS;


import JKU_MMS.Database.SQLite;
import JKU_MMS.Model.Model;
import JKU_MMS.Model.Profile;
import JKU_MMS.Model.Task;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
    // switches screen screen to let the user customize a profile
    public Button createProfile;
 // starts the selected task
    public Button startSelectedTask;
    // removes the selected task
    public Button removeSelectedTask;
    // for now the user has to manually remove finished tasks
    // as a further improvement the model could maybe automatically remove them?
    public Button removeFinishedTasks;
    // the table displaying all currently active tasks
    public TableView<Task> taskTable;
    // columns of the taskTable
    public TableColumn<Task, String> fileNameCol;
    public TableColumn<Task, String> profileNameCol;
    public TableColumn<Task, String> progressCol;
    
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
            String input = inputFile.getText();
			FFmpegBuilder builder = new FFmpegBuilder().addInput(input);

            // TODO: add settings etc...
            // TODO: read current settings from model and apply to builder

            Task newTask = new Task(builder, input, model.currentSettings.getName());
            this.model.tasks.add(newTask);
        });

        process.setOnAction(actionEvent -> {
            // TODO: start processing all Tasks in model.tasks
        });

        createProfile.setOnAction(actionEvent -> {
            System.out.print("Pressed");
        });

        chooseProfile.getItems().addAll(SQLite.getProfileNames());
        chooseProfile.getSelectionModel().select(0);
        
        // setting up the table with the tasks
        taskTable.setItems(model.tasks);
        // assign fileName property to fileName columns
        fileNameCol.setCellValueFactory(c -> c.getValue().fileName);
        // same with profileName column
        profileNameCol.setCellValueFactory(c -> c.getValue().profileName);
        // same with progress column
        progressCol.setCellValueFactory(c -> c.getValue().progress);
        
        // just a few test cases, remove them later
        model.tasks.add(new Task(null, "test.mp4", "default"));
        model.tasks.add(new Task(null, "test2.mp4", "android"));
        model.tasks.add(new Task(null, "test3.mp4", "1080p"));
        model.tasks.add(new Task(null, "test4.mp4", "nosubtitles"));
        model.tasks.add(new Task(null, "test5.mp4", "default"));
        model.tasks.add(new Task(null, "test6.mp4", "default"));
        model.tasks.add(new Task(null, "test7.mp4", "default"));
        model.tasks.get(1).progress.setValue("Finished");
        model.tasks.get(4).progress.setValue("Finished");
        model.tasks.get(5).progress.setValue("66%");
        model.tasks.get(2).progress.setValue("50%");
        model.tasks.get(3).progress.setValue("44%");
        
        startSelectedTask.setOnAction(e -> {
        	int idx = taskTable.getSelectionModel().getSelectedIndex();
        	if (idx < 0) {
        		return;
        	}
        	Task task = model.tasks.get(idx);
        	String progress = task.progress.getValue();
        	if (progress.equalsIgnoreCase("not started")) {
				System.out.println("Starting task " + task.fileName.getValue());
				task.progress.setValue("Starting...");
				task.run();
			} else {
				throw new RuntimeException("Can only start unstarted tasks");
			}
        });
        
        removeSelectedTask.setOnAction(e -> {
        	int idx = taskTable.getSelectionModel().getSelectedIndex();
        	if (idx < 0) {
        		return;
        	}
        	Task t = model.tasks.get(idx);
        	if (!(t.progress.getValue().equalsIgnoreCase("not started") || t.progress.getValue().equalsIgnoreCase("finished"))) {
        		// TODO: stop a running task
        		// in case you can not stop a running ffmpeg operation with this wrapper just throw an exception here
        		System.out.println("Stopping task: " + model.tasks.get(idx).fileName.getValue());
        	}
        	System.out.println("Removing task: " + model.tasks.get(idx).fileName.getValue());
        	model.tasks.remove(idx);
        });
        
        removeFinishedTasks.setOnAction(e -> {
        	for (int i = 0; i < model.tasks.size(); i++) {
        		Task t = model.tasks.get(i);
        		if (t.progress.getValue().equalsIgnoreCase("finished")) {
        			model.tasks.remove(t);
        			i--;
        		}
        	}
        });
    }


    public void close() {
        // TODO
    }
}