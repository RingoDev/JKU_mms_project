package JKU_MMS.Model;

import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import org.apache.commons.lang3.NotImplementedException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Task {
    private final FFmpegBuilder builder;
    private FFmpegJob job;
    public final StringProperty fileName;
    public final StringProperty profileName;
    public final StringProperty progress;
    
    /**
	 * Initializes a new task object
	 * @param builder 
	 * @param fileName
	 * @param profileName
	 */
    public Task(FFmpegBuilder builder, String fileName, String profileName) {
        this.builder = builder;
        progress = new SimpleStringProperty("Not started");
        this.fileName = new SimpleStringProperty(fileName);
        this.profileName = new SimpleStringProperty(profileName);
    }

    /**
     * Builds the job without an progress listener
     * @param executor
     */
    public void build(FFmpegExecutor executor) {
        FFmpegJob job = executor.createJob(builder, new ProgressListener() {
			
			@Override
			public void progress(Progress arg0) {
				// TODO: listen to the progress of the job
				// and update the value in the progress property by calling the progress.setValue(String newValue) method
			}
		});
    }

    /**
     * Builds the job with an progress listener (so progress can be retrieved during execution)
     * Probably not needed because we are working with properties here, so we don't need to pass a progressListener from the outside
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
