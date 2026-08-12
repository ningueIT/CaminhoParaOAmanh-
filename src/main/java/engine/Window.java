package engine;

import input.InputManager;

import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class Window extends JFrame {
    private final GamePanel gamePanel;

    public Window(String title) {
        super(title);

        InputManager inputManager = new InputManager();
        this.gamePanel = new GamePanel(inputManager);

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
}
