package run.tere.bot.speakers;

public class SupportedFeatures {
    private String permittedSynthesisMorphing;

    // Getters and Setters
    public String getPermittedSynthesisMorphing() {
        return permittedSynthesisMorphing;
    }

    public void setPermittedSynthesisMorphing(String permittedSynthesisMorphing) {
        this.permittedSynthesisMorphing = permittedSynthesisMorphing;
    }

    @Override
    public String toString() {
        return "SupportedFeatures{" +
                "permittedSynthesisMorphing='" + permittedSynthesisMorphing + '\'' +
                '}';
    }
}

