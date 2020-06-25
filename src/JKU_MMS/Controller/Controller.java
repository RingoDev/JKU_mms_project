package JKU_MMS.Controller;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SystemUtils;

import JKU_MMS.Main;
import JKU_MMS.Database.SQLite;
import JKU_MMS.Model.Model;
import JKU_MMS.Model.Profile;
import JKU_MMS.Model.Task;
import JKU_MMS.Model.Settings.Codec;
import JKU_MMS.Model.Settings.Format;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;


public class Controller {

    public static FFmpeg ffmpeg;
    public static FFprobe ffprobe;
    public static FFmpegExecutor fFmpegExecutor;
    private final Model model;
    private static boolean executeQueue;

    // General Options

    //the main Container
    public VBox main;

    // opens a file chooser and lets the user choose a video file
    public Button inputChooser;
    public TextField inputFile;
    // opens a directory chooser and lets the user define the outputFolder
    public Button outputChooser;
    public TextField outputPath;

    // Conversion Options

    // dropdown menu which lets user select profile for task
    public ChoiceBox<Object> chooseProfile;
    // dropdown menu which lets user select VideoCodec for the task
    public ComboBox<Codec> chooseVideoCodec;
    // dropdown menu which lets user select AudioCodec for task
    public ComboBox<Codec> chooseAudioCodec;
    // dropdown menu which lets user select Format for task
    public ComboBox<Format> chooseFormat;

    // Fields for settings
    public TextField bitrateText, samplerateText, newProfileName, videoWidth, videoHeight, frameRate;
    public RadioButton subtitlesButton, audioButton;

    // create profile button
    public Button createProfile;

    // Tasks

    // adds a new tasks to the queue
    public Button addTask;
    // starts processing all tasks enqueued in mode.tasks
    public Button process;
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

    public String ffmpeg_path;
    public String ffprobe_path;

    // Data

    // Holds settings data from database
    public Map<String, Profile> profileMap;
    public ObservableList<Object> profiles;
    public Set<Codec> videoCodecs;
    public Set<Codec> audioCodecs;
    public Set<Format> formats;

    private Thread ffmpegTask;

    // Listeners
    private final ChangeListener<Object> profileChangedListener;
    private final ChangeListener<Object> settingsChangedListener;

    public Controller() throws IOException {
        profiles = FXCollections.observableArrayList();
        this.model = new Model();
        profileChangedListener = (observable, oldValue, newValue) -> profileChanged();
        settingsChangedListener = (observable, oldValue, newValue) -> settingsChanged();

        // set path to ffmpeg according to os
        if (SystemUtils.IS_OS_LINUX) {
            ffmpeg_path = "/usr/bin/ffmpeg";
            ffprobe_path = "/usr/bin/ffprobe";
            model.currentSettings.setOutputPath(Paths.get("/tmp"));
        } else {
            BufferedReader reader = new BufferedReader(new FileReader(
                    Paths.get(".env").toFile()));
            ffmpeg_path = reader.readLine();
            ffprobe_path = reader.readLine();
            reader.close();
        }

        // initialise ffmpeg
        try {
            ffmpeg = new FFmpeg(ffmpeg_path);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not find ffmpeg", ButtonType.OK);
            alert.show();
            throw new IllegalStateException("Could not find ffmpeg required for controller");
        }

        // initialise ffprobe
        try {
            ffprobe = new FFprobe(ffprobe_path);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not find ffprobe", ButtonType.OK);
            alert.show();
            throw new IllegalStateException("Could not find ffprobe required for controller");
        }
        fFmpegExecutor = new FFmpegExecutor(ffmpeg, ffprobe);

        // update profile options
        grabOptionsFromDB();
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
    private void initialize() {
        outputPath.setText(model.currentSettings.getOutputPath().toString());

        outputPath.textProperty().addListener((observable, oldValue, newValue) -> model.currentSettings.setOutputPath(Paths.get(newValue)));

        // let the user choose a file when he clicks the button "Browse" next to the video path
        inputChooser.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Video File");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File file = fileChooser.showOpenDialog(Main.window);

            if (file != null) {
                setInputFile(file.getAbsolutePath());
            }
        });

