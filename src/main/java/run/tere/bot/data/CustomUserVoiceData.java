package run.tere.bot.data;

import run.tere.bot.handlers.CustomUserVoiceHandler;

import java.util.Random;

public class CustomUserVoiceData {

    private String userId;
    private float pitch;
    private String voiceId;
    private boolean coeIroInk;

    public CustomUserVoiceData(String userId) {
        Random random = new Random();
        this.userId = userId;
        this.pitch = 24 - random.nextInt(48);
        this.voiceId = null;
        this.coeIroInk = false;
    }

    public String getUserId() {
        return userId;
    }

    public void setPitch(CustomUserVoiceHandler customUserVoiceHandler, float pitch) {
        this.pitch = pitch;
        customUserVoiceHandler.save();
    }

    public boolean isCoeIroInk() {
        return coeIroInk;
    }

    public void setCoeIroInk(CustomUserVoiceHandler customUserVoiceHandler, boolean coeIroInk) {
        this.coeIroInk = coeIroInk;
        customUserVoiceHandler.save();
    }

    public float getPitch() {
        return pitch;
    }

    public String getVoiceId() {
        return voiceId;
    }

}
