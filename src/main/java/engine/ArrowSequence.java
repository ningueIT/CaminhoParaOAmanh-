package engine;

import java.util.List;
import java.util.Objects;

public final class ArrowSequence {
    public enum Direction {
        UP("CIMA"),
        RIGHT("DIREITA"),
        DOWN("BAIXO"),
        LEFT("ESQUERDA");

        private final String label;

        Direction(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum Status {
        ACTIVE,
        COMPLETED,
        FAILED
    }

    public enum InputResult {
        CORRECT,
        COMPLETED,
        INCORRECT,
        INACTIVE
    }

    private final List<Direction> sequence;
    private final double timeLimitSeconds;
    private int currentIndex;
    private double remainingSeconds;
    private Status status = Status.ACTIVE;

    public ArrowSequence(List<Direction> sequence, double timeLimitSeconds) {
        this.sequence = List.copyOf(Objects.requireNonNull(sequence, "sequence"));
        if (this.sequence.isEmpty()) {
            throw new IllegalArgumentException("sequence must not be empty.");
        }
        if (timeLimitSeconds <= 0.0) {
            throw new IllegalArgumentException("timeLimitSeconds must be greater than zero.");
        }

        for (Direction direction : this.sequence) {
            Objects.requireNonNull(direction, "sequence direction");
        }

        this.timeLimitSeconds = timeLimitSeconds;
        this.remainingSeconds = timeLimitSeconds;
    }

    public void fixedUpdate(double deltaSeconds) {
        if (deltaSeconds < 0.0) {
            throw new IllegalArgumentException("deltaSeconds must not be negative.");
        }
        if (status != Status.ACTIVE) {
            return;
        }

        remainingSeconds = Math.max(0.0, remainingSeconds - deltaSeconds);
        if (remainingSeconds == 0.0) {
            status = Status.FAILED;
        }
    }

    public InputResult submit(Direction direction) {
        Objects.requireNonNull(direction, "direction");
        if (status != Status.ACTIVE) {
            return InputResult.INACTIVE;
        }

        if (sequence.get(currentIndex) != direction) {
            status = Status.FAILED;
            return InputResult.INCORRECT;
        }

        currentIndex++;
        if (currentIndex == sequence.size()) {
            status = Status.COMPLETED;
            return InputResult.COMPLETED;
        }

        return InputResult.CORRECT;
    }

    public List<Direction> getSequence() {
        return sequence;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public double getRemainingSeconds() {
        return remainingSeconds;
    }

    public double getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public Status getStatus() {
        return status;
    }
}
