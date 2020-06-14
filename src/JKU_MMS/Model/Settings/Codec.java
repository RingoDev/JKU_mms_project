package JKU_MMS.Model.Settings;

import java.util.Objects;

public class Codec {

    String codecName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Codec codec = (Codec) o;
        return codecName.equals(codec.codecName) &&
                Objects.equals(description, codec.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codecName, description);
    }

    String description;
    Boolean decoding;
    Boolean encoding;
    CodecType codecType;
    Boolean intraCodec;
    Boolean lossyCompression;
    Boolean losslessCompression;
    int importance;

    public Codec(String codecName) {
        this.codecName = codecName;
    }

    public Codec(String codecName, int importance){
        this.codecName = codecName;
        this.importance = importance;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
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
        if (description == null) return codecName;
        StringBuilder sb = new StringBuilder();
        sb.append(this.codecName).append(" - ").append(description);
        if (sb.length() > 40) sb.delete(40, sb.length() - 1).append("...");
        return sb.toString();
    }

    public enum CodecType {
        AUDIO, VIDEO, SUBTITLES
    }

}
