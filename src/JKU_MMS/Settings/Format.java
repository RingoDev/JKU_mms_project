package JKU_MMS.Settings;

public class Format {

    String formatName;

    public String getFormatName() {
        return formatName;
    }

    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getMuxing() {
        return muxing;
    }

    public void setMuxing(Boolean muxing) {
        this.muxing = muxing;
    }

    public Boolean getDemuxing() {
        return demuxing;
    }

    public void setDemuxing(Boolean demuxing) {
        this.demuxing = demuxing;
    }

    String description;
    Boolean muxing;
    Boolean demuxing;

    public Format(String formatName){
        this.formatName = formatName;
    }

    public String toString(){
        return formatName + " - " + description;
    }
}
