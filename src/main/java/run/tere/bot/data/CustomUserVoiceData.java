package run.tere.bot.data;

import run.tere.bot.handlers.CustomUserVoiceHandler;

import java.util.Random;

public class CustomUserVoiceData {

    private String userId;
    private float pitch;
    private String voiceId;
    private int styleId;
    // deprecated
    private boolean coeIroInk;
    private CustomUserVoiceType customUserVoiceType;

    public CustomUserVoiceData(String userId) {
        Random random = new Random();
        this.userId = userId;
        this.pitch = 24 - random.nextInt(48);
        this.voiceId = null;
        this.styleId = 0;
        this.coeIroInk = false;
        this.customUserVoiceType = CustomUserVoiceType.OPEN_JTALK;
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

    public CustomUserVoiceType getCustomUserVoiceType() {
        return customUserVoiceType;
    }

    public void setCustomUserVoiceType(CustomUserVoiceHandler customUserVoiceHandler, CustomUserVoiceType customUserVoiceType) {
        this.customUserVoiceType = customUserVoiceType;
        customUserVoiceHandler.save();
    }

    public float getPitch() {
        return pitch;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public int getStyleId() {
        return styleId;
    }

}
