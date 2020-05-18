package JKU_MMS.Model;

import java.nio.file.Path;

public class Profile {
    private final String name;

    private boolean removeSubtitles;
    private boolean removeAudio;

    // AUDIO SETTING
    private String audioCodec = "copy";
    private int audioSampleRate;
    private int audioBitRate;

    // VIDEO SETTINGS
    private String setVideoCodec = "copy";
    private double setVideoFrameRate;
    private int videoWidth;
    private int videoHeight;
    private String format;  // "mp4, mkv...."

    // all encoded videos with this profile will be save in this directory
    private Path outputPath;

    public Profile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
