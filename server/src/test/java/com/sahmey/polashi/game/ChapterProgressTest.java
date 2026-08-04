package com.sahmey.polashi.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChapterProgressTest {
    @Test
    void thirdConsecutiveRejectTripsEicWin() {
        ChapterProgress progress = new ChapterProgress();
        progress.reject();
        assertFalse(progress.eicWinsByRejection());
        progress.reject();
        assertFalse(progress.eicWinsByRejection());
        progress.reject();
        assertTrue(progress.eicWinsByRejection());
    }

    @Test
    void approvalResetsRejectCounter() {
        ChapterProgress progress = new ChapterProgress();
        progress.reject();
        progress.reject();
        progress.approve();
        progress.reject();
        progress.reject();
        assertFalse(progress.eicWinsByRejection());
    }
}
