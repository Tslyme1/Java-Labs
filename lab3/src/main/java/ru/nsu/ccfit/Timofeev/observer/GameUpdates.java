package ru.nsu.ccfit.Timofeev.observer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.nsu.ccfit.Timofeev.controller.GameListenerType;
import ru.nsu.ccfit.Timofeev.model.GameStatus;
import ru.nsu.ccfit.Timofeev.model.gameboard.GameBoardCell;

import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

@Getter
@AllArgsConstructor
public class GameUpdates {
    private GameBoardCell[][] gameBoard;
    private GameStatus gameStatus;
    private Integer boardHeight;
    private Integer boardWidth;
    private Integer minesAmount;
    private Integer flagsAmount;
    private ActionListener actionListener;
    private GameListenerType gameListenerType;
    private MouseListener mouseListener;
    private Integer x;
    private Integer y;
}
