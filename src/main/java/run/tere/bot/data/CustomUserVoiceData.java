package run.tere.bot.data;

import run.tere.bot.handlers.CustomUserVoiceHandler;

import java.util.Random;

public class CustomUserVoiceData {

    private String userId;
    private float pitch;
    private String voiceId;

    public CustomUserVoiceData(String userId) {
        Random random = new Random();
        this.userId = userId;
        this.pitch = 24 - random.nextInt(48);
        this.voiceId = null;
    }

    public String getUserId() {
        return userId;
    }

    public void setPitch(CustomUserVoiceHandler customUserVoiceHandler, float pitch) {
        this.pitch = pitch;
        customUserVoiceHandler.save();
    }

    public float getPitch() {
        return pitch;
    }

    public String getVoiceId() {
        return voiceId;
    }

}
