package run.tere.bot.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayDeque;
import java.util.Queue;

public class VoiceAudioListener extends AudioEventAdapter {

    private AudioPlayer audioPlayer;
    private Queue<AudioTrack> audioQueue;

    public VoiceAudioListener(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.audioQueue = new ArrayDeque<>();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        audioPlayer.playTrack(audioQueue.poll());
    }

    public void addQueue(AudioTrack audioTrack) {
        audioQueue.add(audioTrack);
        if (audioQueue.size() <= 1) {
            audioPlayer.playTrack(audioQueue.poll());
        }
    }

}
