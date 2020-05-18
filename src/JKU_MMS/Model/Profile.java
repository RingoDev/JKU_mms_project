package JKU_MMS.Model;

import java.nio.file.Path;

public class Profile {
    private final String name;

    private boolean removeSubtitles;
    private boolean removeAudio;

    // AUDIO SETTING
    private String setAudioCodec = "copy";
    private int setAudioSampleRate;
    private int setAudioBitRate;

    // VIDEO SETTINGS
    private String setVideoCodec = "copy";
    private double setVideoFrameRate;
    private int videoWidth;
    private int getVideoHeight;
    private String setFormat;  // "mp4, mkv...."

    // all encoded videos with this profile will be save in this directory
    private Path outputPath;

    public Profile(String name) {
        this.name = name;
    }
}
