package run.tere.bot.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayDeque;
import java.util.Queue;

public class VoiceAudioListener extends AudioEventAdapter {

    private AudioPlayerManager audioPlayerManager;
    private AudioPlayer audioPlayer;
    private Queue<String> audioQueue;

    public VoiceAudioListener(AudioPlayerManager audioPlayerManager, AudioPlayer audioPlayer) {
        this.audioPlayerManager = audioPlayerManager;
        this.audioPlayer = audioPlayer;
        this.audioQueue = new ArrayDeque<>();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        audioPlayerManager.loadItem(audioQueue.poll(), new FunctionalResultHandler(audioTrack -> {
            audioPlayer.playTrack(audioTrack);
        }, null, null, Throwable::printStackTrace));
    }

    public void addQueue(String url) {
        if (audioPlayer.getPlayingTrack() == null) {
            audioPlayerManager.loadItem(url, new FunctionalResultHandler(audioTrack -> audioPlayer.playTrack(audioTrack), null, null, Throwable::printStackTrace));
        } else {
            audioQueue.add(url);
        }
    }

}
