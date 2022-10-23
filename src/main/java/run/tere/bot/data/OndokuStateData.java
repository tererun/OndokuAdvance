package run.tere.bot.data;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import run.tere.bot.listeners.VoiceAudioListener;

public class OndokuStateData {

    private AudioPlayer audioPlayer;
    private AudioPlayerManager audioPlayerManager;
    private VoiceAudioListener voiceAudioListener;
    private String guildId;
    private String voiceChannelId;
    private String textChannelId;

    public OndokuStateData(AudioPlayer audioPlayer, AudioPlayerManager audioPlayerManager, VoiceAudioListener voiceAudioListener, String guildId, String voiceChannelId, String textChannelId) {
        this.audioPlayer = audioPlayer;
        this.audioPlayerManager = audioPlayerManager;
        this.voiceAudioListener = voiceAudioListener;
        this.guildId = guildId;
        this.voiceChannelId = voiceChannelId;
        this.textChannelId = textChannelId;
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }

    public VoiceAudioListener getVoiceAudioListener() {
        return voiceAudioListener;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getTextChannelId() {
        return textChannelId;
    }

    public String getVoiceChannelId() {
        return voiceChannelId;
    }

}
