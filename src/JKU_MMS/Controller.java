package JKU_MMS;


import JKU_MMS.Database.SQLite;
import JKU_MMS.Model.Model;
import JKU_MMS.Model.Profile;
import JKU_MMS.Model.Task;
import JKU_MMS.Settings.Codec;
import JKU_MMS.Settings.Format;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Collectors;


public class Controller {

    private final Model model;
    private Thread ffmpegTask;
    public static FFmpeg ffmpeg;
    public static FFprobe ffprobe;
    public static FFmpegExecutor fFmpegExecutor;
    public TextField inputFile;
    public TextField outputPath;
    // opens a file chooser and lets the user choose a video file
    public Button inputChooser;
    // adds a new tasks to the queue
    public Button addTask;
    // starts processing all tasks enqueued in mode.tasks
    public Button process;
    // dropdown menu which lets user select profile for task
    public ChoiceBox<String> chooseProfile = new ChoiceBox<>();
    // dropdown menu which lets user select VideoCodec for the task
    public ChoiceBox<String> chooseVideoCodec = new ChoiceBox<>();
    // dropdown menu which lets user select AudioCodec for task
    public ChoiceBox<String> chooseAudioCodec = new ChoiceBox<>();
    // dropdown menu which lets user select Format for task
    public ChoiceBox<String> chooseFormat = new ChoiceBox<>();
    // opens a directory chooser and lets the user define the outputFolder
    public Button outputChooser;
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
    // Buttons for removing subtitles and audio
    public RadioButton subtitlesButton;
    public RadioButton audioButton;
    
    public String ffmpeg_path;
    public String ffprobe_path;
    // Holds all the Profiles so we don't have to search the Database every time
    public Map<String, Profile> profileMap;

