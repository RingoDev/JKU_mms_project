package JKU_MMS.Settings;

public class Codec {

    String CodecName;
    String Description;
    Boolean Decoding;
    Boolean Encoding;
    CodecType codecType;
    Boolean IntraCodec;
    Boolean LossyCompression;
    Boolean LosslessCompression;

    public Codec(String CodecName) {
        this.CodecName = CodecName;
    }

    public String getCodecName() {
        return CodecName;
    }

    public void setCodecName(String codecName) {
        CodecName = codecName;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public Boolean getDecoding() {
        return Decoding;
    }

    public void setDecoding(Boolean decoding) {
        Decoding = decoding;
    }

    public Boolean getEncoding() {
        return Encoding;
    }

    public void setEncoding(Boolean encoding) {
        Encoding = encoding;
    }

    public CodecType getCodecType() {
        return codecType;
    }

    public void setCodecType(CodecType codecType) {
        this.codecType = codecType;
    }

    public Boolean getIntraCodec() {
        return IntraCodec;
    }

    public void setIntraCodec(Boolean intraCodec) {
        IntraCodec = intraCodec;
    }

    public Boolean getLossyCompression() {
        return LossyCompression;
    }

    public void setLossyCompression(Boolean lossyCompression) {
        LossyCompression = lossyCompression;
    }

    public Boolean getLosslessCompression() {
        return LosslessCompression;
    }

    public void setLosslessCompression(Boolean losslessCompression) {
        LosslessCompression = losslessCompression;
    }

    public String toString() {
        return this.CodecName + " - " + Description;
    }

    public enum CodecType {
        AUDIO, VIDEO, SUBTITLES
    }

}
