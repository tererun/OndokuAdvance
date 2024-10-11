package run.tere.bot.utils;

import com.google.gson.Gson;
import run.tere.bot.Main;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

public class HttpUtil {

    private static class SynthesisQuery {
        String speakerUuid;
        int styleId;
        String text;
        double speedScale;
        double volumeScale;
        java.util.List<Object> prosodyDetail;
        double pitchScale;
        double intonationScale;
        double prePhonemeLength;
        double postPhonemeLength;
        int outputSamplingRate;
    }

    public static String createFromSynthesis(String text, String speakerUuid, int styleId) throws Exception {
        String urlString = Main.getInstance().getConfigData().getCoeIroInkUri() + "/v1/synthesis";

        SynthesisQuery query = new SynthesisQuery();
        query.speakerUuid = speakerUuid;
        query.styleId = styleId;
        query.text = text;
        query.speedScale = 1.0;
        query.volumeScale = 1.0;
        query.prosodyDetail = Collections.emptyList();
        query.pitchScale = 0.0;
        query.intonationScale = 1.0;
        query.prePhonemeLength = 0.1;
        query.postPhonemeLength = 0.5;
        query.outputSamplingRate = 24000;

        Gson gson = new Gson();
        String jsonQuery = gson.toJson(query);

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonQuery.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = UUID.randomUUID() + ".wav";
            Path filePath = Paths.get("audio", fileName);
            Files.createDirectories(filePath.getParent());

            try (InputStream inputStream = connection.getInputStream();
                 OutputStream outputStream = Files.newOutputStream(filePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return filePath.toAbsolutePath().toString();
        } else {
            throw new RuntimeException("HTTP request failed. Response Code: " + responseCode);
        }
    }
}