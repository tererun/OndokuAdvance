package run.tere.bot.handlers;

import run.tere.bot.data.CustomUserVoiceData;
import run.tere.bot.utils.GsonUtil;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CustomUserVoiceHandler {

    private List<CustomUserVoiceData> customUserVoiceDataList;

    public CustomUserVoiceHandler() {
        this.customUserVoiceDataList = new ArrayList<>();
    }

    public CustomUserVoiceData getCustomUserVoiceData(String userId) {
        for (CustomUserVoiceData customUserVoiceData : customUserVoiceDataList) {
            if (customUserVoiceData.getUserId().equalsIgnoreCase(userId)) return customUserVoiceData;
        }
        return null;
    }

    public List<CustomUserVoiceData> getCustomUserVoiceDataList() {
        return customUserVoiceDataList;
    }

    public void addCustomUserVoiceData(CustomUserVoiceData customUserVoiceData) {
        customUserVoiceDataList.add(customUserVoiceData);
        save();
    }

    public void save() {
        Path path = Paths.get("");
        File file = new File(path.toAbsolutePath().toFile(), "customUserVoiceHandler.json");
        GsonUtil.toJson(file, this);
    }

    public static CustomUserVoiceHandler load() {
        Path path = Paths.get("");
        File file = new File(path.toAbsolutePath().toFile(), "customUserVoiceHandler.json");
        CustomUserVoiceHandler customUserVoiceHandler = (CustomUserVoiceHandler) GsonUtil.fromJson(file, CustomUserVoiceHandler.class);
        if (customUserVoiceHandler == null) {
            customUserVoiceHandler = new CustomUserVoiceHandler();
            GsonUtil.toJson(file, customUserVoiceHandler);
        }
        return customUserVoiceHandler;
    }

}
