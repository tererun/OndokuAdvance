package run.tere.bot.handlers;

import run.tere.bot.Main;
import run.tere.bot.speakers.Speaker;
import run.tere.bot.speakers.SpeakerInfo;
import run.tere.bot.speakers.Style;
import run.tere.bot.utils.HttpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VoicevoxSpeakerHandler {

    private List<Speaker> speakers;
    private List<SpeakerInfo> speakerInfos;
    private Timer timer;

    public VoicevoxSpeakerHandler() {
        this.setupTimerTask();
    }

    private void setupTimerTask() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                fetchSpeakers();
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 1000 * 60 * 60 * 24);
    }

    public void fetchSpeakers() {
        try {
            speakers = HttpUtil.getAvailableSpeakers();
            speakerInfos = new ArrayList<>();
            for (Speaker speaker: Main.getInstance().getVoicevoxSpeakerHandler().getSpeakers()) {
                for (Style style: speaker.getStyles()) {
                    speakerInfos.add(new SpeakerInfo(
                            "voicevox;" + style.getId() + ";" + speaker.getName() + " - " + style.getName(),
                            speaker.getName() + " - " + style.getName()
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public List<SpeakerInfo> getSpeakerInfos() {
        return speakerInfos;
    }

    public Timer getTimer() {
        return timer;
    }
}
