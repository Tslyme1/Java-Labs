package ru.nsu.ccfit.Timofeev;

import ru.nsu.ccfit.Timofeev.controller.GameController;
import ru.nsu.ccfit.Timofeev.model.gameboard.GameBoard;
import ru.nsu.ccfit.Timofeev.view.windows.MainWindow;

public class Application {
    public static void main(String[] args) {
        GameBoard gameBoard = new GameBoard();
        MainWindow mainWindow = new MainWindow();
        GameController gameController = new GameController(gameBoard, mainWindow);
    }
}
