package ru.nsu.ccfit.Timofeev.controller;

import ru.nsu.ccfit.Timofeev.model.GameType;
import ru.nsu.ccfit.Timofeev.model.gameboard.GameBoard;
import ru.nsu.ccfit.Timofeev.model.records.Record;
import ru.nsu.ccfit.Timofeev.model.records.RecordsTable;
import ru.nsu.ccfit.Timofeev.observer.MyObserver;
import ru.nsu.ccfit.Timofeev.view.windows.*;

import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

public class GameController {

    private static final int NOVICE_INFO_PLACE = 0;
    private static final int MEDIUM_INFO_PLACE = 1;
    private static final int EXPERT_INFO_PLACE = 2;
    private static final int TIMER_SECOND = 1000;

    private final GameBoard gameBoard;
    private final MainWindow mainWindow;
    private final RecordsTable recordsTable;

    private int time;
    private Timer timer;

    public GameController(GameBoard gameBoard, MainWindow mainWindow) {
        this.gameBoard = gameBoard;
        this.mainWindow = mainWindow;
        this.gameBoard.addObserver(mainWindow);
        recordsTable = new RecordsTable();
    }

    public void addObserver(MyObserver observer) {
        gameBoard.addObserver(observer);
    }

    public void startGame() {
        gameBoard.startNewGame();
        gameBoard.setActionListener(new NewGameListener(), GameListenerType.NEW_GAME);
        gameBoard.setActionListener(new AboutMenuListener(), GameListenerType.ABOUT_MENU);
        gameBoard.setActionListener(new SettingsMenuListener(), GameListenerType.SETTINGS_MENU);
        gameBoard.setActionListener(new ExitListener(), GameListenerType.EXIT);
        gameBoard.setActionListener(new HighScoreMenuListener(), GameListenerType.HIGH_SCORES);
        addListenersToMines();
    }

    private void addListenersToMines() {
        for (int i = 0; i < gameBoard.getBoardHeight(); i++) {
            for (int j = 0; j < gameBoard.getBoardWidth(); j++) {
                gameBoard.setMouseListener(new ClicksListener(i, j), GameListenerType.MOUSE, i, j);
            }
        }
    }

    private void generateLoseWindow() {
        LoseWindow loseWindow = new LoseWindow(mainWindow);
        loseWindow.setNewGameListener(new NewGameListener());
        loseWindow.setExitListener(new ExitListener());
        loseWindow.setVisible(true);
    }

    private void generateWinWindow() {
        WinWindow winWindow = new WinWindow(mainWindow);
        gameBoard.setGameEnded(true);
        winWindow.setNewGameListener(new NewGameListener());
        winWindow.setExitListener(new ExitListener());
        winWindow.setVisible(true);
    }

    private void generateRecordWindow() {
        RecordsWindow recordsWindow = new RecordsWindow(mainWindow);
        recordsWindow.setNameListener(new RecordNameListener(time, recordsWindow));
        recordsWindow.setVisible(true);
    }

    private void initTimer() {
        MyTimerTask timerTask = new MyTimerTask();
        timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, TIMER_SECOND);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private class MyTimerTask extends TimerTask {

        public MyTimerTask() {
            time = 0;
        }

        @Override
        public void run() {
            mainWindow.setTimerValue(time);
            time++;
        }
    }

    private class NewGameListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            stopTimer();
            gameBoard.startNewGame();
            addListenersToMines();
        }
    }

    private class AboutMenuListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            AboutWindow aboutWindow = new AboutWindow(mainWindow);
            aboutWindow.setVisible(true);
        }
    }

    private class ExitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            mainWindow.dispose();
            System.exit(0);
        }
    }

    private class SettingsMenuListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            SettingsWindow settingsWindow = new SettingsWindow(mainWindow);
            settingsWindow.setGameTypeChoiceListener(new GameTypeChoiceListener(settingsWindow));
            settingsWindow.setVisible(true);
        }
    }

    private class HighScoreMenuListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            HighScoresWindow highScoresWindow = new HighScoresWindow(mainWindow);
            Record[] records = recordsTable.getRecords();
            highScoresWindow.setNoviceRecord(records[NOVICE_INFO_PLACE].getName(),
                    records[NOVICE_INFO_PLACE].getValue());
            highScoresWindow.setMediumRecord(records[MEDIUM_INFO_PLACE].getName(),
                    records[MEDIUM_INFO_PLACE].getValue());
            highScoresWindow.setExpertRecord(records[EXPERT_INFO_PLACE].getName(),
                    records[EXPERT_INFO_PLACE].getValue());
            highScoresWindow.setVisible(true);
        }
    }

    private class GameTypeChoiceListener implements ActionListener {

        SettingsWindow settingsWindow;

        public GameTypeChoiceListener(SettingsWindow settingsWindow) {
            this.settingsWindow = settingsWindow;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            GameType tmpGameType = settingsWindow.getAnswer();
            if (tmpGameType != gameBoard.getGameType()) {
                stopTimer();
                gameBoard.setGameType(tmpGameType);
                gameBoard.startNewGame();
                addListenersToMines();
            }
        }
    }

    private class RecordNameListener implements ActionListener {

        private final RecordsWindow recordsWindow;
        private final int value;

        public RecordNameListener(int value, RecordsWindow recordsWindow) {
            this.recordsWindow = recordsWindow;
            this.value = value;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = recordsWindow.takeTextFromNameField();
            if (name != null) {
                recordsTable.refreshRecordTable(name, value, gameBoard.getGameType());
            }
        }
    }

    private class ClicksListener implements MouseListener {

        private final int y;
        private final int x;

        public ClicksListener(int y, int x) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            switch (e.getButton()) {
                case MouseEvent.BUTTON1 -> {
                    if (gameBoard.isGameEnded()) {
                        break;
                    }
                    if (!gameBoard.isGameStarted(y, x)) {
                        initTimer();
                    }
                    if (gameBoard.isMarked(y, x)) {
                        break;
                    }
                    gameBoard.openUnrevealedCell(y, x, true, true);
                    if (gameBoard.isGameEnded()) {
                        stopTimer();
                        generateLoseWindow();
                    }
                    if (gameBoard.isVictory()) {
                        stopTimer();
                        if (recordsTable.isItRecord(time, gameBoard.getGameType())) {
                            generateRecordWindow();
                        }
                        generateWinWindow();
                    }
                }
                case MouseEvent.BUTTON2 -> {
                    if (gameBoard.isGameEnded()) {
                        break;
                    }
                    if (gameBoard.isUnrevealed(y, x)) {
                        break;
                    }
                    if (gameBoard.isZeroNumberStatus(y, x)) {
                        break;
                    }
                    gameBoard.scanNumber(y, x);
                    if (gameBoard.isGameEnded()) {
                        stopTimer();
                        generateLoseWindow();
                    }
                    if (gameBoard.isVictory()) {
                        stopTimer();
                        if (recordsTable.isItRecord(time, gameBoard.getGameType())) {
                            generateRecordWindow();
                        }
                        generateWinWindow();
                    }
                }
                case MouseEvent.BUTTON3 -> {
                    gameBoard.updateFlags(y, x);
                }
                default -> {
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }
}
