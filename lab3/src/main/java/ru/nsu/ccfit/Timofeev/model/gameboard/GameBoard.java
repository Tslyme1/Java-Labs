package ru.nsu.ccfit.Timofeev.model.gameboard;

import lombok.Getter;
import lombok.Setter;
import ru.nsu.ccfit.Timofeev.controller.GameListenerType;
import ru.nsu.ccfit.Timofeev.model.GameStatus;
import ru.nsu.ccfit.Timofeev.model.GameType;
import ru.nsu.ccfit.Timofeev.model.gameboard.cell.CellMarkStatus;
import ru.nsu.ccfit.Timofeev.model.gameboard.cell.CellMineStatus;
import ru.nsu.ccfit.Timofeev.model.gameboard.cell.CellRevealStatus;
import ru.nsu.ccfit.Timofeev.model.gameboard.cell.ListedCellCoords;
import ru.nsu.ccfit.Timofeev.model.gameboard.cell.CellArea;
import ru.nsu.ccfit.Timofeev.observer.GameUpdates;
import ru.nsu.ccfit.Timofeev.observer.MyObserver;
import ru.nsu.ccfit.Timofeev.observer.Observable;

import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Setter
@Getter
public class GameBoard implements Observable {

    private static final int NOVICE_MINES_NUMBER = 10;
    private static final int MEDIUM_MINES_NUMBER = 40;
    private static final int EXPERT_MINES_NUMBER = 99;
    private static final int NOVICE_BOARD_WIDTH = 9;
    private static final int MEDIUM_BOARD_WIDTH = 16;
    private static final int EXPERT_BOARD_WIDTH = 30;
    private static final int NOVICE_BOARD_HEIGHT = 9;
    private static final int MEDIUM_BOARD_HEIGHT = 16;
    private static final int EXPERT_BOARD_HEIGHT = 16;

    private final Random random = new Random();
    private final List<MyObserver> observerList;

    private int boardWidth;
    private int boardHeight;
    private GameBoardCell[][] board;
    private int numberOfCells;
    private int numberOfMines;
    private int numberOfOpenedCells;
    private int numberOfFlags;
    private GameType gameType;
    private boolean isGameStarted;
    private boolean isGameEnded;
    private boolean isVictory;

