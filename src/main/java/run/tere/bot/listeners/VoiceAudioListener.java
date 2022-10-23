package run.tere.bot.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.List;

public class VoiceAudioListener extends AudioEventAdapter {

    private AudioPlayer audioPlayer;
    private List<String> queueMessages;

    public VoiceAudioListener(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        System.out.println("tract pause");
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        System.out.println("tract resum");
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        System.out.println("tract start");
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        System.out.println("tract exe");
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        System.out.println("tract str");
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        System.out.println(endReason);
    }

    public void addQueue(AudioTrack audioTrack, String message) {
        queueMessages.add(message);
        System.out.println("come here");
        System.out.println("a: " + audioTrack);
        audioPlayer.startTrack(audioTrack, false);
    }

}
