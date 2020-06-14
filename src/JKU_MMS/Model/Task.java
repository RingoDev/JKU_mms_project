package JKU_MMS.Model;

import JKU_MMS.Controller;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

public class Task implements Runnable {
    public final StringProperty fileName;
    public final StringProperty profileName;
    public final StringProperty progress;
    private final FFmpegBuilder builder;
    private final double videoDuration;
    private FFmpegJob job;
    private Exception error;
    private OnTaskCompleteListener listener;

    /**
     * Initializes a new task object
     *
     * @param builder
     * @param fileName
     * @param profileName
     * @param videoDuration
     */
    private Task(FFmpegBuilder builder, String fileName, String profileName, double videoDuration) {
        this.builder = builder;
        progress = new SimpleStringProperty("Not started");
        this.fileName = new SimpleStringProperty(fileName);
        this.profileName = new SimpleStringProperty(profileName);
        this.videoDuration = videoDuration;
    }

    /**
     * Creates a task from an file and profile
     *
     * @param input   File path of a video
     * @param profile Profile with settings that will be applied to the video
     * @return Returns a new {@link Task}
     * @throws IOException Throws an {@link IOException} if the input file could not be found
     */
    public static Task of(String input, Profile profile) throws IOException {
        return of(input, profile, false);
    }

    /**
     * Creates a task from an file and profile
     *
     * @param filePath   File path of a video
     * @param profile    Profile with settings that will be applied to the video
     * @param BUILD_FLAG If set to true the tasks will be build with the {@link FFmpegExecutor} set in the {@link Controller}
     * @return Returns a new {@link Task}
     * @throws IOException Throws an {@link IOException} if the input file could not be found
     */
    public static Task of(String filePath, Profile profile, boolean BUILD_FLAG) throws IOException {
        FFmpegBuilder builder = new FFmpegBuilder().addInput(filePath);
        double duration = Controller.ffprobe.probe(filePath).format.duration;

        String profileName;
        if (profile.getName().equals("CURRENT_SETTINGS")) {
            profileName = "Custom";
        } else {
            profileName = profile.getName();
        }

        Task task = new Task(builder, filePath, profileName, duration);

        applyProfile(task, profile);

        if (BUILD_FLAG) {
            task.build(Controller.fFmpegExecutor);
        }

        return task;
    }

    /**
     * Applies a profile to a task
     *
     * @param task
     * @param profile
     */
    private static void applyProfile(Task task, Profile profile) {
        FFmpegOutputBuilder b = task.builder.addOutput(Paths.get(profile.getOutputPath().toAbsolutePath().toString() + "/" +
                Paths.get(task.fileName.getValue()).getFileName()).toString().split("\\.")[0] + "." +
                profile.getFormat().toString().split("-")[0].strip());

        // TODO: show warnings if incompatible settings are detected e.g. different output format but code is copy etc.

        if (! profile.getVideoCodec().getCodecName().equals("auto")) {
            b.setVideoCodec(profile.getVideoCodec().getCodecName());
            System.out.println("video codec set to: " + profile.getVideoCodec().getCodecName());
        }

        if (! profile.getAudioCodec().getCodecName().equals("auto")) {
            b.setAudioCodec(profile.getAudioCodec().getCodecName());
            System.out.println("audio codec set to: " + profile.getAudioCodec().getCodecName());
        }

        if (profile.getVideoFrameRate() != -1) {
            b.setVideoFrameRate(profile.getVideoFrameRate());
            System.out.println("Set video framerate to: " + profile.getVideoFrameRate());
        }

        if (profile.getVideoHeight() != -1) {
            b.setVideoHeight(profile.getVideoHeight());
            System.out.println("Set video height to: " + profile.getVideoHeight());
        }

        if (profile.getVideoWidth() != -1) {
            b.setVideoWidth(profile.getVideoWidth());
            System.out.println("Set video width to: " + profile.getVideoWidth());
        }

        if (profile.getAudioBitRate() != -1) {
            b.setAudioBitRate(profile.getAudioBitRate());
            System.out.println("Set audio bit rate to: " + profile.getAudioBitRate());
        }

        if (profile.getAudioSampleRate() != -1) {
            b.setAudioSampleRate(profile.getAudioSampleRate());
            System.out.println("Set audio sample rate to: " + profile.getAudioSampleRate());
        }

        System.out.println("video format " + profile.getFormat().getFormatName());

        if (profile.removeAudio()) {
            b.disableAudio();
            System.out.println("disabled audio");
        }

        if (profile.removeSubtitles()) {
            b.disableSubtitle();
            System.out.println("disabled subtitles");
        }

        // TODO: apply format (hint: look at formats for example -> matroska should be .mkv)

        // TODO: display some warnings to the user for example:
        // TODO: if video codec is set to copy but framerate is not -1 (e.g. the video has be re-encoded
        //       for the framerate to be changed (same situation if video height/ width is changed etc.))

        task.builder.addOutput(b);
    }

    /**
     * Builds the job with a progress listener to the {@link StringProperty} progress
     *
     * @param executor
     */
    public void build(FFmpegExecutor executor) {
        try {
            job = executor.createJob(builder, new ProgressListener() {
                @Override
                public void progress(Progress arg0) {
                    progress.setValue((int) ((arg0.out_time_ns / 1_000_000_0.0) / videoDuration) + "%");
                }
            });
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Unable to start this task " + e.toString(), ButtonType.OK);
            alert.showAndWait();
            error = e;
        }
    }

    /**
     * Builds the job with an progress listener (so progress can be retrieved during execution)
     *
     * @param executor
     * @param progressListener
     */
    public void build(FFmpegExecutor executor, ProgressListener progressListener) {
        FFmpegJob job = executor.createJob(builder, progressListener);
    }

    public void setCompletionListener(OnTaskCompleteListener listener) {
        this.listener = listener;
    }

    /**
     * Starts the computation of the job. This should be started in a thread.
     *
     * @throws IllegalStateException if task.build has not been called yet
     */
    @Override
    public void run() {
        if (job == null) {
            throw new IllegalStateException("Job has to be built before being started");
        }
        try {
            this.job.run();
        } catch (Exception e) {
            e.printStackTrace();
            progress.setValue("Error");
            error = e;
            if (listener != null) {
                listener.taskComplete(this, e);
            }
            return;
        }
        progress.setValue("Finished");
        if (listener != null) {
            listener.taskComplete(this, null);
        }
    }

    /**
     * Returns the error
     * @return
     */
    public Exception getError() {
        return error;
    }

    /**
     * Retrieve current state of task
     *
     * @return Returns {@link FFmpegJob.State}
     */
    public FFmpegJob.State getState() {
        return job.getState();
    }

    public FFmpegJob getJob() {
        return job;
    }
}
