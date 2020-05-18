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

    public void build(FFmpegExecutor executor) {
        FFmpegJob job = executor.createJob(builder);
    }

    public void build(FFmpegExecutor executor, ProgressListener progressListener) {
        FFmpegJob job = executor.createJob(builder, progressListener);
    }

    public void run() {
        if (job == null) {
            throw new IllegalStateException("Job has to be build before being started");
        }
        this.job.run();
    }

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
