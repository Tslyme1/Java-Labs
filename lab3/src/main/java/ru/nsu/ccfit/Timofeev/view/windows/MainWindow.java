package ru.nsu.ccfit.Timofeev.view.windows;

import ru.nsu.ccfit.Timofeev.model.gameboard.GameBoardCell;
import ru.nsu.ccfit.Timofeev.model.gameboard.cell.CellMarkStatus;
import ru.nsu.ccfit.Timofeev.model.gameboard.cell.CellMineStatus;
import ru.nsu.ccfit.Timofeev.model.gameboard.cell.CellRevealStatus;
import ru.nsu.ccfit.Timofeev.view.GameImage;
import ru.nsu.ccfit.Timofeev.observer.GameUpdates;
import ru.nsu.ccfit.Timofeev.observer.MyObserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

public class MainWindow extends JFrame implements MyObserver {
    private final Container contentPane;
    private final GridBagLayout mainLayout;

    private JMenuItem newGameMenu;
    private JMenuItem aboutMenu;
    private JMenuItem highScoresMenu;
    private JMenuItem settingsMenu;
    private JMenuItem exitMenu;

    private JButton[][] cellButtons;
    private JLabel timerLabel;
    private JLabel bombsCounterLabel;

    public MainWindow() {
        super("Miner");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        createMenu();

        contentPane = getContentPane();
        mainLayout = new GridBagLayout();
        contentPane.setLayout(mainLayout);

        contentPane.setBackground(new Color(144, 158, 184));
    }

    public void setTimerValue(int value) {
        timerLabel.setText(String.valueOf(value));
    }


    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");

