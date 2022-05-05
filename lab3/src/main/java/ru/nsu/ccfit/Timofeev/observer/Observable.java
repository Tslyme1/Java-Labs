package ru.nsu.ccfit.Timofeev.observer;

public interface Observable {
    void addObserver(MyObserver observer);
    void notifyObservers(GameUpdates gameUpdates);
}
