package run.tere.bot.data;

public class ConfigData {

    private String discordBotToken;
    private String coeIroInkUri;
    private String openJTalkUri;

    public ConfigData(String discordBotToken, String coeIroInkUri, String openJTalkUri) {
        this.discordBotToken = discordBotToken;
        this.coeIroInkUri = coeIroInkUri;
        this.openJTalkUri = openJTalkUri;
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

}
