package run.tere.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import run.tere.bot.data.ConfigData;
import run.tere.bot.handlers.CustomUserVoiceHandler;
import run.tere.bot.handlers.OndokuStateHandler;
import run.tere.bot.listeners.DiscordBotListener;
import run.tere.bot.utils.GsonUtil;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    private static Main instance;
    private JDA jda;
    private ConfigData configData;
    private CustomUserVoiceHandler customUserVoiceHandler;
    private OndokuStateHandler ondokuStateHandler;

    public static void main(String[] args) {
        instance = new Main();
    }

    public Main() {
        initConfig();
        initJDA();
        initCommands();

        customUserVoiceHandler = CustomUserVoiceHandler.load();
        ondokuStateHandler = new OndokuStateHandler();
    }

    private void initConfig() {
        saveDefaultConfig();
        configData = loadConfig();
    }

    public void saveDefaultConfig() {
        Path path = Paths.get("");
        File configFile = new File(path.toAbsolutePath().toFile(), "config.json");
        if (configFile.exists()) return;
        ConfigData defaultConfigData = new ConfigData("bot token here", "localhost:port/generate", "localhost:port/voice", "localhost:port/audio/", "voicevox api key here");
        GsonUtil.toJson(configFile, defaultConfigData);
    }

    public ConfigData loadConfig() {
        Path path = Paths.get("");
        File configFile = new File(path.toAbsolutePath().toFile(), "config.json");
        return (ConfigData) GsonUtil.fromJson(configFile, ConfigData.class);
    }

    public void reloadConfig() {
        configData = loadConfig();
        customUserVoiceHandler = CustomUserVoiceHandler.load();
    }

    private void initJDA() {
        try {
            jda = JDABuilder
                    .createDefault(configData.getDiscordBotToken())
                    .enableIntents(GatewayIntent.GUILD_MESSAGES)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(new DiscordBotListener())
                    .build();
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }
    }

    private void initCommands() {
        jda.upsertCommand("ondoku", "Ondoku?????????????????????????????????")
                .addSubcommands(new SubcommandData("s", "Ondoku??????????????????????????? ???"))
                .addSubcommands(new SubcommandData("b", "Ondoku??????????????????????????? ????"))
                .addSubcommands(
                        new SubcommandData("p", "?????????????????????????????????????????? ???")
                                .addOption(OptionType.NUMBER, "pitch", "??????????????????????????? [-24~24] ???????????????????????? ????")
                )
                .addSubcommands(new SubcommandData("c", "?????????????????????????????? ????"))
                .addSubcommands(new SubcommandData("r", "Ondoku??????????????????????????? ????"))
                .addSubcommands(new SubcommandData("i", "Ondoku?????????????????????????????? ??????"))
                .queue();
    }

    public JDA getJDA() {
        return jda;
    }

    public ConfigData getConfigData() {
        return configData;
    }

    public CustomUserVoiceHandler getCustomUserVoiceHandler() {
        return customUserVoiceHandler;
    }

    public OndokuStateHandler getVoiceChannelHandler() {
        return ondokuStateHandler;
    }

    public static Main getInstance() {
        return instance;
    }

}