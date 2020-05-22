package JKU_MMS.Model;

import JKU_MMS.Controller;
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

public class Task implements Runnable {
    public final StringProperty fileName;
    public final StringProperty profileName;
    public final StringProperty progress;
    private final FFmpegBuilder builder;
    private final double videoDuration;
    private FFmpegJob job;

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
     * Creates a taks from an file and profile
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
     * Creates a taks from an file and profile
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
        FFmpegOutputBuilder b = task.builder.addOutput(Paths.get(profile.getOutputPath().toString() + "/" + Paths.get(task.fileName.getValue()).getFileName()).toString());

        if (profile.getVideoCodec().getCodecName().equals("copy")) {
            b.setVideoCodec("copy");
        } else if (!profile.getVideoCodec().getCodecName().equals("auto")) {
            b.setVideoCodec(profile.getVideoCodec().getCodecName());
            System.out.println("video codec set to: " + profile.getVideoCodec().getCodecName());
        }

        if (profile.getAudioCodec().getCodecName().equals("copy")) {
            b.setAudioCodec("copy");
        } else {
            System.out.println(profile.getAudioCodec());
        }

        if (profile.removeAudio()) {
            b.disableAudio();
        }

        if (profile.removeSubtitles()) {
            b.disableSubtitle();
        }

        System.out.println(profile.getVideoHeight());
        System.out.println(profile.getVideoWidth());
        System.out.println(profile.getVideoFrameRate());

        task.builder.addOutput(b);
    }

    /**
     * Builds the job with a progress listener to the {@link StringProperty} progress
     *
     * @param executor
     */
    public void build(FFmpegExecutor executor) {
        job = executor.createJob(builder, new ProgressListener() {
            @Override
            public void progress(Progress arg0) {
                progress.setValue((int) ((arg0.out_time_ns / 1_000_000_0.0) / videoDuration) + "%");
            }
        });
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
        this.job.run();
        progress.setValue("Finished");
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
