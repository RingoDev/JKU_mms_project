package JKU_MMS.Settings;

public class Format {

    String formatName;
    String description;
    Boolean muxing;
    Boolean demuxing;

    public Format(String formatName, int importance){
        this.formatName = formatName;
        this.importance = importance;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    int importance;

    public Format(String formatName) {
        this.formatName = formatName;
    }

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

    public String toString() {
        if(description == null)return formatName;
        StringBuilder sb = new StringBuilder();
        sb.append(this.formatName).append(" - ").append(description);
        if (sb.length() > 40) sb.delete(40, sb.length() - 1).append("...");
        return sb.toString();
    }
}
