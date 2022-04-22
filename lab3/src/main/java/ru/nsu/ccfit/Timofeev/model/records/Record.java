package ru.nsu.ccfit.Timofeev.model.records;

import lombok.AllArgsConstructor;
import lombok.Getter;

import ru.nsu.ccfit.Timofeev.model.GameType;

@Getter
@AllArgsConstructor
public class Record {
    private String name;
    private int value;
    private GameType gameType;
}
