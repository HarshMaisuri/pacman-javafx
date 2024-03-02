/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import org.tinylog.Logger;

import static de.amr.games.pacman.event.GameEventManager.publishGameEvent;

/**
 * Bonus that appears for some time at a fixed position before it gets eaten or vanishes.
 *
 * @author Armin Reichert
 */
public class StaticBonus extends Entity implements Bonus {

    private final byte symbol;
    private final int points;
    private long countdown;
    private byte state;

    public StaticBonus(byte symbol, int points) {
        this.symbol = symbol;
        this.points = points;
        this.countdown = 0;
        this.state = Bonus.STATE_INACTIVE;
    }

    @Override
    public StaticBonus entity() {
        return this;
    }

    @Override
    public String toString() {
        return "StaticBonus{" +
            "symbol=" + symbol +
            ", points=" + points +
            ", countdown=" + countdown +
            ", state=" + state +
            '}';
    }

    @Override
    public byte state() {
        return state;
    }

    @Override
    public byte symbol() {
        return symbol;
    }

    @Override
    public int points() {
        return points;
    }

    @Override
    public void setInactive() {
        countdown = 0;
        visible = false;
        state = Bonus.STATE_INACTIVE;
        Logger.trace("Bonus gets inactive: {}", this);
    }

    @Override
    public void setEdible(long ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("Bonus edible time must be larger than zero");
        }
        countdown = ticks;
        visible = true;
        state = Bonus.STATE_EDIBLE;
        Logger.trace("Bonus gets edible: {}", this);
    }

    @Override
    public void setEaten(long ticks) {
        if (ticks <= 0) {
            throw new IllegalArgumentException("Bonus edible time must be larger than zero");
        }
        countdown = ticks;
        state = Bonus.STATE_EATEN;
        Logger.trace("Bonus eaten: {}", this);
    }

    @Override
    public void update(GameLevel level) {
        switch (state) {
            case STATE_INACTIVE -> {}
            case STATE_EDIBLE, STATE_EATEN -> {
                if (countdown == 0) {
                    setInactive();
                    publishGameEvent(level.game(), GameEventType.BONUS_EXPIRED, tile());
                } else if (countdown != TickTimer.INDEFINITE) {
                    --countdown;
                }
            }
            default -> throw new IllegalStateException("Unknown bonus state: " + state);
        }
    }
}