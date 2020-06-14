package JKU_MMS.Model;

import JKU_MMS.Model.Settings.Codec;
import JKU_MMS.Model.Settings.Format;

import java.nio.file.Path;
import java.util.Objects;

public class Profile {
    private final String name;

    private boolean removeSubtitles = false;
    private boolean removeAudio = false;

    // AUDIO SETTING
    private Codec audioCodec = new Codec("auto");
    private int audioSampleRate = -1;
    private int audioBitRate = -1;
    // VIDEO SETTINGS
    private Codec VideoCodec = new Codec("auto");
    private double VideoFrameRate = -1;
    private int videoWidth = -1;
    private int videoHeight = -1;
    private Format format = new Format("auto");  // "mp4, mkv...."
    // all encoded videos with this profile will be saved in this directory
    private Path outputPath = Path.of("output");
    private boolean custom = true;
    public Profile(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return removeSubtitles == profile.removeSubtitles &&
                removeAudio == profile.removeAudio &&
                audioSampleRate == profile.audioSampleRate &&
                audioBitRate == profile.audioBitRate &&
                Double.compare(profile.VideoFrameRate, VideoFrameRate) == 0 &&
                videoWidth == profile.videoWidth &&
                videoHeight == profile.videoHeight &&
                Objects.equals(audioCodec, profile.audioCodec) &&
                Objects.equals(VideoCodec, profile.VideoCodec) &&
                Objects.equals(format, profile.format) &&
                Objects.equals(outputPath, profile.outputPath) &&
                name.equals(profile.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(removeSubtitles, removeAudio, audioCodec, audioSampleRate, audioBitRate, VideoCodec, VideoFrameRate, videoWidth, videoHeight, format, outputPath);
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public boolean removeSubtitles() {
        return removeSubtitles;
    }

    public void setRemoveSubtitles(boolean removeSubtitles) {
        this.removeSubtitles = removeSubtitles;
    }

    public boolean removeAudio() {
        return removeAudio;
    }

    public void setRemoveAudio(boolean removeAudio) {
        this.removeAudio = removeAudio;
    }

    public Codec getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(Codec audioCodec) {
        this.audioCodec = audioCodec;
    }

    public int getAudioSampleRate() {
        return audioSampleRate;
    }

    public void setAudioSampleRate(int audioSampleRate) {
        this.audioSampleRate = audioSampleRate;
    }

    public int getAudioBitRate() {
        return audioBitRate;
    }

    public void setAudioBitRate(int audioBitRate) {
        this.audioBitRate = audioBitRate;
    }

    public Codec getVideoCodec() {
        return VideoCodec;
    }

    public void setVideoCodec(Codec setVideoCodec) {
        this.VideoCodec = setVideoCodec;
    }

    public double getVideoFrameRate() {
        return VideoFrameRate;
    }

    public void setVideoFrameRate(double setVideoFrameRate) {
        this.VideoFrameRate = setVideoFrameRate;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(Path outputPath) {
        this.outputPath = outputPath;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

	public String toStringDetailed() {
		return "Profile [name=" + name + ", removeSubtitles=" + removeSubtitles + ", removeAudio=" + removeAudio
				+ ", audioCodec=" + audioCodec + ", audioSampleRate=" + audioSampleRate + ", audioBitRate="
				+ audioBitRate + ", VideoCodec=" + VideoCodec + ", VideoFrameRate=" + VideoFrameRate + ", videoWidth="
				+ videoWidth + ", videoHeight=" + videoHeight + ", format=" + format + ", outputPath=" + outputPath
				+ ", custom=" + custom + "]";
	}
}
