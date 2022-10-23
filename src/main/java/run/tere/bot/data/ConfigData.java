package run.tere.bot.data;

public class ConfigData {

    private String discordBotToken;
    private String coeIroInkUri;

    public ConfigData(String discordBotToken, String coeIroInkUri) {
        this.discordBotToken = discordBotToken;
        this.coeIroInkUri = coeIroInkUri;
    }

    public String getCoeIroInkUri() {
        return coeIroInkUri;
    }

    public String getDiscordBotToken() {
        return discordBotToken;
    }

}
