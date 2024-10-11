package run.tere.bot.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import run.tere.bot.Main;
import run.tere.bot.data.ConfigData;
import run.tere.bot.data.CustomUserVoiceData;
import run.tere.bot.data.CustomUserVoiceType;
import run.tere.bot.data.OndokuStateData;
import run.tere.bot.handlers.AudioPlayerSendHandler;
import run.tere.bot.handlers.CustomUserVoiceHandler;
import run.tere.bot.handlers.OndokuStateHandler;
import run.tere.bot.speakers.SpeakerInfo;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordBotListener extends ListenerAdapter {

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent e) {
        autoDisconnect(e.getChannelLeft());
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent e) {
        autoDisconnect(e.getChannelLeft());
    }

    private void autoDisconnect(VoiceChannel voiceChannel) {
        Main instance = Main.getInstance();
        Guild guild = voiceChannel.getGuild();
        String guildId = guild.getId();
        String voiceChannelId = voiceChannel.getId();
        OndokuStateHandler ondokuStateHandler = instance.getVoiceChannelHandler();
        AudioManager audioManager = guild.getAudioManager();
        VoiceChannel connectedChannel = audioManager.getConnectedChannel();
        if (connectedChannel != null && connectedChannel.getId().equalsIgnoreCase(voiceChannelId) && getConnectedVoiceChannelSizeWithoutBot(connectedChannel) <= 1) {
            OndokuStateData ondokuStateData = ondokuStateHandler.getOndokuStateData(guildId);
            if (ondokuStateData != null) {
                audioManager.closeAudioConnection();
                ondokuStateHandler.getOndokuStateDataList().remove(ondokuStateData);
            }
        }
    }

    private int getConnectedVoiceChannelSizeWithoutBot(VoiceChannel voiceChannel) {
        List<Member> voiceChannelMember = new ArrayList<>();
        for (Member member : voiceChannel.getMembers()) {
            if (!member.getUser().isBot()) voiceChannelMember.add(member);
        }
        return voiceChannelMember.size();
    }

    private HashMap<String, Integer> viewingPages = new HashMap<>();

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        if (!event.getComponentId().equalsIgnoreCase("voiceData")) return;
        Main instance = Main.getInstance();
        String userId = event.getUser().getId();
        String selectedOption = event.getValues().get(0);
        if (selectedOption == null) {
            event.reply("声を選択してください").setEphemeral(true).queue();
            return;
        }
        CustomUserVoiceHandler customUserVoiceHandler = instance.getCustomUserVoiceHandler();
        CustomUserVoiceData customUserVoiceData = customUserVoiceHandler.getCustomUserVoiceData(userId);
        String[] selectedOptionSplit = selectedOption.split(";");
        switch (selectedOptionSplit[0]) {
            case "openjtalk":
                customUserVoiceData.setCustomUserVoiceType(customUserVoiceHandler, CustomUserVoiceType.OPEN_JTALK);
                break;
            case "voicevox":
                customUserVoiceData.setCustomUserVoiceType(customUserVoiceHandler, CustomUserVoiceType.VOICEVOX);
                customUserVoiceData.setVoicevoxSpeakerId(customUserVoiceHandler, Integer.parseInt(selectedOptionSplit[1]));
                break;
            case "coeiroink":
                customUserVoiceData.setCustomUserVoiceType(customUserVoiceHandler, CustomUserVoiceType.COEIROINK);
                break;
        }
        event.reply("声を「**" + selectedOptionSplit[2] + "**」に変更しました").queue();
    }

    private SelectionMenu createSelectionMenu(String userId) {
        CustomUserVoiceHandler customUserVoiceHandler = Main.getInstance().getCustomUserVoiceHandler();
        CustomUserVoiceData customUserVoiceData = customUserVoiceHandler.getCustomUserVoiceData(userId);
        SelectionMenu.Builder builder = SelectionMenu.create("voiceData");
        builder.setPlaceholder("声を選択");

        int pageId = viewingPages.getOrDefault(userId, 0);
        int start = pageId * 25 - 2;
        int pickup = 25;

        if (pageId == 0) {
            pickup = 23;
            start = 0;
            builder.addOption("OpenJTalk", "openjtalk;tohoku-f01-neutral;OpenJTalk");
            if (customUserVoiceData.getVoiceId() != null) builder.addOption("自分の声", "coeiroink;自分の声;自分の声");
        }

        List<SpeakerInfo> speakerInfos = Main.getInstance().getVoicevoxSpeakerHandler().getSpeakerInfos();
        for (int i = start; i < Math.min(speakerInfos.size(), start + pickup); i++) {
            SpeakerInfo speakerInfo = speakerInfos.get(i);
            builder.addOption(speakerInfo.displayName(), speakerInfo.name());
        }

        return builder.build();
    }

    private void editSelectionMenu(ButtonClickEvent event) {
        event.editComponents(ActionRow.of(
                createSelectionMenu(event.getUser().getId())
        ), ActionRow.of(
                Button.secondary("voice_left", "←"),
                Button.secondary("voice_right", "→")
        )).queue();
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        switch (event.getComponentId()) {
            case "voice_left": {
                if (viewingPages.getOrDefault(event.getUser().getId(), 0) <= 0) {
                    event.reply("これ以上左には移動できません").setEphemeral(true).queue();
                    return;
                }
                viewingPages.put(event.getUser().getId(), viewingPages.get(event.getUser().getId()) - 1);
                editSelectionMenu(event);
                break;
            }
            case "voice_right": {
                if (viewingPages.getOrDefault(event.getUser().getId(), 0) >= Math.floor((double) Main.getInstance().getVoicevoxSpeakerHandler().getSpeakerInfos().size() / 25)) {
                    event.reply("これ以上右には移動できません").setEphemeral(true).queue();
                    return;
                }
                viewingPages.put(event.getUser().getId(), viewingPages.get(event.getUser().getId()) + 1);
                editSelectionMenu(event);
                break;
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        Main instance = Main.getInstance();
        ConfigData configData = instance.getConfigData();
        User user = e.getAuthor();
        String userId = user.getId();
        String guildId = e.getGuild().getId();
        String channelId = e.getChannel().getId();
        String message = e.getMessage().getContentDisplay();
        OndokuStateHandler ondokuStateHandler = instance.getVoiceChannelHandler();
        OndokuStateData ondokuStateData = ondokuStateHandler.getOndokuStateData(guildId);
        if (user.isBot()) return;
        if (ondokuStateData == null || !ondokuStateData.getTextChannelId().equalsIgnoreCase(channelId)) return;
        if (message.length() >= 120) {
            message = message.substring(0, 119);
        }
        String regex = "https?://[-_.!~*'()a-zA-Z0-9;/?:@&=+$,%#]+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        message = matcher.replaceAll("URL省略");
        CustomUserVoiceHandler customUserVoiceHandler = instance.getCustomUserVoiceHandler();
        CustomUserVoiceData customUserVoiceData = customUserVoiceHandler.getCustomUserVoiceData(userId);
        CustomUserVoiceType customUserVoiceType = customUserVoiceData.getCustomUserVoiceType();
        String voiceId = customUserVoiceData.getVoiceId();
        int styleId = customUserVoiceData.getStyleId();
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String uri;
        if (customUserVoiceType == null) {
            customUserVoiceData.setCustomUserVoiceType(customUserVoiceHandler, CustomUserVoiceType.OPEN_JTALK);
        }
        if (customUserVoiceType == CustomUserVoiceType.VOICEVOX) {
            uri = configData.getVoicevoxUri() + "/audio/?key=" + configData.getVoicevoxAPIToken() + "&speaker=" + customUserVoiceData.getVoicevoxSpeakerId() + "&pitch=0&intonationScale=1&speed=1&text=" + encodedMessage;
        } else if (customUserVoiceType == CustomUserVoiceType.COEIROINK && voiceId != null) {
            uri = "coeiroink;" + message + ";" + voiceId + ";" + styleId;
        } else {
            uri = configData.getOpenJTalkUri() + "?text=" + encodedMessage + "&voice=/usr/local/src/htsvoice-tohoku-f01/tohoku-f01-neutral.htsvoice&uuid=" + UUID.randomUUID() + "&fm=" + customUserVoiceData.getPitch();
        }
        ondokuStateData.getVoiceAudioListener().addQueue(uri);
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent e) {
        String label = e.getName();
        Guild guild = e.getGuild();
        User user = e.getUser();
        String userId = user.getId();
        if (label.equalsIgnoreCase("ondoku")) {
            if (guild == null) {
                e.reply("ここではこのコマンドを実行できません").queue();
                return;
            }
            String guildId = guild.getId();
            String subCommandName = e.getSubcommandName();
            if (subCommandName == null) {
                sendHelpEmbed(e);
            } else {
                Member member = e.getMember();
                if (member == null) {
                    e.reply("ここではこのコマンドを実行できません").queue();
                    return;
                }
                GuildVoiceState voiceState = member.getVoiceState();
                Main instance = Main.getInstance();
                OndokuStateHandler ondokuStateHandler = instance.getVoiceChannelHandler();
                AudioManager audioManager = guild.getAudioManager();
                if (subCommandName.equalsIgnoreCase("s")) {
                    if (voiceState == null) {
                        e.reply("ボイスチャンネルに入った状態で呼び出してください!").queue();
                        return;
                    }
                    VoiceChannel voiceChannel = voiceState.getChannel();
                    if (voiceChannel == null) {
                        e.reply("ボイスチャンネルに入った状態で呼び出してください!").queue();
                        return;
                    }
                    if (audioManager.getConnectedChannel() != null) {
                        e.reply("すでにこのサーバーでは呼び出されています!").queue();
                        return;
                    }
                    OndokuStateData oldOndokuStateData = ondokuStateHandler.getOndokuStateData(guildId);
                    if (oldOndokuStateData != null) {
                        ondokuStateHandler.getOndokuStateDataList().remove(oldOndokuStateData);
                    }

                    String voiceChannelId = voiceChannel.getId();
                    AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

                    AudioSourceManagers.registerRemoteSources(audioPlayerManager);

                    AudioPlayer audioPlayer = audioPlayerManager.createPlayer();
                    VoiceAudioListener voiceAudioListener = new VoiceAudioListener(audioPlayerManager, audioPlayer);
                    audioPlayer.addListener(voiceAudioListener);

                    audioManager.setSendingHandler(new AudioPlayerSendHandler(audioPlayer));
                    audioManager.openAudioConnection(voiceChannel);

                    OndokuStateData ondokuStateData = new OndokuStateData(audioPlayer, audioPlayerManager, voiceAudioListener, guildId, voiceChannelId, e.getMessageChannel().getId());
                    ondokuStateHandler.getOndokuStateDataList().add(ondokuStateData);

                    e.reply("接続しました! <#" + voiceChannelId + ">").queue();
                } else if (subCommandName.equalsIgnoreCase("b")) {
                    VoiceChannel voiceChannel = audioManager.getConnectedChannel();
                    OndokuStateData ondokuStateData = ondokuStateHandler.getOndokuStateData(guildId);
                    if (voiceChannel != null) {
                        String voiceChannelId = voiceChannel.getId();
                        audioManager.closeAudioConnection();

                        if (ondokuStateData != null) {
                            ondokuStateHandler.getOndokuStateDataList().remove(ondokuStateData);
                        }
                        e.reply("切断しました! <#" + voiceChannelId + ">").queue();
                    } else {
                        if (ondokuStateData == null) {
                            e.reply("このサーバーでは呼び出されていません!").queue();
                        }
                    }
                } else if (subCommandName.equalsIgnoreCase("p")) {
                    OptionMapping optionMapping = e.getOption("pitch");
                    if (optionMapping == null) {
                        e.reply("値を`-24~24`の間で入力してください").queue();
                        return;
                    }
                    double pitchDouble = optionMapping.getAsDouble();
                    if (pitchDouble < -24 || pitchDouble > 24) {
                        e.reply("値を`-24~24`の間で入力してください").queue();
                        return;
                    }
                    CustomUserVoiceHandler customUserVoiceHandler = Main.getInstance().getCustomUserVoiceHandler();
                    CustomUserVoiceData customUserVoiceData = customUserVoiceHandler.getCustomUserVoiceData(userId);
                    customUserVoiceData.setPitch(customUserVoiceHandler, (float) pitchDouble);
                    e.reply("ピッチを `" + pitchDouble + "` に変更しました!").queue();
                } else if (subCommandName.equalsIgnoreCase("c")) {
                    viewingPages.put(user.getId(), 0);

                    e.reply("使いたい声を選択してください")
                            .addActionRow(createSelectionMenu(user.getId()))
                            .addActionRow(
                                    Button.secondary("voice_left", "←"),
                                    Button.secondary("voice_right", "→")
                            )
                            .setEphemeral(true)
                            .queue();
                } else if (subCommandName.equalsIgnoreCase("r")) {
                    if (user.getId().equalsIgnoreCase("292431056135782402")) {
                        Main.getInstance().reloadConfig();
                        e.reply("リロードしました!").queue();
                    } else {
                        e.reply("権限がありません!").queue();
                    }
                } else if (subCommandName.equalsIgnoreCase("i")) {
                    sendHelpEmbed(e);
                } else {
                    sendHelpEmbed(e);
                }
            }
        }
    }

    private void sendHelpEmbed(SlashCommandEvent slashCommandEvent) {
        slashCommandEvent.replyEmbeds(
                new EmbedBuilder()
                        .setTitle("Info / 情報")
                        .setDescription("読み上げBot「Ondoku」の情報です")
                        .addField(
                                ":keyboard: **Commands**",
                                "`/ondoku i`　このヘルプを表示\n" +
                                        "`/ondoku s`　Ondoku を召喚します\n" +
                                        "`/ondoku p 数値`　声の高さを`[-24~24]`の間で変更します\n" +
                                        "`/ondoku c`　読み上げ声を切り替えます\n" +
                                        "`/ondoku r`　Ondoku をリロードします"
                                , true)
                        .addField(
                                ":four_leaf_clover: **Thanks**",
                                "`OpenJTalk`　音声合成システム\n" +
                                        "`HTS voice tohoku-f01`　[音響モデル](https://github.com/icn-lab/htsvoice-tohoku-f01)\n"+
                                        "`VOICEVOX`　[ずんだもん](https://voicevox.hiroshiba.jp/)\n"
                                , true)
                        .setFooter("てれるんお手製 - OndokuAdvance Patch-1.0.0")
                        .build()
        ).queue();
    }

}
