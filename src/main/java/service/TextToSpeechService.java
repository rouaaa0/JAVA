package service;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javafx.application.Platform;

/**
 * Service class to handle Text-to-Speech API calls.
 * Uses the ElevenLabs Text-to-Speech API instead of RapidAPI.
 * This version uses Desktop API to open audio in the system's default player.
 */
public class TextToSpeechService {
    // ElevenLabs API details - more reliable than RapidAPI
    private static final String API_URL = "https://api.elevenlabs.io/v1/text-to-speech";
    private static final String API_KEY = "sk_7a28a4e0d3fe509182fef1b959d237c9db79aab73d51864e"; // Replace with your ElevenLabs API key

    // Default voice IDs for ElevenLabs (these are some of their default voices)
    private static final String DEFAULT_VOICE_ID = "21m00Tcm4TlvDq8ikWAM"; // Rachel voice

    private File audioFile;
    private final ExecutorService executorService;
    private Process currentProcess;

    public TextToSpeechService() {
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Converts text to speech using the ElevenLabs API and plays the result.
     *
     * @param text The text to convert to speech
     * @param voice The voice style to use (mapped to ElevenLabs voices)
     * @param onSuccess Callback for successful conversion
     * @param onError Callback for error handling
     */
    public void convertTextToSpeech(String text, String voice,
                                    Runnable onSuccess, Consumer<Exception> onError) {
        executorService.submit(() -> {
            try {
                // Stop any existing playback
                stopPlayback();

                // Get appropriate voice ID based on requested voice style
                String voiceId = mapVoiceToElevenLabsVoiceId(voice);

                // Prepare request
                URL url = new URL(API_URL + "/" + voiceId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("xi-api-key", API_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Create JSON request body
                String jsonBody = String.format("{\"text\":\"%s\",\"model_id\":\"eleven_monolingual_v1\"}",
                        escapeJSON(text));

                connection.getOutputStream().write(jsonBody.getBytes("UTF-8"));

                // Check response code
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    // If unauthorized, retry with a fallback approach
                    if (responseCode == 401 || responseCode == 403) {
                        // Fallback to a local TTS approach
                        useFallbackTTS(text, onSuccess, onError);
                        return;
                    }
                    throw new IOException("API request failed with response code: " + responseCode);
                }

                // Save audio data to a temporary file
                audioFile = File.createTempFile("tts_audio", ".mp3");
                audioFile.deleteOnExit(); // Clean up the file when the JVM exits

                try (InputStream inputStream = connection.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(audioFile)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }

                // Play the audio file using Desktop API or system commands
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(audioFile);
                } else {
                    playWithSystemCommand(audioFile);
                }

                // Notify success on the JavaFX thread
                Platform.runLater(() -> {
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                if (onError != null) {
                    Platform.runLater(() -> onError.accept(e));
                }
            }
        });
    }

    /**
     * Maps OpenAI voice styles to ElevenLabs voice IDs
     */
    private String mapVoiceToElevenLabsVoiceId(String openAiVoice) {
        switch (openAiVoice) {
            case "alloy":
                return "pNInz6obpgDQGcFmaJgB"; // Adam
            case "echo":
                return "VR6AewLTigWG4xSOukaG"; // Josh
            case "fable":
                return "ErXwobaYiN019PkySvjV"; // Antoni
            case "onyx":
                return "SOYHLrjzK2X1ezoPC6cr"; // Daniel
            case "nova":
                return "EXAVITQu4vr4xnSDxMaL"; // Bella
            case "shimmer":
                return "21m00Tcm4TlvDq8ikWAM"; // Rachel
            default:
                return DEFAULT_VOICE_ID;
        }
    }

    /**
     * Fallback method when API is unavailable
     * Uses system TTS capabilities when available
     */
    private void useFallbackTTS(String text, Runnable onSuccess, Consumer<Exception> onError) {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;

            if (osName.contains("win")) {
                // Windows - use PowerShell's Speech Synthesizer
                String escapedText = text.replace("\"", "\\\"");
                processBuilder = new ProcessBuilder(
                        "powershell",
                        "-command",
                        "Add-Type -AssemblyName System.Speech; " +
                                "(New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('" + escapedText + "');"
                );
            } else if (osName.contains("mac")) {
                // macOS - use say command
                processBuilder = new ProcessBuilder("say", text);
            } else {
                // Linux - use espeak if available
                processBuilder = new ProcessBuilder("espeak", text);
            }

            currentProcess = processBuilder.start();
            currentProcess.waitFor();

            // Notify success
            Platform.runLater(() -> {
                if (onSuccess != null) {
                    onSuccess.run();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (onError != null) {
                Platform.runLater(() -> onError.accept(e));
            }
        }
    }

    /**
     * Play audio using system commands when Desktop API is not available
     */
    private void playWithSystemCommand(File file) throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();

        ProcessBuilder processBuilder;
        if (osName.contains("win")) {
            // Windows
            processBuilder = new ProcessBuilder("cmd", "/c", "start", "", file.getAbsolutePath());
        } else if (osName.contains("mac")) {
            // macOS
            processBuilder = new ProcessBuilder("open", file.getAbsolutePath());
        } else {
            // Linux and others
            processBuilder = new ProcessBuilder("xdg-open", file.getAbsolutePath());
        }

        currentProcess = processBuilder.start();
    }

    /**
     * Stops the current playback if any is in progress.
     */
    public void stopPlayback() {
        if (currentProcess != null) {
            currentProcess.destroyForcibly();
            currentProcess = null;
        }
    }

    /**
     * Escapes special characters in text for JSON
     */
    private String escapeJSON(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Get available voices for the API
     *
     * @return A map of voice IDs to display names
     */
    public static Map<String, String> getAvailableVoices() {
        Map<String, String> voices = new HashMap<>();
        voices.put("alloy", "Alloy (Neutral)");
        voices.put("echo", "Echo (Male)");
        voices.put("fable", "Fable (Male)");
        voices.put("onyx", "Onyx (Male)");
        voices.put("nova", "Nova (Female)");
        voices.put("shimmer", "Shimmer (Female)");
        return voices;
    }

    /**
     * Cleanup resources when service is no longer needed
     */
    public void shutdown() {
        stopPlayback();
        executorService.shutdown();
    }
}