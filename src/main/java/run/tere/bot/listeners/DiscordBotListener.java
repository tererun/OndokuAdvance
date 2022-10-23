package run.tere.bot.listeners;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import run.tere.bot.Main;
import run.tere.bot.data.OndokuStateData;
import run.tere.bot.handlers.AudioPlayerSendHandler;
import run.tere.bot.handlers.OndokuStateHandler;

public class DiscordBotListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent e) {
        Main instance = Main.getInstance();
        User user = e.getAuthor();
        String guildId = e.getGuild().getId();
        String channelId = e.getChannel().getId();
        String message = e.getMessage().getContentRaw();
        OndokuStateHandler ondokuStateHandler = instance.getVoiceChannelHandler();
        OndokuStateData ondokuStateData = ondokuStateHandler.getOndokuStateData(guildId);
        if (user.isBot()) return;
        if (ondokuStateData == null) return;
        AudioPlayerManager audioPlayerManager = ondokuStateData.getAudioPlayerManager();
        AudioPlayer audioPlayer = ondokuStateData.getAudioPlayer();
        audioPlayerManager.loadItem("https://www.youtube.com/watch?v=nyyNKbIOjhM", new FunctionalResultHandler(audioTrack -> {
            audioPlayer.playTrack(audioTrack);
        }, audioPlaylist -> {
            audioPlayer.playTrack(audioPlaylist.getTracks().get(0));
        }, () -> {
            System.out.println("hehehe");
        }, e1 -> {
            e1.printStackTrace();
        }));
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent e) {
        String label = e.getName();
        Guild guild = e.getGuild();
        if (guild == null) {
            e.reply("ここではこのコマンドを実行できません").queue();
            return;
        }
        String guildId = guild.getId();
        User user = e.getUser();
        if (label.equalsIgnoreCase("ondoku")) {
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
                    if (ondokuStateHandler.getOndokuStateData(guildId) != null || audioManager.getConnectedChannel() != null) {
                        e.reply("すでにこのサーバーでは呼び出されています!").queue();
                        return;
                    }

                    String voiceChannelId = voiceChannel.getId();
                    AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

                    AudioSourceManagers.registerRemoteSources(audioPlayerManager);

                    AudioPlayer audioPlayer = audioPlayerManager.createPlayer();
                    VoiceAudioListener voiceAudioListener = new VoiceAudioListener(audioPlayer);
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
