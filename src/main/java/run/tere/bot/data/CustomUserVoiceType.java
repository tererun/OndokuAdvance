package run.tere.bot.data;

public enum CustomUserVoiceType {

    OPEN_JTALK("OpenJTalk"),
    VOICEVOX("VOICEVOX(ずんだもん)"),
    COEIROINK("自分の声");

    private String name;

    CustomUserVoiceType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
