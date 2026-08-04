package com.sahmey.polashi.game;

public final class ChapterProgress {
    private static final int REJECTS_TO_LOSE = 3;

    private int consecutiveRejects = 0;

    public void reject() {
        consecutiveRejects++;
    }

    public void approve() {
        consecutiveRejects = 0;
    }

    public boolean eicWinsByRejection() {
        return consecutiveRejects >= REJECTS_TO_LOSE;
    }
}