        gameMenu.add(newGameMenu = new JMenuItem("New Game"));
        gameMenu.addSeparator();
        gameMenu.add(aboutMenu = new JMenuItem("About"));
        gameMenu.add(highScoresMenu = new JMenuItem("High Scores"));
        gameMenu.add(settingsMenu = new JMenuItem("Settings"));
        gameMenu.addSeparator();
        gameMenu.add(exitMenu = new JMenuItem("Exit"));

        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }

    private void setNewGameMenuAction(ActionListener listener) {
        newGameMenu.addActionListener(listener);
    }

    private void setAboutMenuAction(ActionListener listener) {
        aboutMenu.addActionListener(listener);
    }

    private void setHighScoresMenuAction(ActionListener listener) {
        highScoresMenu.addActionListener(listener);
    }

    private void setSettingsMenuAction(ActionListener listener) {
        settingsMenu.addActionListener(listener);
    }

    private void setExitMenuAction(ActionListener listener) {
        exitMenu.addActionListener(listener);
    }

    private void setCellImage(int x, int y, GameImage gameImage) {
        cellButtons[y][x].setIcon(gameImage.getImageIcon());
    }

    private void setBombsCount(int bombsCount) {
        bombsCounterLabel.setText(String.valueOf(bombsCount));
        repaint();
    }

    private void createGameField(int rowsCount, int colsCount) {
        contentPane.removeAll();
        setPreferredSize(new Dimension(20 * colsCount + 70, 20 * rowsCount + 110));

        addButtonsPanel(createButtonsPanel(rowsCount, colsCount));
        addTimerImage();
        addTimerLabel(timerLabel = new JLabel("0"));
        addBombCounter(bombsCounterLabel = new JLabel("0"));
        addBombCounterImage();
        pack();
        setLocationRelativeTo(null);
    }

    private void setMouseListenerToCell(int y, int x, MouseListener mouseListener) {
        cellButtons[y][x].addMouseListener(mouseListener);
    }

    private JPanel createButtonsPanel(int numberOfRows, int numberOfCols) {
        cellButtons = new JButton[numberOfRows][numberOfCols];
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setPreferredSize(new Dimension(20 * numberOfCols, 20 * numberOfRows));
        buttonsPanel.setLayout(new GridLayout(numberOfRows, numberOfCols, 0, 0));
        for (int row = 0; row < numberOfRows; row++) {
            for (int col = 0; col < numberOfCols; col++) {
                final int x = col;
                final int y = row;
                cellButtons[y][x] = new JButton(GameImage.CLOSED.getImageIcon());
                buttonsPanel.add(cellButtons[y][x]);
            }
        }

        return buttonsPanel;
    }


    private void addButtonsPanel(JPanel buttonsPanel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.gridheight = 1;
        gbc.insets = new Insets(20, 20, 5, 20);
        mainLayout.setConstraints(buttonsPanel, gbc);
        contentPane.add(buttonsPanel);
    }

    private void addTimerImage() {
        JLabel label = new JLabel(GameImage.TIMER.getImageIcon());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 20, 0, 0);
        gbc.weightx = 0.1;
        mainLayout.setConstraints(label, gbc);
        contentPane.add(label);
    }

    private void addTimerLabel(JLabel timerLabel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 5, 0, 0);
        mainLayout.setConstraints(timerLabel, gbc);
        contentPane.add(timerLabel);
    }

    private void addBombCounter(JLabel bombsCounterLabel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.weightx = 0.7;
        mainLayout.setConstraints(bombsCounterLabel, gbc);
        contentPane.add(bombsCounterLabel);
    }

    private void addBombCounterImage() {
        JLabel label = new JLabel(GameImage.BOMB_ICON.getImageIcon());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 3;
        gbc.insets = new Insets(0, 5, 0, 20);
        gbc.weightx = 0.1;
        mainLayout.setConstraints(label, gbc);
        contentPane.add(label);
    }

    private void repaintBoard(GameBoardCell[][] board, int boardHeight, int boardWidth) {
        for (int i = 0; i < boardHeight; i++) {
            for (int j = 0; j < boardWidth; j++) {
                GameBoardCell tmpCell = board[i][j];
                if (tmpCell.getMarkStatus() == CellMarkStatus.MARKED) {
                    setCellImage(j, i, GameImage.MARKED);
                } else if (tmpCell.getRevealStatus() == CellRevealStatus.UNREVEALED) {
                    setCellImage(j, i, GameImage.CLOSED);
                } else if (tmpCell.getMineStatus() == CellMineStatus.MINED) {
                    setCellImage(j, i, GameImage.BOMB);
                } else {
                    switch (tmpCell.getNumberStatus()) {
                        case 0 -> {
                            setCellImage(j, i, GameImage.EMPTY);
                        }
                        case 1 -> {
                            setCellImage(j, i, GameImage.NUM_1);
                        }
                        case 2 -> {
                            setCellImage(j, i, GameImage.NUM_2);
                        }
                        case 3 -> {
                            setCellImage(j, i, GameImage.NUM_3);
                        }
                        case 4 -> {
                            setCellImage(j, i, GameImage.NUM_4);
                        }
                        case 5 -> {
                            setCellImage(j, i, GameImage.NUM_5);
                        }
                        case 6 -> {
                            setCellImage(j, i, GameImage.NUM_6);
                        }
                        case 7 -> {
                            setCellImage(j, i, GameImage.NUM_7);
                        }
                        case 8 -> {
                            setCellImage(j, i, GameImage.NUM_8);
                        }
                    }
                }
            }
        }
        repaint();
    }

    private void addListener(GameUpdates gameUpdates) {
        switch (gameUpdates.getGameListenerType()) {
            case EXIT -> {
                setExitMenuAction(gameUpdates.getActionListener());
            }
            case NEW_GAME -> {
                setNewGameMenuAction(gameUpdates.getActionListener());
            }
            case ABOUT_MENU -> {
                setAboutMenuAction(gameUpdates.getActionListener());
            }
            case HIGH_SCORES -> {
                setHighScoresMenuAction(gameUpdates.getActionListener());
            }
            case SETTINGS_MENU -> {
                setSettingsMenuAction(gameUpdates.getActionListener());
            }
            case MOUSE -> {
                setMouseListenerToCell(gameUpdates.getX(), gameUpdates.getY(),
                        gameUpdates.getMouseListener());
            }
        }
    }

    @Override
    public void update(GameUpdates gameUpdates) {

        switch (gameUpdates.getGameStatus()) {
            case PROCESS -> {
                setBombsCount(gameUpdates.getMinesAmount() -
                        gameUpdates.getFlagsAmount());
                repaintBoard(gameUpdates.getGameBoard(), gameUpdates.getBoardHeight(),
                        gameUpdates.getBoardWidth());
            }
            case NEW -> {
                createGameField(gameUpdates.getBoardHeight(), gameUpdates.getBoardWidth());
                setBombsCount(gameUpdates.getMinesAmount());
                setVisible(true);
                repaint();
            }
            case LISTENER_ADD -> {
                addListener(gameUpdates);
            }
            case NOTHING -> {
            }
        }
    }
}
