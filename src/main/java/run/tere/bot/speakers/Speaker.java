package run.tere.bot.speakers;

import java.util.List;

public class Speaker {
    private SupportedFeatures supportedFeatures;
    private String name;
    private String speakerUuid;
    private List<Style> styles;
    private String version;

    public SupportedFeatures getSupportedFeatures() {
        return supportedFeatures;
    }

    public void setSupportedFeatures(SupportedFeatures supportedFeatures) {
        this.supportedFeatures = supportedFeatures;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpeakerUuid() {
        return speakerUuid;
    }

    public void setSpeakerUuid(String speakerUuid) {
        this.speakerUuid = speakerUuid;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public void setStyles(List<Style> styles) {
        this.styles = styles;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Speaker{" +
                "supportedFeatures=" + supportedFeatures +
                ", name='" + name + '\'' +
                ", speakerUuid='" + speakerUuid + '\'' +
                ", styles=" + styles +
                ", version='" + version + '\'' +
                '}';
    }
}

