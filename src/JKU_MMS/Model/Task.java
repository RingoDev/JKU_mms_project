package JKU_MMS.Model;

import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.progress.ProgressListener;
import org.apache.commons.lang3.NotImplementedException;

public class Task {
    private final FFmpegBuilder builder;
    private FFmpegJob job;

    public Task(FFmpegBuilder builder) {
        this.builder = builder;
    }

    /**
     * Builds the job without an progress listener
     * @param executor
     */
    public void build(FFmpegExecutor executor) {
        FFmpegJob job = executor.createJob(builder);
    }

    /**
     * Builds the job with an progress listener (so progress can be retrieved during execution)
     * @param executor
     * @param progressListener
     */
    public void build(FFmpegExecutor executor, ProgressListener progressListener) {
        FFmpegJob job = executor.createJob(builder, progressListener);
    }

    /**
     * Starts the computation of the job
     * @throws IllegalStateException if task.build has not been called yet
     */
    public void run() {
        if (job == null) {
            throw new IllegalStateException("Job has to be build before being started");
        }
        this.job.run();
    }

    /**
     * Retrieve current state of task
     * @return Returns {@link FFmpegJob.State}
     */
    public FFmpegJob.State getState() {
        return job.getState();
    }

    public FFmpegJob getJob() {
        return job;
    }

    /**
     * Get progress of this task
     * @return Returns int between 0-100
     */
    public int getProgress() {
        // TODO: implement
        throw new NotImplementedException("");
    }
}
