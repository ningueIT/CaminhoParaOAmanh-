package engine;

import input.InputManager;

import javax.swing.JFrame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class Window extends JFrame implements WindowController {
    private final GamePanel gamePanel;
    private boolean fullScreen;

    public Window(String title) {
        super(title);

        InputManager inputManager = new InputManager();
        this.gamePanel = new GamePanel(inputManager, this);

        setContentPane(gamePanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                gamePanel.stop();
            }
        });
    }

    public void showWindow() {
        setVisible(true);
        gamePanel.requestFocusInWindow();
        gamePanel.start();
    }

    @Override
    public boolean isFullScreen() {
        return fullScreen;
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        if (this.fullScreen == fullScreen) {
            return;
        }
        this.fullScreen = fullScreen;

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        dispose();
        if (fullScreen) {
            setUndecorated(true);
            if (gd.isFullScreenSupported()) {
                try {
                    gd.setFullScreenWindow(this);
                } catch (Exception e) {
                    setExtendedState(JFrame.MAXIMIZED_BOTH);
                    setVisible(true);
                }
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                setVisible(true);
            }
        } else {
            if (gd.getFullScreenWindow() == this) {
                gd.setFullScreenWindow(null);
            }
            setUndecorated(false);
            setExtendedState(JFrame.NORMAL);
            setResizable(false);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }
        revalidate();
        gamePanel.revalidate();
        gamePanel.requestFocusInWindow();
    }
}
