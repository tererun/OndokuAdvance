package run.tere.bot.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import run.tere.bot.utils.HttpUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class VoiceAudioListener extends AudioEventAdapter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private AudioPlayerManager audioPlayerManager;
    private AudioPlayer audioPlayer;
    private Queue<String> audioQueue;

    public VoiceAudioListener(AudioPlayerManager audioPlayerManager, AudioPlayer audioPlayer) {
        this.audioPlayerManager = audioPlayerManager;
        this.audioPlayer = audioPlayer;
        this.audioQueue = new ConcurrentLinkedQueue<>();
        this.audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        log("Track ended: reason=" + endReason + ", track=" + track.getIdentifier() + ", queueSize=" + audioQueue.size());
        if (endReason.mayStartNext) {
            String next = audioQueue.poll();
            if (next != null) playVoice(next);
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, com.sedmelluq.discord.lavaplayer.tools.FriendlyException exception) {
        log("Track exception: track=" + track.getIdentifier() + ", message=" + exception.getMessage());
        exception.printStackTrace();
        playNext();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        log("Track stuck: track=" + track.getIdentifier() + ", thresholdMs=" + thresholdMs);
        playNext();
    }

    public void addQueue(String url) {
        if (audioPlayer.getPlayingTrack() == null) {
            log("Playing immediately: " + url);
            playVoice(url);
        } else {
            log("Added to queue (size=" + (audioQueue.size() + 1) + "): " + url);
            audioQueue.add(url);
        }
    }

    private void playVoice(String url) {
        String finalUrl = url;
        if (url.startsWith("coeiroink;")) {
            String[] split = url.split(";");
            try {
                finalUrl = HttpUtil.createCoeiroinkSynthesis(split[1], split[2], Integer.parseInt(split[3]));
            } catch (Exception e) {
                log("Failed to create CoeiroInk synthesis: " + e.getMessage());
                e.printStackTrace();
                playNext();
                return;
            }
        } else if (url.startsWith("voicevox;")) {
            String[] split = url.split(";");
            try {
                finalUrl = HttpUtil.createVoicevoxSynthesis(split[1], Integer.parseInt(split[2]));
            } catch (Exception e) {
                log("Failed to create Voicevox synthesis: " + e.getMessage());
                e.printStackTrace();
                playNext();
                return;
            }
        }
        String finalUrl1 = finalUrl;
        log("Loading track: " + finalUrl1);
        audioPlayerManager.loadItem(finalUrl, new FunctionalResultHandler(audioTrack -> {
            log("Track loaded successfully: " + audioTrack.getIdentifier());
            audioPlayer.playTrack(audioTrack);
        }, null, (() -> {
            log("Failed to load (no matches): " + finalUrl1);
            playNext();
        }), e -> {
            log("Error loading track: " + finalUrl1 + ", error=" + e.getMessage());
            e.printStackTrace();
            playNext();
        }));
    }

    private void playNext() {
        String next = audioQueue.poll();
        if (next != null) {
            playVoice(next);
        }
    }

    private static void log(String message) {
        System.out.println("[" + LocalDateTime.now().format(FORMATTER) + "] [VoiceAudio] " + message);
    }

}
