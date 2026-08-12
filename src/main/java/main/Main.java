package main;

import engine.Window;

import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Window window = new Window("O Caminho para o Amanhecer");
            window.showWindow();
        });
    }
}
