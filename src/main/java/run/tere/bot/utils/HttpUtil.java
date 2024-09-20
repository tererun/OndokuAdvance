package run.tere.bot.utils;

import com.google.gson.Gson;
import com.sedmelluq.discord.lavaplayer.container.wav.WavAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import run.tere.bot.Main;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

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

    public static AudioTrack createFromSynthesis(String text, String speakerUuid) throws Exception {
        String urlString = Main.getInstance().getConfigData().getCoeIroInkUri() + "/v1/synthesis";

        SynthesisQuery query = new SynthesisQuery();
        query.speakerUuid = speakerUuid;
        query.styleId = 0;
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
            File tempFile = File.createTempFile("audio", ".wav");
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            long length = getWavDuration(tempFile);

            try (InputStream inputStream = new FileInputStream(tempFile)) {
                return new WavAudioTrack(
                        new AudioTrackInfo(
                                text,
                                "Synthesized Voice",
                                length,
                                "",
                                false,
                                ""
                        ),
                        new NonSeekableInputStream(inputStream)
                );
            }
        } else {
            throw new RuntimeException("HTTP request failed. Response Code: " + responseCode);
        }
    }

    private static long getWavDuration(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] header = new byte[44];
            inputStream.read(header);

            ByteBuffer byteBuffer = ByteBuffer.wrap(header);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

            if (!"RIFF".equals(new String(header, 0, 4)) || !"WAVE".equals(new String(header, 8, 4))) {
                throw new IOException("Invalid WAV file format");
            }

            int sampleRate = byteBuffer.getInt(24);
            int bitsPerSample = byteBuffer.getShort(34);
            int channels = byteBuffer.getShort(22);

            // データチャンクサイズの取得
            int dataSize = byteBuffer.getInt(40);

            // 長さ（ミリ秒）の計算

            return (long) (dataSize * 1000.0 / ((long) sampleRate * channels * ((double) bitsPerSample / 8)));
        }
    }

}
