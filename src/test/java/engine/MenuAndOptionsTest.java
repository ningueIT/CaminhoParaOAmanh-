package engine;

import input.InputManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuAndOptionsTest {
    private InputManager inputManager;
    private TestWindowController windowController;
    private GamePanel gamePanel;

    private static final class TestWindowController implements WindowController {
        private boolean fullScreen;

        @Override
        public boolean isFullScreen() {
            return fullScreen;
        }

        @Override
        public void setFullScreen(boolean fullScreen) {
            this.fullScreen = fullScreen;
        }
    }

    @BeforeEach
    void setUp() {
        inputManager = new InputManager();
        windowController = new TestWindowController();
        gamePanel = new GamePanel(inputManager, windowController);
    }

    @Test
    void startsInMainMenuWithPlayTabSelected() {
        assertEquals(GameState.MAIN_MENU, gamePanel.getGameState());
        assertEquals(GamePanel.MENU_TAB_PLAY, gamePanel.getActiveMenuTab());
    }

    @Test
    void tabSwitchingToOptionsAndBack() {
        // Press RIGHT to switch to options tab
        inputManager.keyPressed(new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);
        inputManager.keyReleased(new KeyEvent(gamePanel, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);

        assertEquals(GamePanel.MENU_TAB_OPTIONS, gamePanel.getActiveMenuTab());

        // Press ESCAPE to return to play tab
        inputManager.keyPressed(new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);
        inputManager.keyReleased(new KeyEvent(gamePanel, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);

        assertEquals(GamePanel.MENU_TAB_PLAY, gamePanel.getActiveMenuTab());
    }

    @Test
    void optionsTabAdjustVolume() {
        gamePanel.setActiveMenuTab(GamePanel.MENU_TAB_OPTIONS);
        gamePanel.setOptionsMenuSelection(0); // Volume option
        float initialVolume = gamePanel.getAudioManager().getVolume();

        // Press RIGHT to increase volume
        inputManager.keyPressed(new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);
        inputManager.keyReleased(new KeyEvent(gamePanel, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);

        assertTrue(gamePanel.getAudioManager().getVolume() > initialVolume);

        // Press LEFT to decrease volume
        inputManager.keyPressed(new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);
        inputManager.keyReleased(new KeyEvent(gamePanel, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);

        assertEquals(initialVolume, gamePanel.getAudioManager().getVolume(), 0.001f);
    }

    @Test
    void optionsTabToggleFullScreen() {
        gamePanel.setActiveMenuTab(GamePanel.MENU_TAB_OPTIONS);
        gamePanel.setOptionsMenuSelection(1); // Fullscreen option
        assertFalse(windowController.isFullScreen());

        // Press ENTER to toggle fullscreen
        inputManager.keyPressed(new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);
        inputManager.keyReleased(new KeyEvent(gamePanel, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);

        assertTrue(windowController.isFullScreen());

        // Toggle back
        inputManager.keyPressed(new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);
        inputManager.keyReleased(new KeyEvent(gamePanel, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);

        assertFalse(windowController.isFullScreen());
    }

    @Test
    void fullScreenToggleShortcutF11() {
        assertFalse(windowController.isFullScreen());

        inputManager.keyPressed(new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F11, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);
        inputManager.keyReleased(new KeyEvent(gamePanel, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_F11, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);

        assertTrue(windowController.isFullScreen());
    }

    @Test
    void playTabStartsGameOnConfirm() {
        assertEquals(GameState.MAIN_MENU, gamePanel.getGameState());
        assertEquals(GamePanel.MENU_TAB_PLAY, gamePanel.getActiveMenuTab());

        inputManager.keyPressed(new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));
        gamePanel.fixedUpdate(0.016);

        assertEquals(GameState.PLAYING, gamePanel.getGameState());
    }

    @Test
    void mouseClickSwitchTabsAndStartGame() {
        gamePanel.setSize(1280, 720);
        assertEquals(GamePanel.MENU_TAB_PLAY, gamePanel.getActiveMenuTab());

        // Click on Opções tab header
        inputManager.mousePressed(new java.awt.event.MouseEvent(gamePanel, java.awt.event.MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 700, 275, 1, false, java.awt.event.MouseEvent.BUTTON1));
        gamePanel.fixedUpdate(0.016);
        inputManager.mouseReleased(new java.awt.event.MouseEvent(gamePanel, java.awt.event.MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 700, 275, 1, false, java.awt.event.MouseEvent.BUTTON1));
        gamePanel.fixedUpdate(0.016);

        assertEquals(GamePanel.MENU_TAB_OPTIONS, gamePanel.getActiveMenuTab());

        // Click on Jogar tab header
        inputManager.mousePressed(new java.awt.event.MouseEvent(gamePanel, java.awt.event.MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 520, 275, 1, false, java.awt.event.MouseEvent.BUTTON1));
        gamePanel.fixedUpdate(0.016);
        inputManager.mouseReleased(new java.awt.event.MouseEvent(gamePanel, java.awt.event.MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 520, 275, 1, false, java.awt.event.MouseEvent.BUTTON1));
        gamePanel.fixedUpdate(0.016);

        assertEquals(GamePanel.MENU_TAB_PLAY, gamePanel.getActiveMenuTab());

        // Click on Iniciar jornada
        inputManager.mousePressed(new java.awt.event.MouseEvent(gamePanel, java.awt.event.MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 640, 370, 1, false, java.awt.event.MouseEvent.BUTTON1));
        gamePanel.fixedUpdate(0.016);
        inputManager.mouseReleased(new java.awt.event.MouseEvent(gamePanel, java.awt.event.MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 0, 640, 370, 1, false, java.awt.event.MouseEvent.BUTTON1));
        gamePanel.fixedUpdate(0.016);

        assertEquals(GameState.PLAYING, gamePanel.getGameState());
    }

    @Test
    void renderScaleCalculations() {
        gamePanel.setSize(1280, 720);
        assertEquals(1.0, gamePanel.getRenderScale(), 0.001);
        assertEquals(0, gamePanel.getRenderOffsetX());
        assertEquals(0, gamePanel.getRenderOffsetY());
        assertEquals(100, gamePanel.screenToGameX(100));
        assertEquals(200, gamePanel.screenToGameY(200));

        // 1920x1080 (16:9 full screen scale = 1.5)
        gamePanel.setSize(1920, 1080);
        assertEquals(1.5, gamePanel.getRenderScale(), 0.001);
        assertEquals(0, gamePanel.getRenderOffsetX());
        assertEquals(0, gamePanel.getRenderOffsetY());
        assertEquals(100, gamePanel.screenToGameX(150));
        assertEquals(200, gamePanel.screenToGameY(300));
    }
}
