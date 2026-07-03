package ru.yandex.practicum;

public class TurnResult {
    private final String result;
    private final int remainingSteps;
    private final boolean gameOver;
    private final boolean won;

    public TurnResult(String result, int remainingSteps, boolean gameOver, boolean won) {
        this.result = result;
        this.remainingSteps = remainingSteps;
        this.gameOver = gameOver;
        this.won = won;
    }

    public String getResult() {
        return result;
    }

    public int getRemainingSteps() {
        return remainingSteps;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isWon() {
        return won;
    }
}
