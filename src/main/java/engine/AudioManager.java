package engine;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class AudioManager {
    private static final float PROCEDURAL_BGM_SAMPLE_RATE = 22_050.0f;
    private static final int PROCEDURAL_BGM_SECONDS = 4;
    private static final double[] PROCEDURAL_BGM_NOTES = {196.0, 246.94, 174.61, 220.0, 261.63, 196.0, 174.61, 220.0};

    private final Map<String, Clip> clips = new HashMap<>();
    private Clip backgroundMusic;
    private String backgroundMusicId;

    // Clip mantem o som em memoria, ideal para efeitos curtos como salto e ataque.
    public void loadClip(String id, String resourcePath)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        URL resource = AudioManager.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalArgumentException("Audio resource not found: " + resourcePath);
        }

        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(resource)) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clips.put(id, clip);
        }
    }

    public void play(String id) {
        Clip clip = requireClip(id);
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public void stop(String id) {
        requireClip(id).stop();
    }

    public boolean playBackgroundLoop(String id, String resourcePath) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(resourcePath, "resourcePath");
        if (id.isBlank() || resourcePath.isBlank()) {
            throw new IllegalArgumentException("Background music id and resourcePath must not be blank.");
        }
        if (backgroundMusic != null && backgroundMusic.isOpen() && id.equals(backgroundMusicId)) {
            return true;
        }

        URL resource = AudioManager.class.getResource(resourcePath);
        if (resource == null) {
            try {
                replaceBackgroundMusic(id, createProceduralBackgroundMusic());
                return false;
            } catch (LineUnavailableException exception) {
                throw new IllegalStateException("Unable to start procedural background music.", exception);
            }
        }

        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(resource)) {
            replaceBackgroundMusic(id, openClip(audioStream));
            return true;
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException exception) {
            throw new IllegalStateException("Unable to play background music: " + resourcePath, exception);
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic == null) {
            return;
        }

        backgroundMusic.stop();
        backgroundMusic.close();
        backgroundMusic = null;
        backgroundMusicId = null;
    }

    public void playEvent(SoundEffect soundEffect) {
        Objects.requireNonNull(soundEffect, "soundEffect");
        Clip clip = clips.get(soundEffect.getClipId());
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
            return;
        }

        // Mantem os eventos audiveis ate que os clips nomeados sejam carregados.
        Toolkit.getDefaultToolkit().beep();
    }

    public void closeAll() {
        stopBackgroundMusic();
        for (Clip clip : clips.values()) {
            clip.close();
        }
        clips.clear();
    }

    private Clip requireClip(String id) {
        Clip clip = clips.get(id);
        if (clip == null) {
            throw new IllegalArgumentException("Clip not loaded: " + id);
        }
        return clip;
    }

    private void replaceBackgroundMusic(String id, Clip nextBackgroundMusic) {
        Clip previousBackgroundMusic = backgroundMusic;
        backgroundMusic = nextBackgroundMusic;
        backgroundMusicId = id;
        backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);

        if (previousBackgroundMusic != null) {
            previousBackgroundMusic.stop();
            previousBackgroundMusic.close();
        }
    }

    private Clip createProceduralBackgroundMusic() throws LineUnavailableException {
        int sampleCount = (int) (PROCEDURAL_BGM_SAMPLE_RATE * PROCEDURAL_BGM_SECONDS);
        byte[] samples = new byte[sampleCount];

        for (int index = 0; index < sampleCount; index++) {
            double timeSeconds = index / PROCEDURAL_BGM_SAMPLE_RATE;
            int noteIndex = ((int) (timeSeconds * 2.0)) % PROCEDURAL_BGM_NOTES.length;
            double frequency = PROCEDURAL_BGM_NOTES[noteIndex];
            double melody = Math.sin(Math.PI * 2.0 * frequency * timeSeconds);
            double harmony = Math.sin(Math.PI * 2.0 * (frequency * 0.5) * timeSeconds);
            samples[index] = (byte) Math.round((melody * 34.0) + (harmony * 18.0));
        }

        AudioFormat format = new AudioFormat(PROCEDURAL_BGM_SAMPLE_RATE, 8, 1, true, false);
        try (AudioInputStream stream = new AudioInputStream(
                new ByteArrayInputStream(samples),
                format,
                samples.length
        )) {
            return openClip(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to close procedural background stream.", exception);
        }
    }

    private Clip openClip(AudioInputStream audioStream) throws IOException, LineUnavailableException {
        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);
        return clip;
    }

    public enum SoundEffect {
        JUMP("jump"),
        DAMAGE("damage");

        private final String clipId;

        SoundEffect(String clipId) {
            this.clipId = clipId;
        }

        private String getClipId() {
            return clipId;
        }
    }
}
