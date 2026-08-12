package engine;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class AudioManager {
    private final Map<String, Clip> clips = new HashMap<>();

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
