package utils;

import javafx.concurrent.Task;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioPlayer {
    private Clip audioClip;
    private AtomicBoolean isPlaying;
    private File audioFile;
    private long clipTimePosition;

    public AudioPlayer() {
        this.isPlaying = new AtomicBoolean(false);
        this.clipTimePosition = 0;
    }

    public boolean loadAudio(File file) {
        try {
            this.audioFile = file;
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);

            // Ajouter un listener pour mettre à jour l'état de lecture
            audioClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    isPlaying.set(false);
                    clipTimePosition = 0;
                }
            });

            return true;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void play() {
        if (audioClip != null && !isPlaying.get()) {
            audioClip.setMicrosecondPosition(clipTimePosition);
            audioClip.start();
            isPlaying.set(true);
        }
    }

    public void pause() {
        if (audioClip != null && isPlaying.get()) {
            clipTimePosition = audioClip.getMicrosecondPosition();
            audioClip.stop();
            isPlaying.set(false);
        }
    }

    public void stop() {
        if (audioClip != null) {
            audioClip.stop();
            clipTimePosition = 0;
            isPlaying.set(false);
        }
    }

    public boolean isPlaying() {
        return isPlaying.get();
    }

    public void setVolume(float volume) {
        if (audioClip != null) {
            FloatControl gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
            // volume entre 0 et 1
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }

    public long getDurationInSeconds() {
        if (audioClip != null) {
            return audioClip.getMicrosecondLength() / 1_000_000;
        }
        return 0;
    }

    public void seekTo(long microseconds) {
        if (audioClip != null) {
            boolean wasPlaying = isPlaying.get();
            if (wasPlaying) {
                audioClip.stop();
            }
            audioClip.setMicrosecondPosition(microseconds);
            clipTimePosition = microseconds;
            if (wasPlaying) {
                audioClip.start();
            }
        }
    }

    public long getCurrentPosition() {
        if (audioClip != null && isPlaying.get()) {
            return audioClip.getMicrosecondPosition();
        }
        return clipTimePosition;
    }

    public void close() {
        if (audioClip != null) {
            audioClip.close();
        }
    }
}