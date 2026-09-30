package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class InputManager implements KeyListener, MouseListener, MouseMotionListener {
    private final Set<Integer> pressedKeys = ConcurrentHashMap.newKeySet();
    private volatile boolean primaryAttackPressed;
    private volatile boolean mousePositionKnown;
    private volatile int mouseX;
    private volatile int mouseY;

    public boolean isMovingLeft() {
        return pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT);
    }

    public boolean isMovingRight() {
        return pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT);
    }

    public boolean isMovingUp() {
        return pressedKeys.contains(KeyEvent.VK_W) || pressedKeys.contains(KeyEvent.VK_UP);
    }

    public boolean isMovingDown() {
        return pressedKeys.contains(KeyEvent.VK_S) || pressedKeys.contains(KeyEvent.VK_DOWN);
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
        return primaryAttackPressed || pressedKeys.contains(KeyEvent.VK_F) || pressedKeys.contains(KeyEvent.VK_J);
    }

    public boolean isMagicPressed() {
        return pressedKeys.contains(KeyEvent.VK_Q);
    }

    public boolean isConfirmPressed() {
        return pressedKeys.contains(KeyEvent.VK_ENTER);
    }

    public boolean isUseHealthPotionPressed() {
        return pressedKeys.contains(KeyEvent.VK_1) || pressedKeys.contains(KeyEvent.VK_NUMPAD1);
    }

    public boolean isUseManaPotionPressed() {
        return pressedKeys.contains(KeyEvent.VK_2) || pressedKeys.contains(KeyEvent.VK_NUMPAD2);
    }

    public boolean hasMousePosition() {
        return mousePositionKnown;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
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

    @Override
    public void mouseClicked(MouseEvent event) {
    }

    @Override
    public void mousePressed(MouseEvent event) {
        updateMousePosition(event);
        if (event.getButton() == MouseEvent.BUTTON1) {
            primaryAttackPressed = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        updateMousePosition(event);
        if (event.getButton() == MouseEvent.BUTTON1) {
            primaryAttackPressed = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        updateMousePosition(event);
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        updateMousePosition(event);
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        updateMousePosition(event);
    }

    private void updateMousePosition(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
        mousePositionKnown = true;
    }
}
