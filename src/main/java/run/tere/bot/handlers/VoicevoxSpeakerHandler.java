package run.tere.bot.handlers;

import run.tere.bot.speakers.Speaker;
import run.tere.bot.utils.HttpUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VoicevoxSpeakerHandler {

    private List<Speaker> speakers;
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public Timer getTimer() {
        return timer;
    }
}
