package run.tere.bot.listeners;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
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
import run.tere.bot.utils.VersionUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiscordBotListener extends ListenerAdapter {

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        AudioChannelUnion channelLeft = event.getChannelLeft();
        if (channelLeft != null) {
            autoDisconnect(channelLeft.asVoiceChannel());
        }
    }

    private void autoDisconnect(VoiceChannel voiceChannel) {
        Main instance = Main.getInstance();
        Guild guild = voiceChannel.getGuild();
        String guildId = guild.getId();
        String voiceChannelId = voiceChannel.getId();
        OndokuStateHandler ondokuStateHandler = instance.getVoiceChannelHandler();
        AudioManager audioManager = guild.getAudioManager();
        AudioChannelUnion audioChannelUnion = audioManager.getConnectedChannel();
        VoiceChannel connectedChannel = audioChannelUnion == null ? null : audioChannelUnion.asVoiceChannel();
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
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
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
        event.reply("<@" + userId + "> の声を「**" + selectedOptionSplit[2] + "**」に変更しました").queue();
    }

    private StringSelectMenu createSelectionMenu(String userId) {
        CustomUserVoiceHandler customUserVoiceHandler = Main.getInstance().getCustomUserVoiceHandler();
        CustomUserVoiceData customUserVoiceData = customUserVoiceHandler.getCustomUserVoiceData(userId);
        StringSelectMenu.Builder builder = StringSelectMenu.create("voiceData");
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

    private void editSelectionMenu(ButtonInteractionEvent event) {
        event.editComponents(ActionRow.of(
                createSelectionMenu(event.getUser().getId())
        ), ActionRow.of(
                Button.secondary("voice_left", "←"),
                Button.secondary("voice_right", "→")
        )).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
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
    public void onModalInteraction(ModalInteractionEvent event) {
        switch (event.getModalId()) {
            case "add_dict_modal": {
                String key = event.getValues().get(0).getAsString();
                String value = event.getValues().get(1).getAsString();
                Guild guild = event.getGuild();
                if (guild == null) {
                    event.reply("このコマンドはサーバー内でのみ使用できます").setEphemeral(true).queue();
                    return;
                }
                Main.getInstance().getDictHandler().addDict(guild.getId(), key, value);
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("辞書に単語を追加しました")
                        .addField("単語", key, true)
                        .addField("読み", value, true)
                        .build()
                ).queue();
                break;
            }
            case "remove_dict_modal": {
                String key = event.getValues().get(0).getAsString();
                Guild guild = event.getGuild();
                if (guild == null) {
                    event.reply("このコマンドはサーバー内でのみ使用できます").setEphemeral(true).queue();
                    return;
                }
                Main.getInstance().getDictHandler().removeDict(guild.getId(), key);
                event.replyEmbeds(new EmbedBuilder()
                        .setTitle("辞書から単語を削除しました")
                        .addField("単語", key, true)
                        .build()
                ).queue();
                break;
            }
            case "add_json_dict_modal": {
                String json = event.getValues().get(0).getAsString();
                Guild guild = event.getGuild();
                if (guild == null) {
                    event.reply("このコマンドはサーバー内でのみ使用できます").setEphemeral(true).queue();
                    return;
                }

                Gson gson = new Gson();

                try {
                    Map<String, String> map = gson.fromJson(json, new TypeToken<Map<String, String>>() {
                    }.getType());

                    Main.getInstance().getDictHandler().addDictList(guild.getId(), map);
                    event.replyEmbeds(new EmbedBuilder()
                            .setTitle("辞書にJSONで単語を追加しました")
                            .addField("JSON", json, true)
                            .build()
                    ).queue();
                } catch (JsonSyntaxException e) {
                    event.reply("JSONの形式が正しくありません").setEphemeral(true).queue();
                }
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
        for (Map.Entry<String, String> entry : instance.getDictHandler().getDict(guildId).entrySet()) {
            message = message.replaceAll(entry.getKey(), entry.getValue());
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
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
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
                    AudioChannelUnion audioChannelUnion = voiceState.getChannel();
                    VoiceChannel voiceChannel = audioChannelUnion == null ? null : audioChannelUnion.asVoiceChannel();
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
                    AudioChannelUnion audioChannelUnion = audioManager.getConnectedChannel();
                    VoiceChannel voiceChannel = audioChannelUnion == null ? null : audioChannelUnion.asVoiceChannel();
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
                } else if (subCommandName.equalsIgnoreCase("ad")) {
                    Modal modal = Modal.create("add_dict_modal", "辞書に単語を追加")
                            .addComponents(ActionRow.of(
                                    TextInput.create("key","単語", TextInputStyle.SHORT)
                                            .setPlaceholder("単語を入力してください")
                                            .setRequired(true)
                                            .build()), ActionRow.of(
                                    TextInput.create("value","読み", TextInputStyle.SHORT)
                                            .setPlaceholder("読みを入力してください")
                                            .setRequired(true)
                                            .build()
                            )).build();

                    e.replyModal(modal).queue();
                } else if (subCommandName.equalsIgnoreCase("rd")) {
                    Modal modal = Modal.create("remove_dict_modal", "辞書から単語を削除")
                            .addComponents(ActionRow.of(
                                    TextInput.create("key","単語", TextInputStyle.SHORT)
                                            .setPlaceholder("単語を入力してください")
                                            .setRequired(true)
                                            .build()
                                    )
                            ).build();

                    e.replyModal(modal).queue();
                } else if (subCommandName.equalsIgnoreCase("ajd")) {
                    Modal modal = Modal.create("add_json_dict_modal", "辞書にJSONで単語を追加")
                            .addComponents(ActionRow.of(
                                            TextInput.create("json","JSON", TextInputStyle.PARAGRAPH)
                                                    .setPlaceholder("JSONを入力してください")
                                                    .setRequired(true)
                                                    .build()
                                    )
                            ).build();

                    e.replyModal(modal).queue();
                } else if (subCommandName.equalsIgnoreCase("d")) {
                    EmbedBuilder builder = new EmbedBuilder()
                            .setTitle("辞書")
                            .setDescription("辞書の一覧です");

                    for (Map.Entry<String, String> entry : instance.getDictHandler().getDict(guildId).entrySet()) {
                        builder.addField(entry.getKey(), entry.getValue(), true);
                    }

                    e.replyEmbeds(builder.build()).queue();
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

    private void sendHelpEmbed(SlashCommandInteractionEvent slashCommandInteractionEvent) {
        slashCommandInteractionEvent.replyEmbeds(
                new EmbedBuilder()
                        .setTitle("Info / 情報")
                        .setDescription("読み上げBot「Ondoku」の情報です")
                        .addField(
                                ":keyboard: **Commands**",
                                "`/ondoku i`　このヘルプを表示\n" +
                                        "`/ondoku s`　Ondoku を召喚します\n" +
                                        "`/ondoku p 数値`　声の高さを`[-24~24]`の間で変更します\n" +
                                        "`/ondoku c`　読み上げ声を切り替えます\n" +
                                        "`/ondoku ad`　単語を辞書に追加します\n" +
                                        "`/ondoku rd`　単語を辞書から削除します\n" +
                                        "`/ondoku ajd`　単語のJSONを辞書に追加します\n" +
                                        "`/ondoku d`　辞書の一覧を表示します\n" +
                                        "`/ondoku r`　Ondoku をリロードします"
                                , true)
                        .addField(
                                ":four_leaf_clover: **Thanks**",
                                """
                                        `OpenJTalk`　音声合成システム
                                        [HTS voice tohoku-f01](https://github.com/icn-lab/htsvoice-tohoku-f01)
                                        [VOICEVOX](https://voicevox.hiroshiba.jp/)
                                        VOICEVOX:四国めたん
                                        VOICEVOX:ずんだもん
                                        VOICEVOX:春日部つむぎ
                                        VOICEVOX:波音リツ
                                        VOICEVOX:玄野武宏
                                        VOICEVOX:白上虎太郎
                                        VOICEVOX:青山龍星
                                        VOICEVOX:冥鳴ひまり
                                        VOICEVOX:九州そら
                                        VOICEVOX:もち子(cv 明日葉よもぎ)
                                        VOICEVOX:剣崎雌雄
                                        VOICEVOX:WhiteCUL
                                        VOICEVOX:後鬼
                                        VOICEVOX:No.7
                                        VOICEVOX:ちび式じい
                                        VOICEVOX:櫻歌ミコ
                                        VOICEVOX:小夜/SAYO
                                        VOICEVOX:ナースロボ＿タイプＴ
                                        VOICEVOX:†聖騎士 紅桜†
                                        VOICEVOX:雀松朱司
                                        VOICEVOX:麒ヶ島宗麟
                                        VOICEVOX:春歌ナナ
                                        VOICEVOX:猫使アル
                                        VOICEVOX:猫使ビィ
                                        VOICEVOX:中国うさぎ
                                        VOICEVOX:栗田まろん
                                        VOICEVOX:あいえるたん
                                        VOICEVOX:満別花丸
                                        VOICEVOX:琴詠 ニア
                                        """
                                , true)
                        .setFooter("てれるんお手製 - OndokuAdvance " + VersionUtil.getVersion())
                        .build()
        ).queue();
    }

}
