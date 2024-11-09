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

    public boolean addDict(DictHandler dictHandler, String key, String value) {
        for (String dictKey : dict.keySet()) {
            if (dictKey.equalsIgnoreCase(key)) return false;
        }
        dict.put(key, value);
        dictHandler.save();
        return true;
    }

    public String getDict(String key) {
        return dict.get(key);
    }

    public HashMap<String, String> getDict() {
        return dict;
    }

    public boolean removeDict(DictHandler dictHandler, String key) {
        for (String dictKey : dict.keySet()) {
            if (dictKey.equalsIgnoreCase(key)) {
                dict.remove(key);
                dictHandler.save();
                return true;
            }
        }
        return false;
    }

    public String getGuildId() {
        return guildId;
    }

}