    public Controller() throws IOException {
        this.model = new Model();

        if (SystemUtils.IS_OS_LINUX) {
            // TODO: set with whereis command
            //ProcessBuilder ffmpegWh = new ProcessBuilder("whereis", "ffmpeg");
            //ProcessBuilder ffprobeWh = new ProcessBuilder("whereis", "ffmpeg");

            ffmpeg_path = "/usr/bin/ffmpeg";
            ffprobe_path = "/usr/bin/ffprobe";
            model.currentSettings.setOutputPath(Paths.get("/tmp"));
        } else {
            BufferedReader reader = new BufferedReader(new FileReader(
                    Paths.get(".env").toFile()));
            ffmpeg_path = reader.readLine();
            ffprobe_path = reader.readLine();
            reader.close();

            //TODO createDirectory when Task is started, not when application is opened.
            model.currentSettings.setOutputPath(Files.createTempDirectory("encoded_tmp-"));
        }

        try {
            ffmpeg = new FFmpeg(ffmpeg_path);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not find ffmpeg required for controller");
            // TODO: handle exception and open a popup for the user to set a path
        }

        try {
            ffprobe = new FFprobe(ffprobe_path);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could not find ffprobe required for controller");
            // TODO: handle exception and open a popup for the user to set a path
        }
        fFmpegExecutor = new FFmpegExecutor(ffmpeg, ffprobe);

        try{
            profileMap = SQLite.getAllProfiles();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() throws SQLException {
        outputPath.setText(model.currentSettings.getOutputPath().toString());

        outputPath.textProperty().addListener((observable, oldValue, newValue) -> {
            model.currentSettings.setOutputPath(Paths.get(newValue));
        });

        inputChooser.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Video File");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File file = fileChooser.showOpenDialog(Main.window);

            if (file != null) {
                inputFile.setText(file.getAbsolutePath());
            }
        });

        addTask.setOnAction(actionEvent -> {
            String filePath = inputFile.getText();

            try {
                this.model.tasks.add(Task.of(filePath, model.currentSettings, true));
            } catch (IOException e) {
                //TODO create PopUp Window with ErrorMessage
                e.printStackTrace();
                System.err.println("Unable to create task for " + filePath + " because the file could not be accessed");
            }
        });

        process.setOnAction(actionEvent -> {
            // TODO: start processing all Tasks in model.tasks
        });

        outputChooser.setOnAction(actionEvent -> {
            DirectoryChooser outputChooser = new DirectoryChooser();
            outputChooser.setTitle("Select Output Folder");
            outputChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File selectedDirectory = outputChooser.showDialog(Main.window);
            if (selectedDirectory != null) {
                outputPath.setText(selectedDirectory.getAbsolutePath());
            }
        });

        //TODO when Profile is selected, display corresponding Settings as Standard

        // adding saved Profiles to the ChoiceBox
        chooseProfile.getItems().addAll(SQLite.getProfileNames());
        // display the first Value in list as standard select
        chooseProfile.getSelectionModel().select(0);
        // adding saved VideoCodecs to the ChoiceBox
        chooseVideoCodec.getItems().addAll(SQLite.getVideoCodecs().stream().map(Codec::toString).collect(Collectors.toList()));
        // display the first Value in list as standard select
        chooseVideoCodec.getSelectionModel().select(0);
        // adding saved AudioCodecs to the ChoiceBox
        chooseAudioCodec.getItems().addAll(SQLite.getAudioCodecs().stream().map(Codec::toString).collect(Collectors.toList()));
        // display the first Value in list as standard select
        chooseAudioCodec.getSelectionModel().select(0);
        // adding available Formats to the ChoiceBox
        chooseFormat.getItems().addAll(SQLite.getFormats().stream().map(Format::toString).collect(Collectors.toList()));
        // display the first Value in list as standard select
        chooseFormat.getSelectionModel().select(0);

        // add listener to chooseProfile
        ChangeListener<Object> profileChangedListener = (observable, oldValue, newValue) -> profileChanged();
        chooseProfile.getSelectionModel().selectedItemProperty().addListener(profileChangedListener);
        // add listeners to settings
        ChangeListener<Object> settingsChangedListener = (observable, oldValue, newValue) -> settingsChanged();
		chooseAudioCodec.getSelectionModel().selectedItemProperty().addListener(settingsChangedListener);
        chooseVideoCodec.getSelectionModel().selectedItemProperty().addListener(settingsChangedListener);
        audioButton.selectedProperty().addListener(settingsChangedListener);
        subtitlesButton.selectedProperty().addListener(settingsChangedListener);
        
        // setting up the table with the tasks
        taskTable.setItems(model.tasks);
        // assign fileName property to fileName columns
        fileNameCol.setCellValueFactory(c -> c.getValue().fileName);
        // same with profileName column
        profileNameCol.setCellValueFactory(c -> c.getValue().profileName);
        // same with progress column
        progressCol.setCellValueFactory(c -> c.getValue().progress);
        
        startSelectedTask.setOnAction(e -> {
        	int idx = taskTable.getSelectionModel().getSelectedIndex();
        	if (idx < 0) {
        		return;
        	}
        	Task task = model.tasks.get(idx);
        	String progress = task.progress.getValue();
        	if (progress.equalsIgnoreCase("not started")) {
				if (ffmpegTask != null) {
                    if (ffmpegTask.isAlive()) {
                        System.err.println("Cant start another tasks while one is being processed");
                        return;
                    } else {
                        try {
                            ffmpegTask.join();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                System.out.println("Starting task " + task.fileName.getValue());
                task.progress.setValue("Starting...");

                ffmpegTask = new Thread(task);
                ffmpegTask.start();
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
        // TODO join ffmpegTask thread with timeout
    }
    
    public void settingsChanged() {
    	System.out.println("Settings were changed");
    	Profile curProfile = getProfile();
    	Map<String,Profile> map = profileMap;

    	// TODO check if current settings match a profile in the database
    	// if yes: change chooseProfile to the name of the profile (and remove "Custom" from the ComboBox options if necessary)
    	// if no: change chooseProfile to "Custom" (add "Custom" to the ComboBox options if necessary)
	}
    
    public Profile getProfile() {
    	// TODO: create a profile object from all currently selected settings
    	return null;
    }
    
    public void profileChanged() {
    	System.out.println("Profile was changed to " + profileMap.get(chooseProfile.getValue()));

		// TODO fill all ComboBoxes and RadioButtons with the settings of the newly selected profile
	}
}