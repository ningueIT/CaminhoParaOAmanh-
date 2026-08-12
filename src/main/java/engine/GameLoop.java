package engine;

public final class GameLoop implements Runnable {
    private static final double FIXED_TIME_STEP = 1.0 / 60.0;
    private static final double MAX_FRAME_TIME = 0.25;
    private static final int MAX_UPDATES_PER_FRAME = 5;

    private final GamePanel gamePanel;

    private Thread loopThread;
    private volatile boolean running;

    public GameLoop(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void start() {
        if (running) {
            return;
        }

        running = true;
        loopThread = new Thread(this, "game-loop");
        loopThread.start();
    }

    public void stop() {
        running = false;

        if (loopThread == null || Thread.currentThread() == loopThread) {
            return;
        }

        try {
            loopThread.join(500L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        long previousTime = System.nanoTime();
        double accumulator = 0.0;

        while (running) {
            long currentTime = System.nanoTime();
            double frameTime = (currentTime - previousTime) / 1_000_000_000.0;
            previousTime = currentTime;

            frameTime = Math.min(frameTime, MAX_FRAME_TIME);
            accumulator = Math.min(accumulator + frameTime, FIXED_TIME_STEP * MAX_UPDATES_PER_FRAME);

            int updates = 0;

            // Processa a lógica em passos fixos para manter física e input estáveis.
            while (accumulator >= FIXED_TIME_STEP && updates < MAX_UPDATES_PER_FRAME) {
                gamePanel.fixedUpdate(FIXED_TIME_STEP);
                accumulator -= FIXED_TIME_STEP;
                updates++;
            }

            if (updates == MAX_UPDATES_PER_FRAME && accumulator >= FIXED_TIME_STEP) {
                accumulator = FIXED_TIME_STEP;
            }

            double alpha = accumulator / FIXED_TIME_STEP;
            gamePanel.requestRender(alpha);
            sleepBriefly();
        }
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(1L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }
}
