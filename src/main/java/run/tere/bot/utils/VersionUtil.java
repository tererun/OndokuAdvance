package run.tere.bot.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionUtil {
    public static String getVersion() {
        Properties properties = new Properties();
        try {
            InputStream inputStream = VersionUtil.class.getClassLoader().getResourceAsStream("version.properties");
            properties.load(inputStream);
            return properties.getProperty("version");
        } catch (IOException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}
