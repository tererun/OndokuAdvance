package run.tere.bot.handlers;

import java.util.HashMap;
import java.util.Map;

public class GuildDictHandler {

    private String guildId;
    private HashMap<String, String> dict;

    public GuildDictHandler(String guildId) {
        this.guildId = guildId;
        this.dict = new HashMap<>();
    }

    public void addDictList(DictHandler dictHandler, Map<String, String> dict) {
        this.dict.putAll(dict);
        dictHandler.save();
    }

    public void addDict(DictHandler dictHandler, String key, String value) {
        dict.put(key, value);
        dictHandler.save();
    }

    public String getDict(String key) {
        return dict.get(key);
    }

    public HashMap<String, String> getDict() {
        return dict;
    }

    public void removeDict(DictHandler dictHandler, String key) {
        dict.remove(key);
        dictHandler.save();
    }

    public String getGuildId() {
        return guildId;
    }

}
