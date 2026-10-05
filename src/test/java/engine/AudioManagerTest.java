package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AudioManagerTest {
    private AudioManager audioManager;

    @BeforeEach
    void setUp() {
        audioManager = new AudioManager();
    }

    @Test
    void defaultVolumeIsEightyPercent() {
        assertEquals(0.8f, audioManager.getVolume(), 0.001f);
        assertEquals(80, audioManager.getVolumePercentage());
    }

    @Test
    void setVolumeClampsBetweenZeroAndOne() {
        audioManager.setVolume(-0.5f);
        assertEquals(0.0f, audioManager.getVolume(), 0.001f);
        assertEquals(0, audioManager.getVolumePercentage());

        audioManager.setVolume(1.5f);
        assertEquals(1.0f, audioManager.getVolume(), 0.001f);
        assertEquals(100, audioManager.getVolumePercentage());

        audioManager.setVolume(0.42f);
        assertEquals(0.42f, audioManager.getVolume(), 0.001f);
        assertEquals(42, audioManager.getVolumePercentage());
    }

    @Test
    void increaseAndDecreaseVolume() {
        audioManager.setVolume(0.5f);
        audioManager.increaseVolume(0.1f);
        assertEquals(0.6f, audioManager.getVolume(), 0.001f);

        audioManager.decreaseVolume(0.2f);
        assertEquals(0.4f, audioManager.getVolume(), 0.001f);
    }
}
