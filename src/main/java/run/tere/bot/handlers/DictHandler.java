package run.tere.bot.handlers;

import run.tere.bot.utils.GsonUtil;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DictHandler {

    private HashMap<String, GuildDictHandler> guildDictHandlers;

    public DictHandler() {
        this.guildDictHandlers = new HashMap<>();
    }

    public void addDictList(String guildId, Map<String, String> dict) {
        GuildDictHandler guildDictHandler = guildDictHandlers.computeIfAbsent(guildId, GuildDictHandler::new);
        guildDictHandler.addDictList(this, dict);
    }

    public void addDict(String guildId, String key, String value) {
        GuildDictHandler guildDictHandler = guildDictHandlers.computeIfAbsent(guildId, GuildDictHandler::new);
        guildDictHandler.addDict(this, key, value);
    }

    public HashMap<String, String> getDict(String guildId) {
        GuildDictHandler guildDictHandler = guildDictHandlers.get(guildId);
        if (guildDictHandler == null) return null;
        return guildDictHandler.getDict();
    }

    public String getDictValue(String guildId, String key) {
        GuildDictHandler guildDictHandler = guildDictHandlers.get(guildId);
        if (guildDictHandler == null) return null;
        return guildDictHandler.getDict(key);
    }

    public void removeDict(String guildId, String key) {
        GuildDictHandler guildDictHandler = guildDictHandlers.get(guildId);
        if (guildDictHandler == null) return;
        guildDictHandler.removeDict(this, key);
    }

    public void save() {
        Path path = Paths.get("");
        File file = new File(path.toAbsolutePath().toFile(), "dictHandler.json");
        GsonUtil.toJson(file, this);
    }

    public static DictHandler load() {
        Path path = Paths.get("");
        File file = new File(path.toAbsolutePath().toFile(), "dictHandler.json");
        DictHandler dictHandler = (DictHandler) GsonUtil.fromJson(file, DictHandler.class);
        if (dictHandler == null) {
            dictHandler = new DictHandler();
            GsonUtil.toJson(file, dictHandler);
        }
        return dictHandler;
    }

}
