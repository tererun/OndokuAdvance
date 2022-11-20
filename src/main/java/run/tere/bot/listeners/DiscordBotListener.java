package run.tere.bot.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import run.tere.bot.Main;
import run.tere.bot.data.ConfigData;
import run.tere.bot.data.CustomUserVoiceData;
import run.tere.bot.data.OndokuStateData;
import run.tere.bot.handlers.AudioPlayerSendHandler;
import run.tere.bot.handlers.CustomUserVoiceHandler;
import run.tere.bot.handlers.OndokuStateHandler;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class DiscordBotListener extends ListenerAdapter {

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
        message = message.replaceAll("(https?|ftp)(://[-_.!~*'()a-zA-Z0-9;/?:@&=+$,%#]+)$", "URL省略");
        CustomUserVoiceHandler customUserVoiceHandler = instance.getCustomUserVoiceHandler();
        CustomUserVoiceData customUserVoiceData = customUserVoiceHandler.getCustomUserVoiceData(userId);
        String voiceId = customUserVoiceData.getVoiceId();
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String uri;
        if (voiceId == null || !customUserVoiceData.isCoeIroInk()) {
            uri = configData.getOpenJTalkUri() + "?text=" + encodedMessage + "&voice=/usr/local/src/htsvoice-tohoku-f01/tohoku-f01-neutral.htsvoice&uuid=" + UUID.randomUUID() + "&fm=" + customUserVoiceData.getPitch();
        } else {
            uri = configData.getCoeIroInkUri() + "?model="+ customUserVoiceData.getVoiceId() + "&uuid=" + UUID.randomUUID() + "&text=" + encodedMessage;
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
                        OndokuStateData ondokuStateData = ondokuStateHandler.getOndokuStateData(guildId);
                        if (ondokuStateData != null) {
                            ondokuStateHandler.getOndokuStateDataList().remove(ondokuStateData);
                        }
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
                    if (ondokuStateData == null || voiceChannel == null) {
                        e.reply("このサーバーでは呼び出されていません!").queue();
                        return;
                    }

                    String voiceChannelId = voiceChannel.getId();
                    audioManager.closeAudioConnection();

                    ondokuStateHandler.getOndokuStateDataList().remove(ondokuStateData);

                    e.reply("切断しました! <#" + voiceChannelId + ">").queue();
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
                    CustomUserVoiceHandler customUserVoiceHandler = Main.getInstance().getCustomUserVoiceHandler();
                    CustomUserVoiceData customUserVoiceData = customUserVoiceHandler.getCustomUserVoiceData(userId);
                    if (customUserVoiceData.getVoiceId() == null) {
                        e.reply("他の合成音声を割り当てていないため切り替えができませんでした!").queue();
                        return;
                    }
                    customUserVoiceData.setCoeIroInk(customUserVoiceHandler, !customUserVoiceData.isCoeIroInk());
                    e.reply("合成音声を切り替えました!").queue();
                } else if (subCommandName.equalsIgnoreCase("r")) {
                    if (user.getId().equalsIgnoreCase("292431056135782402")) {
                        Main.getInstance().reloadConfig();
                        e.reply("リロードしました!").queue();
                    } else {
                        e.reply("権限がありません!").queue();
                    }
                } else {
                    sendHelpEmbed(e);
                }
            }
        }
    }

    private void sendHelpEmbed(SlashCommandEvent slashCommandEvent) {
        slashCommandEvent.replyEmbeds(
                new EmbedBuilder()
                        .setTitle("Help / ヘルプ")
                        .setDescription("読み上げBot「Ondoku」のヘルプです")
                        .addField(
                                ":keyboard: **Commands**",
                                "`/ondoku`　このヘルプを表示\n" +
                                        "`/ondoku s`　Ondoku を召喚します\n" +
                                        "`/ondoku p 数値`　声の高さを`[-24~24]`の間で変更します\n" +
                                        "`/ondoku r`　Ondoku をリロードします"
                                , true)
                        .addField(
                                ":four_leaf_clover: **Thanks**",
                                "`OpenJTalk`　音声合成システム\n" +
                                        "`HTS voice tohoku-f01`　[音響モデル](https://github.com/icn-lab/htsvoice-tohoku-f01)\n"
                                , true)
                        .setFooter("てれるんお手製 - OndokuAdvance Patch-1.0.0")
                        .build()
        );
    }

}
