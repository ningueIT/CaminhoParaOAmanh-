package engine;

public interface WindowController {
    boolean isFullScreen();
    void setFullScreen(boolean fullScreen);
    default void toggleFullScreen() {
        setFullScreen(!isFullScreen());
    }
}
