package JKU_MMS.Model;

import java.nio.file.Path;

public class Profile {
    private final String name;

    private boolean removeSubtitles = false;
    private boolean removeAudio = false;

    // AUDIO SETTING
    private String audioCodec = "copy";
    private int audioSampleRate = -1;
    private int audioBitRate = -1;

    // VIDEO SETTINGS
    private String VideoCodec = "copy";
    private double VideoFrameRate = -1;
    private int videoWidth = -1;
    private int videoHeight = -1;
    private String format;  // "mp4, mkv...."
    // all encoded videos with this profile will be saved in this directory
    private Path outputPath;

    private boolean custom = true;

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public Profile(String name) {
        this.name = name;
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

    public String getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(String audioCodec) {
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

    public String getVideoCodec() {
        return VideoCodec;
    }

    public void setVideoCodec(String setVideoCodec) {
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

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
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
}
