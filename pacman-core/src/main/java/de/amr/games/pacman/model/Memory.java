/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.model.actors.Ghost;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class Memory {
    public byte bonusReachedIndex; // 0=first, 1=second, -1=no bonus
    public boolean levelCompleted;
    public boolean pacKilled;
    public final List<Ghost> pacPrey = new ArrayList<>(4);
    public final List<Ghost> killedGhosts = new ArrayList<>(4);

    public Memory() {
        forgetEverything();
    }

    /**
     * Ich scholze jetzt.
     */
    public void forgetEverything() {
        bonusReachedIndex = -1;
        levelCompleted = false;
        pacKilled = false;
        pacPrey.clear();
        killedGhosts.clear();
    }

    @Override
    public String toString() {
        String levelCompleted = this.levelCompleted ? "Level completed" : "";
        String bonus = bonusReachedIndex != -1
            ? String.format("Bonus %d reached", bonusReachedIndex)
            : "";
        var power = new StringBuilder();
        if (!power.isEmpty()) {
            power.insert(0, "Pac power:");
        }
        String killed = pacKilled ? "Pac killed" : "";
        String prey = pacPrey.isEmpty() ? "" : String.format("Prey: %s", pacPrey);
        String killedGhosts = this.killedGhosts.isEmpty() ? "" : this.killedGhosts.toString();

        String summary = Stream.of(levelCompleted, bonus, power.toString(), killed, prey, killedGhosts)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining(" "));
        return summary.isBlank() ? "" : String.format("[Last frame: %s]", summary);
    }
}