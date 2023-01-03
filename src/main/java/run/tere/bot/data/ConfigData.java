package run.tere.bot.data;

public class ConfigData {

    private String discordBotToken;
    private String coeIroInkUri;
    private String openJTalkUri;
    private String voicevoxUri;
    private String voicevoxAPIToken;

    public ConfigData(String discordBotToken, String coeIroInkUri, String openJTalkUri, String voicevoxUri, String voicevoxAPIToken) {
        this.discordBotToken = discordBotToken;
        this.coeIroInkUri = coeIroInkUri;
        this.openJTalkUri = openJTalkUri;
        this.voicevoxUri = voicevoxUri;
        this.voicevoxAPIToken = voicevoxAPIToken;
    }

    public String getCoeIroInkUri() {
        return coeIroInkUri;
    }

    public String getOpenJTalkUri() {
        return openJTalkUri;
    }

    public String getDiscordBotToken() {
        return discordBotToken;
    }

    public String getVoicevoxUri() {
        return voicevoxUri;
    }

    public String getVoicevoxAPIToken() {
        return voicevoxAPIToken;
    }

}
