package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InputManager implements KeyListener {
    private final Set<Integer> pressedKeys = ConcurrentHashMap.newKeySet();

    public boolean isMovingLeft() {
        return pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT);
    }

    public boolean isMovingRight() {
        return pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT);
    }

    public boolean isJumpPressed() {
        return pressedKeys.contains(KeyEvent.VK_SPACE);
    }

    public boolean isDodging() {
        return pressedKeys.contains(KeyEvent.VK_SHIFT);
    }

    public boolean isInteracting() {
        return pressedKeys.contains(KeyEvent.VK_E);
    }

    public boolean isAttackPressed() {
        return pressedKeys.contains(KeyEvent.VK_F) || pressedKeys.contains(KeyEvent.VK_J);
    }

    public boolean isMagicPressed() {
        return pressedKeys.contains(KeyEvent.VK_Q);
    }

    public boolean isConfirmPressed() {
        return pressedKeys.contains(KeyEvent.VK_ENTER);
    }

    @Override
    public void keyTyped(KeyEvent event) {
    }

    @Override
    public void keyPressed(KeyEvent event) {
        pressedKeys.add(event.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent event) {
        pressedKeys.remove(event.getKeyCode());
    }
}
