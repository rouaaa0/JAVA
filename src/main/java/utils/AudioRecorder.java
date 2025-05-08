package utils;

import javafx.concurrent.Task;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioRecorder {
    private TargetDataLine line;
    private AudioFormat format;
    private ByteArrayOutputStream out;
    private AtomicBoolean isRecording;
    private long startTime;
    private long duration; // durée en secondes
    private Task<Void> recordingTask;

    public AudioRecorder() {
        this.isRecording = new AtomicBoolean(false);
        this.duration = 0;

        // Format Audio standard: 44.1kHz, 16-bit, mono
        this.format = new AudioFormat(44100, 16, 1, true, false);
    }

    public boolean startRecording() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Line not supported");
                return false;
            }

            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            isRecording.set(true);
            startTime = System.currentTimeMillis();
            out = new ByteArrayOutputStream();

            recordingTask = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        byte[] buffer = new byte[4096];
                        while (isRecording.get()) {
                            int count = line.read(buffer, 0, buffer.length);
                            if (count > 0) {
                                out.write(buffer, 0, count);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };

            Thread recordingThread = new Thread(recordingTask);
            recordingThread.setDaemon(true);
            recordingThread.start();

            return true;
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stopRecording() {
        if (isRecording.get()) {
            isRecording.set(false);
            duration = (System.currentTimeMillis() - startTime) / 1000; // en secondes

            if (line != null) {
                line.stop();
                line.close();
            }
        }
    }

    public File saveRecording(String outputFilePath) throws IOException {
        if (out == null || out.size() == 0) {
            throw new IOException("Aucun enregistrement disponible à sauvegarder.");
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());
        AudioInputStream ais = new AudioInputStream(bais, format, out.size() / format.getFrameSize());

        File outputFile = new File(outputFilePath);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);

        return outputFile;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isRecording() {
        return isRecording.get();
    }

    public byte[] getAudioData() {
        return out != null ? out.toByteArray() : new byte[0];
    }

    public AudioFormat getFormat() {
        return format;
    }
}