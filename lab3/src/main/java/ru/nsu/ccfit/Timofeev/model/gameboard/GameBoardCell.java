package ru.nsu.ccfit.Timofeev.model.gameboard;

import lombok.Getter;
import lombok.Setter;
import ru.nsu.ccfit.Timofeev.model.gameboard.cell.CellMarkStatus;
import ru.nsu.ccfit.Timofeev.model.gameboard.cell.CellMineStatus;
import ru.nsu.ccfit.Timofeev.model.gameboard.cell.CellRevealStatus;

@Getter
@Setter
public class GameBoardCell {

    private CellMarkStatus markStatus;
    private CellMineStatus mineStatus;
    private int numberStatus;
    private CellRevealStatus revealStatus;

    public GameBoardCell() {
        markStatus = CellMarkStatus.UNMARKED;
        mineStatus = CellMineStatus.NOT_MINED;
        numberStatus = 0;
        revealStatus = CellRevealStatus.UNREVEALED;
    }
}
