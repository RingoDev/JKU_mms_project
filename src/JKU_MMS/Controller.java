package JKU_MMS;


import JKU_MMS.Database.SQLite;
import JKU_MMS.Model.Model;
import JKU_MMS.Model.Profile;
import JKU_MMS.Model.Task;
import JKU_MMS.Settings.Codec;
import JKU_MMS.Settings.Format;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
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
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;


public class Controller {

    public static FFmpeg ffmpeg;
    public static FFprobe ffprobe;
    public static FFmpegExecutor fFmpegExecutor;
    private final Model model;
    public TextField inputFile;
    public TextField outputPath;
    public TextField videoWidth;
    public TextField videoHeight;
    public TextField frameRate;
    // opens a file chooser and lets the user choose a video file
    public Button inputChooser;
    // adds a new tasks to the queue
    public Button addTask;
    // starts processing all tasks enqueued in mode.tasks
    public Button process;
    // dropdown menu which lets user select profile for task
    public ChoiceBox<Profile> chooseProfile = new ChoiceBox<>();
    // dropdown menu which lets user select VideoCodec for the task
    public ChoiceBox<Codec> chooseVideoCodec = new ChoiceBox<>();
    // dropdown menu which lets user select AudioCodec for task
    public ChoiceBox<Codec> chooseAudioCodec = new ChoiceBox<>();
    // dropdown menu which lets user select Format for task
    public ChoiceBox<Format> chooseFormat = new ChoiceBox<>();
    // opens a directory chooser and lets the user define the outputFolder
    public Button outputChooser;
    // starts the selected task
    public Button startSelectedTask;
    // removes the selected task
    public Button removeSelectedTask;
    // create profile button
    public Button createProfile;
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
    // textFields for settings
    public TextField bitrateText, samplerateText, newProfileName;
    private Thread ffmpegTask;

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

        try {
            profileMap = SQLite.getAllProfiles();
        } catch (SQLException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private static int integerChanged(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return -1;
        }
    }

