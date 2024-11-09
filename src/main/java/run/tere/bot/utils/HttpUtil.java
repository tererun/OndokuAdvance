package run.tere.bot.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import run.tere.bot.Main;
import run.tere.bot.speakers.Speaker;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HttpUtil {

    private static class CoeiroinkSynthesisQuery {
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

    public static List<Speaker> getAvailableSpeakers() throws Exception {
        String urlString = Main.getInstance().getConfigData().getVoicevoxUri() + "/speakers";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader in = new BufferedReader(inputStreamReader)) {
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                Gson gson = new Gson();
                return gson.fromJson(response.toString(), new TypeToken<List<Speaker>>() {}.getType());
            }
        } else {
            throw new RuntimeException("HTTP request failed. Response Code: " + connection.getResponseCode());
        }
    }

    public static String createVoicevoxSynthesis(String text, int speakerId) throws IOException {
        String audioQueryUrlString = Main.getInstance().getConfigData().getVoicevoxUri() + "/audio_query";
        String synthesisUrlString = Main.getInstance().getConfigData().getVoicevoxUri() + "/synthesis";

        HashMap<String, String> audioQueryParams = new HashMap<>();
        audioQueryParams.put("text", text);
        audioQueryParams.put("speaker", String.valueOf(speakerId));

        HttpURLConnection audioQueryConnection = post(appendQueryParams(audioQueryUrlString, audioQueryParams), null);

        int audioQueryConnectionResponseCode = audioQueryConnection.getResponseCode();
        if (audioQueryConnectionResponseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(audioQueryConnection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                HashMap<String, String> synthesisParams = new HashMap<>();
                synthesisParams.put("speaker", String.valueOf(speakerId));

                String body = response.toString();

                System.out.println("BODY: " + body);

                return fetchWave(appendQueryParams(synthesisUrlString, synthesisParams), body);
            }
        } else {
            throw new RuntimeException("HTTP request failed. Response Code: " + audioQueryConnectionResponseCode);
        }
    }

    public static String createCoeiroinkSynthesis(String text, String speakerUuid, int styleId) throws Exception {
        String urlString = Main.getInstance().getConfigData().getCoeIroInkUri() + "/v1/synthesis";

        CoeiroinkSynthesisQuery query = new CoeiroinkSynthesisQuery();
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

        return fetchWave(urlString, jsonQuery);
    }

    @NotNull
    private static String fetchWave(String synthesisUrlString, String body) throws IOException {
        HttpURLConnection synthesisConnection = post(synthesisUrlString, body);

        int synthesisConnectionResponseCode = synthesisConnection.getResponseCode();
        if (synthesisConnectionResponseCode == HttpURLConnection.HTTP_OK) {
            String fileName = UUID.randomUUID() + ".wav";
            Path filePath = Paths.get("audio", fileName);
            Files.createDirectories(filePath.getParent());

            try (InputStream inputStream = synthesisConnection.getInputStream();
                 OutputStream outputStream = Files.newOutputStream(filePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return filePath.toAbsolutePath().toString();
        } else {
            throw new RuntimeException("HTTP request failed. Response Code: " + synthesisConnectionResponseCode);
        }
    }

    @NotNull
    private static String appendQueryParams(String baseUrl, HashMap<String, String> queryParams) {
        StringBuilder urlString = new StringBuilder(baseUrl);

        boolean first = true;
        for (String key : queryParams.keySet()) {
            if (first) {
                urlString.append("?").append(key).append("=").append(URLEncoder.encode(queryParams.get(key), StandardCharsets.UTF_8));
                first = false;
            } else {
                urlString.append("&").append(key).append("=").append(URLEncoder.encode(queryParams.get(key), StandardCharsets.UTF_8));
            }
        }

        return urlString.toString();
    }

    @NotNull
    private static HttpURLConnection post(String urlString, String bodyJson) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection audioQueryConnection = (HttpURLConnection) url.openConnection();
        audioQueryConnection.setRequestMethod("POST");
        audioQueryConnection.setDoOutput(true);
        audioQueryConnection.setRequestProperty("Content-Type", "application/json");

        if (bodyJson != null) {
            try (OutputStream os = audioQueryConnection.getOutputStream()) {
                byte[] input = bodyJson.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }

        return audioQueryConnection;
    }
}