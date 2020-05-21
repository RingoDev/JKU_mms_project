package JKU_MMS.Settings;

public class Codec {

    String codecName;
    String description;
    Boolean decoding;
    Boolean encoding;
    CodecType codecType;
    Boolean intraCodec;
    Boolean lossyCompression;
    Boolean losslessCompression;

    public Codec(String CodecName) {
        this.codecName = CodecName;
    }

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDecoding() {
        return decoding;
    }

    public void setDecoding(Boolean decoding) {
        this.decoding = decoding;
    }

    public Boolean getEncoding() {
        return encoding;
    }

    public void setEncoding(Boolean encoding) {
        this.encoding = encoding;
    }

    public CodecType getCodecType() {
        return codecType;
    }

    public void setCodecType(CodecType codecType) {
        this.codecType = codecType;
    }

    public Boolean getIntraCodec() {
        return intraCodec;
    }

    public void setIntraCodec(Boolean intraCodec) {
        this.intraCodec = intraCodec;
    }

    public Boolean getLossyCompression() {
        return lossyCompression;
    }

    public void setLossyCompression(Boolean lossyCompression) {
        this.lossyCompression = lossyCompression;
    }

    public Boolean getLosslessCompression() {
        return losslessCompression;
    }

    public void setLosslessCompression(Boolean losslessCompression) {
        this.losslessCompression = losslessCompression;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.codecName).append(" - ").append(description);
        if (sb.length() > 40) sb.delete(40, sb.length()-1).append("...");
        return sb.toString();
    }

    public enum CodecType {
        AUDIO, VIDEO, SUBTITLES
    }

}