    public GameBoard() {
        observerList = new ArrayList<>();
        setGameType(GameType.NOVICE);
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public void startNewGame() {
        isGameStarted = false;
        isGameEnded = false;
        isVictory = false;
        switch (this.gameType) {
            case NOVICE -> {
                boardWidth = NOVICE_BOARD_WIDTH;
                boardHeight = NOVICE_BOARD_HEIGHT;
            }
            case MEDIUM -> {
                boardWidth = MEDIUM_BOARD_WIDTH;
                boardHeight = MEDIUM_BOARD_HEIGHT;
            }
            case EXPERT -> {
                boardWidth = EXPERT_BOARD_WIDTH;
                boardHeight = EXPERT_BOARD_HEIGHT;
            }
        }
        numberOfCells = boardHeight * boardWidth;
        numberOfFlags = 0;
        numberOfOpenedCells = 0;
        numberOfMines = takeMinesAmount();
        board = new GameBoardCell[boardHeight][boardWidth];
        initBoard();
        notifyObservers(getUpdates(GameStatus.NEW));
    }

    public boolean isGameStarted(int y, int x) {
        if (isGameStarted) {
            return true;
        }
        beginGame(x, y);
        return false;
    }

    public void openUnrevealedCell(int y, int x, boolean withFlagsRemove, boolean withUpdate) {
        GameBoardCell tmpCell = board[y][x];
        if (tmpCell.getMarkStatus() == CellMarkStatus.MARKED && withFlagsRemove) {
            tmpCell.setMarkStatus(CellMarkStatus.UNMARKED);
            numberOfFlags--;
        } else if (tmpCell.getMarkStatus() == CellMarkStatus.MARKED && !withFlagsRemove) {
            if (withUpdate) {
                notifyObservers(getUpdates(GameStatus.NOTHING));
            }
            return;
        }
        if (tmpCell.getRevealStatus() == CellRevealStatus.REVEALED) {
            if (withUpdate) {
                notifyObservers(getUpdates(GameStatus.NOTHING));
            }
            return;
        }
        board[y][x].setRevealStatus(CellRevealStatus.REVEALED);
        numberOfOpenedCells++;
        if (tmpCell.getMineStatus() == CellMineStatus.MINED) {
            setAllUnrevealed();
            isGameEnded = true;
            if (withUpdate) {
                notifyObservers(getUpdates(GameStatus.PROCESS));
            }
            return;
        }
        if (tmpCell.getNumberStatus() == 0) {
            CellArea cellArea = new CellArea(boardWidth, boardHeight, y, x);

            for (int i = cellArea.getStartYPoint(); i < cellArea.getEndYPoint() + 1; i++) {
                for (int j = cellArea.getStartXPoint(); j < cellArea.getEndXPoint() + 1; j++) {
                    if (i == y && j == x) {
                        continue;
                    }
                    if (board[i][j].getRevealStatus() == CellRevealStatus.UNREVEALED) {
                        openUnrevealedCell(i, j, withFlagsRemove, withUpdate);
                    }
                }
            }
        }
        if (numberOfCells - numberOfOpenedCells == numberOfMines) {
            isVictory = true;
            if (withUpdate) {
                notifyObservers(getUpdates(GameStatus.PROCESS));
            }
            return;
        }
        if (withUpdate) {
            notifyObservers(getUpdates(GameStatus.PROCESS));
        }
    }

    public void scanNumber(int y, int x) {
        GameBoardCell tmpCell = board[y][x];
        if (tmpCell.getRevealStatus() == CellRevealStatus.UNREVEALED) {
            return;
        }
        if (tmpCell.getNumberStatus() > 0) {
            CellArea cellArea = new CellArea(boardWidth, boardHeight, y, x);
            int flagsNumber = countFlagsInArea(cellArea, y, x);
            if (flagsNumber != tmpCell.getNumberStatus()) {
                return;
            }
            openArea(cellArea, y, x);
            notifyObservers(getUpdates(GameStatus.PROCESS));
        }
    }

    public boolean isMarked(int y, int x) {
        return (board[y][x].getMarkStatus() == CellMarkStatus.MARKED);
    }

    public boolean isUnrevealed(int y, int x) {
        return (board[y][x].getRevealStatus() == CellRevealStatus.UNREVEALED);
    }

    public boolean isZeroNumberStatus(int y, int x) {
        return (board[y][x].getNumberStatus() == 0);
    }

    public void updateFlags(int y, int x) {
        if (isGameEnded) {
            return;
        }
        if (isUnrevealed(y, x)) {
            if (isMarked(y, x)) {
                removeFlag(y, x);
                notifyObservers(getUpdates(GameStatus.PROCESS));
                return;
            }
            if (isUnmarked(y, x)) {
                placeFlag(y, x);
                notifyObservers(getUpdates(GameStatus.PROCESS));
            }
        }
    }

    public void setActionListener(ActionListener actionListener, GameListenerType gameListenerType) {
        notifyObservers(getUpdates(actionListener, gameListenerType));
    }

    public void setMouseListener(MouseListener mouseListener, GameListenerType gameListenerType,
                                 int x, int y) {
        notifyObservers(getUpdates(x, y, mouseListener, gameListenerType));
    }

    private void beginGame(int x, int y) {
        placeMines(x, y);
        isGameStarted = true;
    }

    private void placeMines(int chosenX, int chosenY) {
        ArrayList<ListedCellCoords> cellsList = listCells(chosenX, chosenY);
        mineTheBoard(cellsList);
        placeNumbers();
    }

    private void openArea(CellArea cellArea, int y, int x) {
        for (int i = cellArea.getStartYPoint(); i < cellArea.getEndYPoint() + 1; i++) {
            for (int j = cellArea.getStartXPoint(); j < cellArea.getEndXPoint() + 1; j++) {
                if (i == y && j == x) {
                    continue;
                }
                if (board[i][j].getRevealStatus() == CellRevealStatus.UNREVEALED) {
                    openUnrevealedCell(i, j, false, false);
                }
            }
        }
    }

    private boolean isUnmarked(int y, int x) {
        return (board[y][x].getMarkStatus() == CellMarkStatus.UNMARKED);
    }

    private int countFlagsInArea(CellArea cellArea, int y, int x) {
        int flagsNumber = 0;
        for (int i = cellArea.getStartYPoint(); i < cellArea.getEndYPoint() + 1; i++) {
            for (int j = cellArea.getStartXPoint(); j < cellArea.getEndXPoint() + 1; j++) {
                if (i == y && j == x) {
                    continue;
                }
                if (board[i][j].getMarkStatus() == CellMarkStatus.MARKED) {
                    flagsNumber++;
                }
            }
        }
        return flagsNumber;
    }

    private void placeFlag(int y, int x) {
        if (numberOfMines > numberOfFlags) {
            board[y][x].setMarkStatus(CellMarkStatus.MARKED);
            numberOfFlags++;
        }
    }

    private void removeFlag(int y, int x) {
        if (board[y][x].getMarkStatus() == CellMarkStatus.MARKED) {
            board[y][x].setMarkStatus(CellMarkStatus.UNMARKED);
            numberOfFlags--;
        }
    }

    private void setAllUnrevealed() {
        for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                if (board[i][j].getMineStatus() == CellMineStatus.MINED) {
                    board[i][j].setRevealStatus(CellRevealStatus.REVEALED);
                    board[i][j].setMarkStatus(CellMarkStatus.UNMARKED);
                }
            }
        }
    }

    private void placeNumbers() {
        for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                if (board[i][j].getMineStatus() != CellMineStatus.MINED) {
                    board[i][j].setNumberStatus(countMinesAroundAmount(i, j));
                }
            }
        }
    }

    private int countMinesAroundAmount(int y, int x) {
        int countedMinesAmount = 0;

        CellArea cellArea = new CellArea(boardWidth, boardHeight, y, x);

        for (int i = cellArea.getStartYPoint(); i < cellArea.getEndYPoint() + 1; i++) {
            for (int j = cellArea.getStartXPoint(); j < cellArea.getEndXPoint() + 1; j++) {
                if (board[i][j].getMineStatus() == CellMineStatus.MINED) {
                    countedMinesAmount++;
                }
            }
        }

        return countedMinesAmount;
    }

    private ArrayList<ListedCellCoords> listCells(int chosenX, int chosenY) {
        ArrayList<ListedCellCoords> cellsList = new ArrayList<ListedCellCoords>();

        for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                if (chosenY != i || chosenX != j) {
                    cellsList.add(new ListedCellCoords(i, j));
                }
            }
        }
        return cellsList;
    }

    private void mineTheBoard(ArrayList<ListedCellCoords> cellsList) {
        int randomRange = boardWidth * boardHeight - 1;
        for (int i = 0; i < numberOfMines; i++) {
            int supposedCellIdx = random.nextInt(randomRange);
            ListedCellCoords tmpCoord = cellsList.remove(supposedCellIdx);
            board[tmpCoord.getYCoord()][tmpCoord.getXCoord()].setMineStatus(CellMineStatus.MINED);
            randomRange--;
        }
    }

    private int takeMinesAmount() {
        switch (gameType) {
            case NOVICE -> {
                return NOVICE_MINES_NUMBER;
            }
            case MEDIUM -> {
                return MEDIUM_MINES_NUMBER;
            }
            case EXPERT -> {
                return EXPERT_MINES_NUMBER;
            }
        }
        return 0;
    }

    private void initBoard() {
        for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                board[i][j] = new GameBoardCell();
            }
        }
    }

    private GameUpdates getUpdates(GameStatus gameStatus) {
        switch (gameStatus) {
            case PROCESS -> {
                return new GameUpdates(board, GameStatus.PROCESS, boardHeight,
                        boardWidth, numberOfMines, numberOfFlags, null,
                        null, null, null, null);
            }
            case NEW -> {
                System.out.println(boardHeight + " " + boardWidth + " " + numberOfMines);
                return new GameUpdates(board, GameStatus.NEW, boardHeight,
                        boardWidth, numberOfMines, numberOfFlags, null,
                        null, null, null, null);
            }
            case NOTHING -> {
                return new GameUpdates(null, GameStatus.NOTHING, null,
                        null, null, null, null,
                        null, null, null, null);
            }
            case LISTENER_ADD -> {
                return new GameUpdates(null, GameStatus.LISTENER_ADD, null,
                        null, null, null, null,
                        null, null, null, null);
            }
        }
        return new GameUpdates(board, GameStatus.PROCESS, boardHeight, boardWidth, numberOfMines,
                numberOfFlags, null, null, null,
                null, null);
    }

    private GameUpdates getUpdates(ActionListener actionListener,
                                   GameListenerType gameListenerType) {
        return new GameUpdates(null, GameStatus.LISTENER_ADD, null, null,
                null, null, actionListener, gameListenerType, null,
                null, null);
    }

    private GameUpdates getUpdates(int x, int y, MouseListener mouseListener,
                                   GameListenerType gameListenerType) {
        return new GameUpdates(null, GameStatus.LISTENER_ADD, null, null,
                null, null, null, gameListenerType,
                mouseListener, x, y);
    }

    @Override
    public void addObserver(MyObserver observer) {
        observerList.add(observer);
    }

    @Override
    public void notifyObservers(GameUpdates gameUpdates) {
        observerList.forEach(x -> x.update(gameUpdates));
    }
}