    private static double doubleChanged(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return -1.0;
        }
    }

    @FXML
    private void initialize() throws SQLException {
        outputPath.setText(model.currentSettings.getOutputPath().toString());

        outputPath.textProperty().addListener((observable, oldValue, newValue) -> model.currentSettings.setOutputPath(Paths.get(newValue)));

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

        chooseProfile.setConverter(new StringConverter<>() {

            @Override
            public String toString(Profile profile) {
                if (profile == null) return null;
                return profile.toString();
            }

            @Override
            public Profile fromString(String string) {
                return profileMap.get(string);
            }
        });
        chooseProfile.getItems().addAll(profileMap.values().stream().filter(Profile::isCustom).collect(Collectors.toList()));
        //items.add(new Separator());
        chooseProfile.getItems().addAll( profileMap.values().stream().filter(p -> !p.isCustom()).collect(Collectors.toList()));
        // display the first Value in list as standard select
        chooseProfile.getSelectionModel().select(0);
        // adding saved VideoCodecs to the ChoiceBox
        chooseVideoCodec.getItems().add(new Codec("auto"));
        chooseVideoCodec.getItems().add(new Codec("copy"));
        chooseVideoCodec.getItems().addAll(SQLite.getVideoCodecs());
        // display the first Value in list as standard select
        chooseVideoCodec.getSelectionModel().select(0);
        // adding saved AudioCodecs to the ChoiceBox
        chooseAudioCodec.getItems().add(new Codec("auto"));
        chooseAudioCodec.getItems().add(new Codec("copy"));
        chooseAudioCodec.getItems().addAll(SQLite.getAudioCodecs());
        // display the first Value in list as standard select
        chooseAudioCodec.getSelectionModel().select(0);
        // adding available Formats to the ChoiceBox
        chooseFormat.getItems().add(new Format("auto"));
        chooseFormat.getItems().add(new Format("copy"));
        chooseFormat.getItems().addAll(SQLite.getFormats());
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
        bitrateText.textProperty().addListener(settingsChangedListener);
        videoWidth.textProperty().addListener(settingsChangedListener);
        videoHeight.textProperty().addListener(settingsChangedListener);
        frameRate.textProperty().addListener(settingsChangedListener);
        samplerateText.textProperty().addListener(settingsChangedListener);

        // if settings change set them directly in the model
        chooseAudioCodec.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> model.currentSettings.setAudioCodec(t1));
        chooseVideoCodec.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> model.currentSettings.setVideoCodec(t1));
        chooseFormat.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> model.currentSettings.setFormat(t1));
        audioButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> model.currentSettings.setRemoveAudio(t1));
        subtitlesButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> model.currentSettings.setRemoveSubtitles(t1));
        frameRate.textProperty().addListener((observableValue, s, t1) -> model.currentSettings.setVideoFrameRate(doubleChanged(t1)));
        videoWidth.textProperty().addListener((observableValue, s, t1) -> model.currentSettings.setVideoWidth(integerChanged(t1)));
        videoHeight.textProperty().addListener((observableValue, s, t1) -> model.currentSettings.setVideoHeight(integerChanged(t1)));

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
                // in case you can not stop a running FFmpeg operation with this wrapper just throw an exception here
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

        createProfile.setOnAction(event -> {
            String name = newProfileName.getText();
            if (name.isEmpty()) throw new RuntimeException("Profile name cannot be blank");
            Profile newProfile = getProfile();
            // we should allow double Settings for Simplicity
            //if (profileMap.containsValue(newProfile)) throw new RuntimeException("Profile with these settings already exists");
            if (profileMap.containsKey(name)) throw new RuntimeException("Profile with this name already exists");

            profileMap.put(name, newProfile);

            try {
                SQLite.addProfile(newProfile);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            chooseProfile.getItems().add(0, newProfile);
            chooseProfile.getSelectionModel().select(newProfile);
            newProfileName.setText("");
            chooseProfile.getItems().remove("Custom");
            System.out.println("Saved profile as " + name);
        });


        // TextFormatter that only permits non-negativ Integers as Values
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("([1-9][0-9]*)?")) {
                return change;
            }
            return null;
        };

        videoHeight.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), 0, integerFilter));
        videoWidth.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), 0, integerFilter));
        samplerateText.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), 0, integerFilter));
        videoWidth.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), 0, integerFilter));
        bitrateText.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), 0, integerFilter));
        frameRate.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), 0, integerFilter));
    }

    public void close() {
        // TODO join ffmpegTask thread with timeout
    }

    public void settingsChanged() {
        System.out.println("Settings were changed");

        // what does this part do?


//    	Profile curProfile = getProfile();
//    	Map<String,Profile> map = profileMap;
//
//
//    	if (map.containsValue(curProfile)) {
//    		Profile p = null;
//    		String name = null;
//    		for (Map.Entry<String, Profile> e : map.entrySet()) {
//    			if (e.getValue().equals(curProfile)) {
//    				p = e.getValue();
//    				name = e.getKey();
//    				break;
//    			}
//    		}
//    		chooseProfile.getItems().remove("Custom");
//    		chooseProfile.getSelectionModel().select(name);
//    	} else {
//    		if (!chooseProfile.getItems().contains("Custom")) chooseProfile.getItems().add("Custom");
//    		chooseProfile.getSelectionModel().select("Custom");
//    	}
    }

    public Profile getProfile() {
        String profileName = newProfileName.getText();
        Format format = chooseFormat.getValue();
        Codec videoCodec = chooseVideoCodec.getValue();
        Codec audioCodec = chooseAudioCodec.getValue();
        boolean removeSubtitles = subtitlesButton.selectedProperty().get();
        boolean removeAudio = audioButton.selectedProperty().get();
        int samplerate = samplerateText.getText().isEmpty() ? -1 : Integer.parseInt(samplerateText.getText());
        int width = videoWidth.getText().isEmpty() ? -1 : Integer.parseInt(videoWidth.getText());
        int height = videoHeight.getText().isEmpty() ? -1 : Integer.parseInt(videoHeight.getText());
        int bitrate = bitrateText.getText().isEmpty() ? -1 : Integer.parseInt(bitrateText.getText());
        double framerate = frameRate.getText().isEmpty() ? -1 : Double.parseDouble(frameRate.getText());

        Profile p = new Profile(profileName);
        p.setFormat(format);
        p.setVideoCodec(videoCodec);
        p.setAudioCodec(audioCodec);
        p.setRemoveSubtitles(removeSubtitles);
        p.setRemoveAudio(removeAudio);
        p.setAudioSampleRate(samplerate);
        p.setVideoWidth(width);
        p.setVideoHeight(height);
        p.setAudioBitRate(bitrate);
        p.setVideoFrameRate(framerate);

        return p;
    }

    public void profileChanged() {
        Profile selectedProfile = chooseProfile.getSelectionModel().getSelectedItem();
        System.out.println("Profile was changed to " + selectedProfile.getName());

        chooseFormat.getSelectionModel().select(selectedProfile.getFormat());
        chooseVideoCodec.getSelectionModel().select(selectedProfile.getVideoCodec());
        chooseAudioCodec.getSelectionModel().select(selectedProfile.getAudioCodec());
        subtitlesButton.setSelected(selectedProfile.removeSubtitles());
        audioButton.setSelected(selectedProfile.removeAudio());
        samplerateText.setText(Integer.toString(selectedProfile.getAudioSampleRate()));
        videoWidth.setText(Integer.toString(selectedProfile.getVideoWidth()));
        videoHeight.setText(Integer.toString(selectedProfile.getVideoHeight()));
        bitrateText.setText(Integer.toString(selectedProfile.getAudioBitRate()));
        frameRate.setText(Integer.toString((int) selectedProfile.getVideoFrameRate()));
        //TODO handle double Text Data (framerate)

    }
}