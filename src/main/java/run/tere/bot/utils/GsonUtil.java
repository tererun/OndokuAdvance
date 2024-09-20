package run.tere.bot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class GsonUtil {

    public static void toJson(File file, Object object) {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(object, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object fromJson(File file, Type typeOfT) {
        if (!file.exists()) return null;
        try (Reader reader = new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, typeOfT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
