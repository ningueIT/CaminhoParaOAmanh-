package engine;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;

public final class Animation {
    private final BufferedImage[] frames;
    private final int frameDelay;
    private int currentFrameIndex;
    private int frameTicks;

    public Animation(BufferedImage[] frames, int frameDelay) {
        Objects.requireNonNull(frames, "frames");
        if (frames.length == 0) {
            throw new IllegalArgumentException("Animation must contain at least one frame.");
        }
        if (frameDelay <= 0) {
            throw new IllegalArgumentException("frameDelay must be greater than zero.");
        }

        this.frames = Arrays.copyOf(frames, frames.length);
        for (BufferedImage frame : this.frames) {
            Objects.requireNonNull(frame, "animation frame");
        }
        this.frameDelay = frameDelay;
    }

    public synchronized void update() {
        frameTicks++;
        if (frameTicks < frameDelay) {
            return;
        }

        frameTicks = 0;
        currentFrameIndex = (currentFrameIndex + 1) % frames.length;
    }

    public synchronized BufferedImage getCurrentFrame() {
        return frames[currentFrameIndex];
    }

    public synchronized void reset() {
        currentFrameIndex = 0;
        frameTicks = 0;
    }

    public int getFrameCount() {
        return frames.length;
    }
}
