package run.tere.bot.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import run.tere.bot.utils.HttpUtil;

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
        this.audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        String next = audioQueue.poll();
        if (next != null) playVoice(next);
    }

    public void addQueue(String url) {
        if (audioPlayer.getPlayingTrack() == null) {
            playVoice(url);
        } else {
            audioQueue.add(url);
        }
    }

    private void playVoice(String url) {
        String finalUrl = url;
        if (url.startsWith("coeiroink;")) {
            String[] split = url.split(";");
            try {
                finalUrl = HttpUtil.createFromSynthesis(split[1], split[2], Integer.parseInt(split[3]));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        String finalUrl1 = finalUrl;
        audioPlayerManager.loadItem(finalUrl, new FunctionalResultHandler(audioTrack -> {
            audioPlayer.playTrack(audioTrack);
        }, null, (() -> {
            System.out.println("failed to load: " + finalUrl1);
        }), Throwable::printStackTrace));
    }

}
