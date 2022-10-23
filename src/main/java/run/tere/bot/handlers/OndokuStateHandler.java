package run.tere.bot.handlers;

import run.tere.bot.data.OndokuStateData;

import java.util.ArrayList;
import java.util.List;

public class OndokuStateHandler {

    private List<OndokuStateData> ondokuStateDataList;

    public OndokuStateHandler() {
        this.ondokuStateDataList = new ArrayList<>();
    }

    public OndokuStateData getOndokuStateData(String guildId) {
        for (OndokuStateData ondokuStateData : ondokuStateDataList) {
            if (ondokuStateData.getGuildId().equalsIgnoreCase(guildId)) return ondokuStateData;
        }
        return null;
    }

    public List<OndokuStateData> getOndokuStateDataList() {
        return ondokuStateDataList;
    }

}
