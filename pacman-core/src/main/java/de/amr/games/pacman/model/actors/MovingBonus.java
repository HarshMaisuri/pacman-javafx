/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.GameLevel;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.event.GameEventManager.publishGameEvent;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * A bonus that tumbles through the world, starting at some portal, making one round around the ghost house and leaving
 * the maze at some portal at the other border.
 *
 * <p>
 * That's however not exactly the original Ms. Pac-Man behaviour with predefined "fruit paths".
 *
 * @author Armin Reichert
 */
public class MovingBonus extends Creature implements Bonus {

    private final Pulse animation = new Pulse(10, false);
    private final byte symbol;
    private final int points;
    private byte state;
    private long countdown;
    private RouteBasedSteering steering;

    public MovingBonus(byte symbol, int points) {
        super("MovingBonus-" + symbol);
        this.symbol = symbol;
        this.points = points;
        reset();
        canTeleport = false; // override default from Creature
        countdown = 0;
        state = STATE_INACTIVE;
    }

    @Override
    public boolean canReverse() {
        return false;
    }

    @Override
    public boolean canAccessTile(Vector2i tile) {
        if (world.house().contains(tile)) {
            return false;
        }
        if (world.insideBounds(tile)) {
            return !world.isWall(tile);
        }
        return world.belongsToPortal(tile);
    }


    @Override
    public MovingBonus entity() {
        return this;
    }

    @Override
    public String toString() {
        return "MovingBonus{" +
            "symbol=" + symbol +
            ", points=" + points +
            ", countdown=" + countdown +
            ", state=" + stateName() +
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
        animation.stop();
        setSpeed(0);
        hide();
        state = STATE_INACTIVE;
        Logger.trace("Bonus inactive: {}", this);
    }

    @Override
    public void setEdible(long ticks) {
        animation.restart();
        setSpeed(0.5f); // how fast in the original game?
        setTargetTile(null);
        show();
        countdown = ticks;
        state = STATE_EDIBLE;
        Logger.trace("Bonus edible: {}", this);
    }

    @Override
    public void setEaten(long ticks) {
        animation.stop();
        countdown = ticks;
        state = STATE_EATEN;
        Logger.trace("Bonus eaten: {}", this);
    }

    public void setRoute(List<NavPoint> route, boolean leftToRight) {
        checkNotNull(route);
        centerOverTile(route.getFirst().tile());
        setMoveAndWishDir(leftToRight ? Direction.RIGHT : Direction.LEFT);
        route.removeFirst();
        steering = new RouteBasedSteering(route);
    }

    public float elongationY() {
        if (!animation.isRunning()) {
            return 0;
        }
        return animation.on() ? -3f : 3f;
    }

    @Override
    public void update(GameLevel level) {
        switch (state) {
            case STATE_INACTIVE -> {}
            case STATE_EDIBLE -> {
                steering.steer(this);
                if (steering.isComplete()) {
                    Logger.trace("Moving bonus reached target: {}", this);
                    setInactive();
                    publishGameEvent(level.game(), GameEventType.BONUS_EXPIRED, tile());
                } else {
                    navigateTowardsTarget();
                    tryMoving();
                    animation.tick();
                }
            }
            case STATE_EATEN -> {
                if (countdown == 0) {
                    setInactive();
                    Logger.trace("Bonus expired: {}", this);
                    publishGameEvent(level.game(), GameEventType.BONUS_EXPIRED, tile());
                } else if (countdown != TickTimer.INDEFINITE) {
                    --countdown;
                }
            }
            default -> throw new IllegalStateException("Unknown bonus state: " + state);
        }
    }
}