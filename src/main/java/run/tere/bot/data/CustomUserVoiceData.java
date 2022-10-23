package run.tere.bot.data;

public class CustomUserVoiceData {

    private String userId;
    private String voiceId;

    public CustomUserVoiceData(String userId, String voiceId) {
        this.userId = userId;
        this.voiceId = voiceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getVoiceId() {
        return voiceId;
    }

}