        // create a new task when the User clicks on the "add Task" button
        addTask.setOnAction(actionEvent -> {
            String filePath = inputFile.getText();

            try {
                this.model.tasks.add(Task.of(filePath, (Profile) chooseProfile.getSelectionModel().getSelectedItem(), true));
                Files.createDirectories(Paths.get(outputPath.getText()));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "File '" + filePath + "' could not be found", ButtonType.OK);
                alert.showAndWait();
            }
        });

        // start processing the Queue when the user presses the process Queue button
        process.setOnAction(actionEvent -> {
            executeQueue = true;
            if (fFmpegExecutor == null) {
                this.startNextTask();
            }
        });

        // let the user choose an output folder if he clicks the "Browse" button next to the output path
        outputChooser.setOnAction(actionEvent -> {
            DirectoryChooser outputChooser = new DirectoryChooser();
            outputChooser.setTitle("Select Output Folder");
            outputChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File selectedDirectory = outputChooser.showDialog(Main.window);
            if (selectedDirectory != null) {
                outputPath.setText(selectedDirectory.getAbsolutePath());
            }
        });

        setChoiceBoxConverters();

        fillChoiceBoxes();

        // if settings change set them directly in the model
        chooseAudioCodec.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> model.currentSettings.setAudioCodec(t1));
        chooseVideoCodec.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> model.currentSettings.setVideoCodec(t1));
        chooseFormat.getSelectionModel().selectedItemProperty().addListener((observableValue, s, t1) -> model.currentSettings.setFormat(t1));
        audioButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> model.currentSettings.setRemoveAudio(t1));
        subtitlesButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> model.currentSettings.setRemoveSubtitles(t1));
        frameRate.textProperty().addListener((observableValue, s, t1) -> model.currentSettings.setVideoFrameRate(doubleChanged(t1)));
        videoWidth.textProperty().addListener((observableValue, s, t1) -> model.currentSettings.setVideoWidth(integerChanged(t1)));
        videoHeight.textProperty().addListener((observableValue, s, t1) -> model.currentSettings.setVideoHeight(integerChanged(t1)));
        bitrateText.textProperty().addListener((observableValue, s, t1) -> model.currentSettings.setAudioBitRate(integerChanged(t1)));
        samplerateText.textProperty().addListener((observableValue, s, t1) -> model.currentSettings.setAudioSampleRate(integerChanged(t1)));

        // setting up the table with the tasks
        taskTable.setItems(model.tasks);
        // assign fileName property to fileName columns
        fileNameCol.setCellValueFactory(c -> c.getValue().fileName);
        // same with profileName column
        profileNameCol.setCellValueFactory(c -> c.getValue().profileName);
        // same with progress column
        progressCol.setCellValueFactory(c -> c.getValue().progress);

        // Start the task which is currently selected
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

                task.setCompletionListener((t, e1) -> {
                    if (e1 != null) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Settings -> Error executing task " + task.getError().getMessage(), ButtonType.OK);
                            alert.show();
                        });
                    }

                    if (executeQueue) {
                        startNextTask();
                    }
                });

                ffmpegTask = new Thread(task);
                ffmpegTask.start();
            } else {
                throw new RuntimeException("Can only start unstarted tasks");
            }
        });

        // remove the task which is currently selected (only if its not running!)
        removeSelectedTask.setOnAction(e -> {
            int idx = taskTable.getSelectionModel().getSelectedIndex();
            if (idx < 0) {
                return;
            }
            Task t = model.tasks.get(idx);
            if (!(t.progress.getValue().equalsIgnoreCase("not started") || t.progress.getValue().equalsIgnoreCase("finished") || t.progress.getValue().equalsIgnoreCase("error"))) {
                System.out.println("Can't stop a running task");
                return;
            }
            System.out.println("Removing task: " + model.tasks.get(idx).fileName.getValue());
            model.tasks.remove(idx);
        });

        // remove all finished tasks
        removeFinishedTasks.setOnAction(e -> {
            for (int i = 0; i < model.tasks.size(); i++) {
                Task t = model.tasks.get(i);
                if (t.progress.getValue().equalsIgnoreCase("finished")) {
                    model.tasks.remove(t);
                    i--;
                }
            }
        });

        // Create a new profile from the current settings and save it to the db
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
            newProfileName.setText("");
            // temporarily remove the listener because we don't want profileChanged() to be called here
            chooseProfile.getSelectionModel().selectedItemProperty().removeListener(profileChangedListener);
            // remove the custom profile
            profiles.remove(model.currentSettings);
            // add the new profile and select it
            profiles.add(0, newProfile);
            chooseProfile.getSelectionModel().select(newProfile);
            // re-add the listener
            chooseProfile.getSelectionModel().selectedItemProperty().addListener(profileChangedListener);
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
                new TextFormatter<>(new IntegerStringConverter(), null, integerFilter));
        videoWidth.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), null, integerFilter));
        samplerateText.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), null, integerFilter));
        videoWidth.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), null, integerFilter));
        bitrateText.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), null, integerFilter));
        frameRate.setTextFormatter(
                new TextFormatter<>(new IntegerStringConverter(), null, integerFilter));

        chooseProfile.getSelectionModel().selectedItemProperty().addListener(profileChangedListener);
        // display the first Value in list as standard select
        // this fires the profileChanged listener which fills up the settings and adds the settingsChanged listener to each component afterwards
        Object item = profiles.get(0) instanceof Separator ? profiles.get(1) : profiles.get(0);
        chooseProfile.getSelectionModel().select(item);

        addDragAndDrop();
    }

    /**
     * Start the next task in the Queue (if it is being processed)
     */
    private void startNextTask() {
        int next_task = 0;
        for (int i = 0; i < model.tasks.size(); i++) {
            Task t = model.tasks.get(i);
            if (!t.progress.getValue().equalsIgnoreCase("not started")) {
                next_task++;
            } else {
                break;
            }
        }

        // if there are no tasks left for processing stop the queue
        if (next_task == model.tasks.size()) {
            executeQueue = false;
        }

        // if there are tasks left process the queue
        if (executeQueue) {
            Task task = model.tasks.get(next_task);
            String progress = task.progress.getValue();
            if (progress.equalsIgnoreCase("not started")) {
                System.out.println("Starting task " + task.fileName.getValue());
                task.progress.setValue("Starting...");

                task.setCompletionListener((t, e1) -> {
                    if (e1 != null) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Settings -> Error executing task " + task.getError().getMessage(), ButtonType.OK);
                            alert.show();
                        });
                    }

                    if (executeQueue) {
                        startNextTask();
                    }
                });

                task.run();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Can only start unstarted tasks", ButtonType.OK);
                alert.show();
                throw new RuntimeException("Can only start unstarted tasks");
            }
        }
    }

    /**
     * Close the ffmpeg tasks
     */
    public void close() {
        if (ffmpegTask != null) {

            try {
                ffmpegTask.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ffmpegTask.interrupt();
        }
    }

    /**
     * Adds the custom profile option to the chooseProfile ChoiceBox if necessary.
     * Is called when the user changes settings.
     */
    public void settingsChanged() {
        System.out.println("Settings were changed");
        System.out.println(model.currentSettings.toStringDetailed());
        if (!profiles.contains(model.currentSettings)) {
            System.out.println("Adding custom profile to the ChoiceBox");
            profiles.add(0, model.currentSettings);
            // temporarily remove the listener so profileChanged() doesn't get called
            // this is because it should only get called when the user changes the profile or on startup, but not when we change it to the custom option
            chooseProfile.getSelectionModel().selectedItemProperty().removeListener(profileChangedListener);
            chooseProfile.getSelectionModel().select(model.currentSettings);
            // re-add the listener
            chooseProfile.getSelectionModel().selectedItemProperty().addListener(profileChangedListener);
        }
    }

    /**
     * Converts the selected Options into a single new Profile
     *
     * @return the Profile with the selected Options
     */
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
        String output = outputPath.getText();

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
        p.setOutputPath(Paths.get(output).toAbsolutePath());

        return p;
    }

    /**
     * Selects Profile-specific Options in all fields.
     * Is called every time the user chooses a new profile from the chooseProfile ChoiceBox AND on startup.
     */
    public void profileChanged() {
        Profile selectedProfile = (Profile) chooseProfile.getSelectionModel().getSelectedItem();
        System.out.println("Profile was changed to " + selectedProfile.getName());
        System.out.println(selectedProfile.toStringDetailed());
        // temporarily remove the settingsChangedListeners
        // this is because settingsChanged() should only fire when the user changes the settings, but not when we do it
        removeSettingsChangedListener();
        // remove current_settings from the ChoiceBox if necessary
        profiles.remove(model.currentSettings);

        chooseFormat.getSelectionModel().select(selectedProfile.getFormat());
        chooseVideoCodec.setValue(selectedProfile.getVideoCodec());
        chooseAudioCodec.getSelectionModel().select(selectedProfile.getAudioCodec());
        subtitlesButton.setSelected(selectedProfile.removeSubtitles());
        audioButton.setSelected(selectedProfile.removeAudio());
        samplerateText.setText(selectedProfile.getAudioSampleRate() == -1 ? "" : Integer.toString(selectedProfile.getAudioSampleRate()));
        videoWidth.setText(selectedProfile.getVideoWidth() == -1 ? "" : Integer.toString(selectedProfile.getVideoWidth()));
        videoHeight.setText(selectedProfile.getVideoHeight() == -1 ? "" : Integer.toString(selectedProfile.getVideoHeight()));
        bitrateText.setText(selectedProfile.getAudioBitRate() == -1 ? "" : Integer.toString(selectedProfile.getAudioBitRate()));
        frameRate.setText(selectedProfile.getVideoFrameRate() == -1 ? "" : Integer.toString((int) selectedProfile.getVideoFrameRate()));
        outputPath.setText(selectedProfile.getOutputPath().toAbsolutePath().toString());

        // re-add listeners
        addSettingsChangedListener();
    }

    /**
     * Fills the ChoiceBoxes with Profiles, Codecs and Formats
     */
    private void fillChoiceBoxes() {
        chooseProfile.setItems(profiles);
        profiles.addAll(profileMap.values().stream().filter(Profile::isCustom).collect(Collectors.toList()));
        profiles.add(new Separator());
        profiles.addAll(profileMap.values().stream().filter(p -> !p.isCustom()).collect(Collectors.toList()));

        // adding saved VideoCodecs to the ChoiceBox
        chooseVideoCodec.getItems().addAll(videoCodecs);
        chooseVideoCodec.getSelectionModel().select(0);

        // adding saved AudioCodecs to the ChoiceBox
        chooseAudioCodec.getItems().addAll(audioCodecs);
        chooseAudioCodec.getSelectionModel().select(0);

        // adding available Formats to the ChoiceBox
        chooseFormat.getItems().addAll(formats);
        chooseFormat.getSelectionModel().select(0);
    }

    /**
     * Requests Profiles, Codecs and Formats from Database
     */
    private void grabOptionsFromDB() {

        try {
            profileMap = SQLite.getAllProfiles();
        } catch (SQLException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            videoCodecs = SQLite.getVideoCodecs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            audioCodecs = SQLite.getAudioCodecs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            formats = SQLite.getFormats();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets Converters for ChoiceBoxes so it can accurately translate between String and the corresponding type.
     */
    private void setChoiceBoxConverters() {
        chooseProfile.setConverter(new StringConverter<>() {
            @Override
            public String toString(Object profile) {
                if (profile == null) return null;
                return profile.toString();
            }

            @Override
            public Profile fromString(String string) {
                return profileMap.get(string);
            }
        });

        chooseVideoCodec.setConverter(new StringConverter<>() {
            @Override
            public String toString(Codec codec) {
                if (codec == null) return null;
                return codec.toString();
            }

            @Override
            public Codec fromString(String string) {
                return videoCodecs.stream().filter(c -> c.toString().equals(string)).findFirst().orElseThrow();
            }
        });

        chooseAudioCodec.setConverter(new StringConverter<>() {
            @Override
            public String toString(Codec codec) {
                if (codec == null) return null;
                return codec.toString();
            }

            @Override
            public Codec fromString(String string) {
                return audioCodecs.stream().filter(c -> c.toString().equals(string)).findFirst().orElseThrow();
            }
        });
        chooseFormat.setConverter(new StringConverter<>() {
            @Override
            public String toString(Format format) {
                if (format == null) return null;
                return format.toString();
            }

            @Override
            public Format fromString(String string) {
                return formats.stream().filter(c -> c.toString().equals(string)).findFirst().orElseThrow();
            }
        });
    }

    private void addSettingsChangedListener() {
        chooseFormat.getSelectionModel().selectedItemProperty().addListener(settingsChangedListener);
        chooseAudioCodec.getSelectionModel().selectedItemProperty().addListener(settingsChangedListener);
        chooseVideoCodec.getSelectionModel().selectedItemProperty().addListener(settingsChangedListener);
        audioButton.selectedProperty().addListener(settingsChangedListener);
        subtitlesButton.selectedProperty().addListener(settingsChangedListener);
        bitrateText.textProperty().addListener(settingsChangedListener);
        videoWidth.textProperty().addListener(settingsChangedListener);
        videoHeight.textProperty().addListener(settingsChangedListener);
        frameRate.textProperty().addListener(settingsChangedListener);
        samplerateText.textProperty().addListener(settingsChangedListener);
        outputPath.textProperty().addListener(settingsChangedListener);
    }

    private void removeSettingsChangedListener() {
        chooseFormat.getSelectionModel().selectedItemProperty().removeListener(settingsChangedListener);
        chooseAudioCodec.getSelectionModel().selectedItemProperty().removeListener(settingsChangedListener);
        chooseVideoCodec.getSelectionModel().selectedItemProperty().removeListener(settingsChangedListener);
        audioButton.selectedProperty().removeListener(settingsChangedListener);
        subtitlesButton.selectedProperty().removeListener(settingsChangedListener);
        bitrateText.textProperty().removeListener(settingsChangedListener);
        videoWidth.textProperty().removeListener(settingsChangedListener);
        videoHeight.textProperty().removeListener(settingsChangedListener);
        frameRate.textProperty().removeListener(settingsChangedListener);
        samplerateText.textProperty().removeListener(settingsChangedListener);
        outputPath.textProperty().removeListener(settingsChangedListener);
    }

    private void addDragAndDrop() {

        main.setOnDragOver(event -> {
            if (event.getGestureSource() != main
                    && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.ANY);
            }
            event.consume();
        });

        main.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                setInputFile(db.getFiles().get(0).getAbsolutePath());
                success = true;
            }
            /* let the source know whether the string was successfully
             * transferred and used */
            event.setDropCompleted(success);
            event.consume();
        });

    }

    private void setInputFile(String str){
        inputFile.setText(str);
    }
}
