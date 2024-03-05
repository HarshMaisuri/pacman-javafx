/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.Steering;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.*;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.event.GameEventManager.publishGameEvent;
import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.NavPoint.np;
import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.model.actors.GhostState.*;

/**
 * A game level.
 *
 * @author Armin Reichert
 */
public class GameLevel {

    private final GameLevelData data;

    private final GameModel game;

    private final GhostHouseManagement ghostHouseManagement;

    /**
     * 1-based level number.
     */
    private final int levelNumber;

    private final boolean demoLevel;

    private final TickTimer huntingTimer = new TickTimer("HuntingTimer");

    /**
     * Memory of what happens during current frame.
     */
    private final Memory thisFrame = new Memory();

    private final World world;

    private final Pac pac;

    private final Ghost[] ghosts;

    private byte huntingPhaseIndex;

    private byte numGhostsKilledInLevel;

    private byte numGhostsKilledByEnergizer;

    private byte cruiseElroyState;

    public GameLevel(GameModel game, World world, int levelNumber, GameLevelData levelData, boolean demoLevel) {
        checkGameNotNull(game);
        checkNotNull(world);
        checkLevelNumber(levelNumber);
        checkNotNull(levelData);

        this.game = game;
        this.world = world;
        this.levelNumber = levelNumber;
        this.data = levelData;
        this.demoLevel = demoLevel;

        boolean isMsPacManGame = game.variant() == GameVariant.MS_PACMAN;

        pac = new Pac(isMsPacManGame ? "Ms. Pac-Man" : "Pac-Man");
        pac.setWorld(world);
        pac.setBaseSpeed(SPEED_AT_100_PERCENT);
        pac.setFadingTicks(PAC_POWER_FADING_TICKS); // not sure

        ghosts = new Ghost[] {
            new Ghost(RED_GHOST, "Blinky"),
            new Ghost(PINK_GHOST, "Pinky"),
            new Ghost(CYAN_GHOST, "Inky"),
            new Ghost(ORANGE_GHOST, isMsPacManGame ? "Sue" : "Clyde")
        };
        ghosts().forEach(ghost -> {
            ghost.setWorld(world);
            ghost.setHouse(world.house());
            ghost.setFnHuntingBehavior(isMsPacManGame ? this::ghostHuntsInMsPacManGame : this::ghostHuntsInPacManGame);
            ghost.setFnFrightenedBehavior(this::ghostRoamsThroughWorld);
            ghost.setRevivalPosition(ghostRevivalPosition(ghost.id()));
            ghost.setFnIsSteeringAllowed(dir -> isSteeringAllowed(ghost, dir));
            ghost.setBaseSpeed(SPEED_AT_100_PERCENT);
            ghost.setSpeedReturningToHouse(SPEED_GHOST_RETURNING_TO_HOUSE);
            ghost.setSpeedInsideHouse(SPEED_GHOST_INSIDE_HOUSE);
        });

        ghostHouseManagement = new GhostHouseManagement(this, world.house());

        bonusSymbols = new byte[2];
        bonusSymbols[0] = nextBonusSymbol();
        bonusSymbols[1] = nextBonusSymbol();

        Logger.trace("Game level {} ({}) created.", this.levelNumber, game.variant());
    }

    private Vector2i chasingTarget(byte ghostID) {
        return switch (ghostID) {
            // Blinky: attacks Pac-Man directly
            case RED_GHOST -> pac.tile();
            // Pinky: ambushes Pac-Man
            case PINK_GHOST -> pac.tilesAheadBuggy(4);
            // Inky: attacks from opposite side as Blinky
            case CYAN_GHOST -> pac.tilesAheadBuggy(2).scaled(2).minus(ghosts[RED_GHOST].tile());
            // Clyde/Sue: attacks directly but retreats if Pac is near
            case ORANGE_GHOST -> ghosts[ORANGE_GHOST].tile().euclideanDistance(pac.tile()) < 8
                ? ghostScatterTarget(ORANGE_GHOST)
                : pac.tile();
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public Direction initialGhostDirection(byte ghostID) {
        return switch (ghostID) {
            case RED_GHOST -> LEFT;
            case PINK_GHOST -> DOWN;
            case CYAN_GHOST, ORANGE_GHOST -> UP;
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public Vector2f initialGhostPosition(byte ghostID) {
        return switch (ghostID) {
            case RED_GHOST -> ArcadeWorld.HOUSE_DOOR.entryPosition();
            case PINK_GHOST -> ArcadeWorld.HOUSE_SEAT_MIDDLE;
            case CYAN_GHOST -> ArcadeWorld.HOUSE_SEAT_LEFT;
            case ORANGE_GHOST -> ArcadeWorld.HOUSE_SEAT_RIGHT;
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public Vector2f ghostRevivalPosition(byte ghostID) {
        return switch (ghostID) {
            case RED_GHOST, PINK_GHOST -> ArcadeWorld.HOUSE_SEAT_MIDDLE;
            case CYAN_GHOST -> ArcadeWorld.HOUSE_SEAT_LEFT;
            case ORANGE_GHOST -> ArcadeWorld.HOUSE_SEAT_RIGHT;
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public Vector2i ghostScatterTarget(byte ghostID) {
        return switch (ghostID) {
            case RED_GHOST -> ArcadeWorld.SCATTER_TARGET_RIGHT_UPPER_CORNER;
            case PINK_GHOST -> ArcadeWorld.SCATTER_TARGET_LEFT_UPPER_CORNER;
            case CYAN_GHOST -> ArcadeWorld.SCATTER_TARGET_RIGHT_LOWER_CORNER;
            case ORANGE_GHOST -> ArcadeWorld.SCATTER_TARGET_LEFT_LOWER_CORNER;
            default -> throw new IllegalGhostIDException(ghostID);
        };
    }

    public void end() {
        pac.setRestingTicks(Pac.REST_INDEFINITE);
        pac.selectAnimation(Pac.ANIM_MUNCHING);
        ghosts().forEach(Ghost::hide);
        deactivateBonus();
        world.mazeFlashing().reset();
        stopHuntingPhase();
        Logger.trace("Game level {} ({}) ended.", levelNumber, game.variant());
    }

    public GameLevelData data() {
        return data;
    }

    public GameModel game() {
        return game;
    }

    public TickTimer huntingTimer() {
        return huntingTimer;
    }

    public boolean isDemoLevel() {
        return demoLevel;
    }

    /**
     * @return level number, starting with 1.
     */
    public int number() {
        return levelNumber;
    }

    public World world() {
        return world;
    }

    public Pac pac() {
        return pac;
    }

    public void steerPac() {
        if (pac == null) {
            Logger.error("Cannot steer Pac: No Pac available");
        } else if (GameController.it().isAutopilotEnabled() || isDemoLevel()) {
            GameController.it().autopilot().steer(this);

        } else if (pac.steering().isPresent()) {
            pac.steering().get().steer(this);
        } else {
            Logger.error("Cannot steer Pac: No Pac steering available");
        }
    }

    /**
     * @param id ghost ID, one of {@link GameModel#RED_GHOST}, {@link GameModel#PINK_GHOST},
     *           {@value GameModel#CYAN_GHOST}, {@link GameModel#ORANGE_GHOST}
     * @return the ghost with the given ID
     */
    public Ghost ghost(byte id) {
        checkGhostID(id);
        return ghosts[id];
    }

    /**
     * @param states states specifying which ghosts are returned
     * @return all ghosts which are in any of the given states or all ghosts, if no states are specified
     */
    public Stream<Ghost> ghosts(GhostState... states) {
        if (states.length > 0) {
            return Stream.of(ghosts).filter(ghost -> ghost.is(states));
        }
        // when no states are given, return *all* ghosts (ghost.is() would return *no* ghosts!)
        return Stream.of(ghosts);
    }

    /**
     * @return Pac-Man and the ghosts in order RED, PINK, CYAN, ORANGE
     */
    public Stream<Creature> guys() {
        return Stream.of(pac, ghosts[RED_GHOST], ghosts[PINK_GHOST], ghosts[CYAN_GHOST], ghosts[ORANGE_GHOST]);
    }

    /**
     * @return information about what happened during the current simulation step
     */
    public Memory thisFrame() {
        return thisFrame;
    }

    /**
     * @return Blinky's "cruise elroy" state. Values: <code>0, 1, 2, -1, -2</code>. (0=off, negative=disabled).
     */
    public byte cruiseElroyState() {
        return cruiseElroyState;
    }

    /**
     * @param cruiseElroyState Values: <code>0, 1, 2, -1, -2</code>. (0=off, negative=disabled).
     */
    public void setCruiseElroyState(int cruiseElroyState) {
        if (cruiseElroyState < -2 || cruiseElroyState > 2) {
            throw new IllegalArgumentException(
                "Cruise Elroy state must be one of -2, -1, 0, 1, 2, but is " + cruiseElroyState);
        }
        this.cruiseElroyState = (byte) cruiseElroyState;
        Logger.trace("Cruise Elroy state set to {}", cruiseElroyState);
    }

    private void setCruiseElroyStateEnabled(boolean enabled) {
        if (enabled && cruiseElroyState < 0 || !enabled && cruiseElroyState > 0) {
            cruiseElroyState = (byte) (-cruiseElroyState);
            Logger.trace("Cruise Elroy state set to {}", cruiseElroyState);
        }
    }

    public int numGhostsKilledInLevel() {
        return numGhostsKilledInLevel;
    }

    public int numGhostsKilledByEnergizer() {
        return numGhostsKilledByEnergizer;
    }

    /**
     * @param ghost a ghost
     * @param dir   a direction
     * @return tells if the ghost can steer towards the given direction
     */
    public boolean isSteeringAllowed(Ghost ghost, Direction dir) {
        checkNotNull(ghost);
        checkNotNull(dir);
        if (upwardsBlockedTiles().isEmpty()) {
            return true;
        }
        // In the Pac-Man game, *hunting* ghosts cannot move upwards at specific tiles
        boolean upwardsBlocked = upwardsBlockedTiles().contains(ghost.tile());
        return !(upwardsBlocked && dir == UP && ghost.is(HUNTING_PAC));
    }

    public List<Vector2i> upwardsBlockedTiles() {
        return switch (game.variant()) {
            case MS_PACMAN -> Collections.emptyList();
            case PACMAN -> ArcadeWorld.PACMAN_RED_ZONE;
        };
    }

    /**
     * Hunting happens in different phases. Phases 0, 2, 4, 6 are scattering phases where the ghosts target for their
     * respective corners and circle around the walls in their corner, phases 1, 3, 5, 7 are chasing phases where the
     * ghosts attack Pac-Man.
     *
     * @param index hunting phase index (0..7)
     */
    public void startHuntingPhase(int index) {
        if (index < 0 || index > 7) {
            throw new IllegalArgumentException("Hunting phase index must be 0..7, but is " + index);
        }
        huntingPhaseIndex = (byte) index;
        var durations = game.huntingDurations(levelNumber);
        var ticks = durations[index] == -1 ? TickTimer.INDEFINITE : durations[index];
        huntingTimer.reset(ticks);
        huntingTimer.start();
        Logger.info("Hunting phase {} ({}, {} ticks / {} seconds) started. {}",
            index, currentHuntingPhaseName(), huntingTimer.duration(),
            (float) huntingTimer.duration() / GameModel.FPS, huntingTimer);
    }

    private void stopHuntingPhase() {
        huntingTimer.stop();
        Logger.info("Hunting timer stopped");
    }

    /**
     * Advances the current hunting phase and enters the next phase when the current phase ends. On every change between
     * phases, the living ghosts outside the ghost house reverse their move direction.
     *
     * @return if new hunting phase has been started
     */
    private boolean updateHuntingPhase() {
        if (huntingTimer.hasExpired()) {
            startHuntingPhase(huntingPhaseIndex + 1);
            return true;
        }
        huntingTimer.advance();
        return false;
    }

    /**
     * @return number of current phase <code>(0-7)
     */
    public int huntingPhaseIndex() {
        return huntingPhaseIndex;
    }

    /**
     * <p>
     * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* hunting/scatter phase. Some say,
     * the original intention had been to randomize the scatter target of *all* ghosts in Ms. Pac-Man
     * but because of a bug, only the scatter target of Blinky and Pinky would have been affected. Who knows?
     * </p>
     * <br>
     * <p>
     * Frightened ghosts choose a "random" direction when they enter a new tile. If the chosen direction
     * can be taken, it is stored and taken as soon as possible.
     * Otherwise, the remaining directions are checked in clockwise order.
     * </p>
     *
     * @see <a href="https://www.youtube.com/watch?v=eFP0_rkjwlY">YouTube: How Frightened Ghosts Decide Where to Go</a>
     */
    private void ghostHuntsInMsPacManGame(Ghost ghost) {
        boolean cruiseElroy = ghost.id() == RED_GHOST && cruiseElroyState > 0;
        if (scatterPhase().isPresent() && (ghost.id() == RED_GHOST || ghost.id() == PINK_GHOST)) {
            ghostRoamsThroughWorld(ghost);
        } else if (chasingPhase().isPresent() || cruiseElroy) {
            ghostFollowsTarget(ghost, chasingTarget(ghost.id()), huntingSpeedPercentage(ghost));
        } else {
            ghostFollowsTarget(ghost, ghostScatterTarget(ghost.id()), huntingSpeedPercentage(ghost));
        }
    }

    private void ghostHuntsInPacManGame(Ghost ghost) {
        boolean cruiseElroy = ghost.id() == RED_GHOST && cruiseElroyState > 0;
        if (chasingPhase().isPresent() || cruiseElroy) {
            ghostFollowsTarget(ghost, chasingTarget(ghost.id()), huntingSpeedPercentage(ghost));
        } else {
            ghostFollowsTarget(ghost, ghostScatterTarget(ghost.id()), huntingSpeedPercentage(ghost));
        }
    }

    private void ghostFollowsTarget(Ghost ghost, Vector2i targetTile, byte relSpeed) {
        ghost.setPercentageSpeed(relSpeed);
        ghost.setTargetTile(targetTile);
        ghost.navigateTowardsTarget();
        ghost.tryMoving();
    }

    private void ghostRoamsThroughWorld(Ghost ghost) {
        var speed = world().isTunnel(ghost.tile())
            ? data.ghostSpeedTunnelPercentage()
            : data.ghostSpeedFrightenedPercentage();
        if (!world.belongsToPortal(ghost.tile()) && (ghost.isNewTileEntered() || !ghost.moved())) {
            ghost.setWishDir(chooseFrightenedDirection(ghost));
        }
        ghost.setPercentageSpeed(speed);
        ghost.tryMoving();
    }

    private Direction chooseFrightenedDirection(Ghost ghost) {
        Direction opposite = ghost.moveDir().opposite();
        Direction dir = pseudoRandomDirection();
        while (dir == opposite || !ghost.canAccessTile(ghost.tile().plus(dir.vector()))) {
            dir = dir.nextClockwise();
        }
        return dir;
    }

    private Direction pseudoRandomDirection() {
        float rnd = Globals.randomFloat(0, 100);
        if (rnd < 16.3) return UP;
        if (rnd < 16.3 + 25.2) return RIGHT;
        if (rnd < 16.3 + 25.2 + 28.5) return DOWN;
        return LEFT;
    }

    /**
     * @return (optional) index of current scattering phase <code>(0-3)</code>
     */
    public Optional<Integer> scatterPhase() {
        return isEven(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    /**
     * @return (optional) index of current chasing phase <code>(0-3)</code>
     */
    public Optional<Integer> chasingPhase() {
        return isOdd(huntingPhaseIndex) ? Optional.of(huntingPhaseIndex / 2) : Optional.empty();
    }

    public String currentHuntingPhaseName() {
        return isEven(huntingPhaseIndex) ? "Scattering" : "Chasing";
    }

    /**
     * Pac-Man and the ghosts are placed at their initial positions and locked. The bonus, Pac-Man power timer and
     * energizer pulse are reset too.
     *
     * @param guysVisible if the guys are visible
     */
    public void letsGetReadyToRumble(boolean guysVisible) {
        pac.reset();
        pac.setPosition(ArcadeWorld.PAC_POSITION);
        pac.setMoveAndWishDir(Direction.LEFT);
        pac.setVisible(guysVisible);
        pac.resetAnimation();
        ghosts().forEach(ghost -> {
            ghost.reset();
            ghost.setPosition(initialGhostPosition(ghost.id()));
            ghost.setMoveAndWishDir(initialGhostDirection(ghost.id()));
            ghost.setVisible(guysVisible);
            ghost.setState(LOCKED);
            ghost.resetAnimation();
        });
        world.mazeFlashing().reset();
        world.energizerBlinking().reset();
    }

    /**
     * @param ghost a ghost
     * @return relative speed of ghost in percent of the base speed
     */
    public byte huntingSpeedPercentage(Ghost ghost) {
        if (world.isTunnel(ghost.tile())) {
            return data.ghostSpeedTunnelPercentage();
        }
        if (ghost.id() == RED_GHOST && cruiseElroyState == 1) {
            return data.elroy1SpeedPercentage();
        }
        if (ghost.id() == RED_GHOST && cruiseElroyState == 2) {
            return data.elroy2SpeedPercentage();
        }
        return data.ghostSpeedPercentage();
    }

    /* --- Here comes the main logic of the game. --- */

    private void collectInformation() {
        thisFrame.forgetEverything(); // Ich scholze jetzt!
        var pacTile = pac.tile();
        if (world.hasFoodAt(pacTile)) {
            thisFrame.foodFoundTile = pacTile;
            thisFrame.energizerFound = world.isEnergizerTile(pacTile);
        }
        if (thisFrame.energizerFound && data.pacPowerSeconds() > 0) {
            thisFrame.pacPowerStarts = true;
            thisFrame.pacPowerActive = true;
        } else {
            thisFrame.pacPowerFading = pac.powerTimer().remaining() == GameModel.PAC_POWER_FADING_TICKS;
            thisFrame.pacPowerLost = pac.powerTimer().hasExpired();
            thisFrame.pacPowerActive = pac.powerTimer().isRunning();
        }
    }

    private void handleFoodFound(Vector2i foodTile) {
        world.removeFood(foodTile);
        if (world.uneatenFoodCount() == 0) {
            thisFrame.levelCompleted = true;
        }
        if (isBonusReached()) {
            thisFrame.bonusReachedIndex += 1;
        }
        if (thisFrame.energizerFound) {
            numGhostsKilledByEnergizer = 0;
            pac.setRestingTicks(GameModel.RESTING_TICKS_ENERGIZER);
            int points = GameModel.POINTS_ENERGIZER;
            game.scorePoints(points);
            Logger.info("Scored {} points for eating energizer", points);
        } else {
            pac.setRestingTicks(GameModel.RESTING_TICKS_NORMAL_PELLET);
            game.scorePoints(GameModel.POINTS_NORMAL_PELLET);
        }
        ghostHouseManagement.onFoodFound();
        if (world.uneatenFoodCount() == data.elroy1DotsLeft()) {
            setCruiseElroyState(1);
        } else if (world.uneatenFoodCount() == data.elroy2DotsLeft()) {
            setCruiseElroyState(2);
        }
        publishGameEvent(game, GameEventType.PAC_FOUND_FOOD, foodTile);
    }

    private void handlePacPowerStarts() {
        pac.powerTimer().restartSeconds(data.pacPowerSeconds());
        Logger.info("{} power starting, duration {} ticks", pac.name(), pac.powerTimer().duration());
        ghosts(HUNTING_PAC).forEach(ghost -> ghost.setState(FRIGHTENED));
        ghosts(FRIGHTENED).forEach(Ghost::reverseAsSoonAsPossible);
        publishGameEvent(game, GameEventType.PAC_GETS_POWER);
    }

    private void handlePacPowerLost() {
        Logger.info("{} power ends, timer: {}", pac.name(), pac.powerTimer());
        pac.powerTimer().stop();
        pac.powerTimer().resetIndefinitely();
        huntingTimer.start();
        Logger.info("Hunting timer restarted");
        ghosts(FRIGHTENED).forEach(ghost -> ghost.setState(HUNTING_PAC));
        publishGameEvent(game, GameEventType.PAC_LOST_POWER);
    }

    public void simulateOneFrame() {
        collectInformation();

        // Food found?
        if (thisFrame.foodFoundTile != null) {
            pac.endStarving();
            handleFoodFound(thisFrame.foodFoundTile);
        } else {
            pac.starve();
        }

        // Level complete?
        if (thisFrame.levelCompleted) {
            logWhatHappenedThisFrame();
            return;
        }

        // Bonus?
        if (thisFrame.bonusReachedIndex != -1) {
            handleBonusReached(thisFrame.bonusReachedIndex);
        }

        // Pac power state changed?
        if (thisFrame.pacPowerStarts) {
            handlePacPowerStarts();
        } else if (thisFrame.pacPowerFading) {
            publishGameEvent(game, GameEventType.PAC_STARTS_LOSING_POWER);
        } else if (thisFrame.pacPowerLost) {
            handlePacPowerLost();
        }

        // Now check who gets killed
        thisFrame.pacPrey.clear();
        thisFrame.pacPrey.addAll(ghosts(FRIGHTENED).filter(pac::sameTile).toList());
        thisFrame.pacKilled = !GameController.it().isPacImmune() && ghosts(HUNTING_PAC).anyMatch(pac::sameTile);

        // Update world
        world.mazeFlashing().tick();
        world.energizerBlinking().tick();

        pac.update(this);

        unlockGhost();
        ghosts().forEach(ghost -> ghost.update(pac));

        // Update bonus
        if (bonus != null) {
            boolean eaten = checkPacEatsBonus(bonus);
            if (eaten) {
                publishGameEvent(game, GameEventType.BONUS_EATEN);
            }
            bonus.update(this);
        }

        // Update hunting timer
        if (thisFrame.pacPowerStarts || thisFrame.pacKilled) {
            stopHuntingPhase();
        } else {
            boolean huntingPhaseChange = updateHuntingPhase();
            if (huntingPhaseChange) {
                ghosts(HUNTING_PAC, LOCKED, LEAVING_HOUSE).forEach(Ghost::reverseAsSoonAsPossible);
            }
        }

        logWhatHappenedThisFrame();
    }

    public void simulateOneTestFrame(TickTimer timer, int lastTestedLevel) {
        if (number() <= lastTestedLevel) {
            if (timer.atSecond(0.5)) {
                guys().forEach(Creature::show);
            } else if (timer.atSecond(1.5)) {
                handleBonusReached(0);
            } else if (timer.atSecond(2.5)) {
                bonus().ifPresent(bonus -> bonus.setEaten(120));
                publishGameEvent(game, GameEventType.BONUS_EATEN);
            } else if (timer.atSecond(4.5)) {
                handleBonusReached(1);
            } else if (timer.atSecond(5.5)) {
                guys().forEach(Creature::hide);
                bonus().ifPresent(bonus -> bonus.setEaten(60));
                publishGameEvent(game, GameEventType.BONUS_EATEN);
            } else if (timer.atSecond(6.5)) {
                var flashing = world().mazeFlashing();
                flashing.restart(2 * data.numFlashes());
            } else if (timer.atSecond(12.0)) {
                end();
                game.createAndStartLevel(levelNumber + 1);
                timer.restartIndefinitely();
                publishGameEvent(game, GameEventType.LEVEL_STARTED);
            }
            world().energizerBlinking().tick();
            world().mazeFlashing().tick();
            ghosts().forEach(ghost -> ghost.update(pac()));
            bonus().ifPresent(bonus -> bonus.update(this));
        } else {
            GameController.it().restart(GameState.BOOT);
        }
    }

    private void logWhatHappenedThisFrame() {
        var memoText = thisFrame.toString();
        if (!memoText.isBlank()) {
            Logger.trace(memoText);
        }
    }

    /**
     * Called by cheat action only.
     */
    public void killAllHuntingAndFrightenedGhosts() {
        thisFrame.pacPrey.clear();
        thisFrame.pacPrey.addAll(ghosts(HUNTING_PAC, FRIGHTENED).toList());
        numGhostsKilledByEnergizer = 0;
        killEdibleGhosts();
    }

    public void killEdibleGhosts() {
        if (!thisFrame.pacPrey.isEmpty()) {
            thisFrame.pacPrey.forEach(this::killGhost);
            numGhostsKilledInLevel += (byte) thisFrame.pacPrey.size();
            if (numGhostsKilledInLevel == 16) {
                int points = GameModel.POINTS_ALL_GHOSTS_KILLED_IN_LEVEL;
                game.scorePoints(points);
                Logger.info("Scored {} points for killing all ghosts at level {}", points, levelNumber);
            }
        }
    }

    private void killGhost(Ghost ghost) {
        ghost.setKilledIndex(numGhostsKilledByEnergizer);
        ghost.setState(EATEN);
        int points = game.pointsForKillingGhost(numGhostsKilledByEnergizer);
        game.scorePoints(points);
        thisFrame.killedGhosts.add(ghost);
        numGhostsKilledByEnergizer += 1;
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
    }

    // Pac-Man

    public void onPacKilled() {
        pac.killed();
        ghostHouseManagement.onPacKilled();
        setCruiseElroyStateEnabled(false);
        Logger.info("{} died at tile {}", pac.name(), pac.tile());
    }

    private void unlockGhost() {
        ghostHouseManagement.unlockGhost().ifPresent(unlocked -> {
            Ghost ghost = unlocked.ghost();
            if (ghost.insideHouse(ghostHouseManagement.house())) {
                ghost.setState(LEAVING_HOUSE);
            } else {
                ghost.setMoveAndWishDir(LEFT);
                ghost.setState(HUNTING_PAC);
            }
            if (ghost.id() == ORANGE_GHOST && cruiseElroyState < 0) {
                // Blinky's "cruise elroy" state is re-enabled when orange ghost is unlocked
                setCruiseElroyStateEnabled(true);
            }
            Logger.info("{} unlocked: {}", ghost.name(), unlocked.reason());
        });
    }

    // Bonus Management

    private final byte[] bonusSymbols;
    private Bonus bonus;

    private byte nextBonusSymbol() {
        return switch (game.variant()) {
            case MS_PACMAN -> nextMsPacManBonusSymbol();
            // In the Pac-Man game variant, each level has a single bonus symbol appearing twice during the level
            case PACMAN -> switch (levelNumber) {
                case 1 -> GameModel.PACMAN_CHERRIES;
                case 2 -> GameModel.PACMAN_STRAWBERRY;
                case 3, 4 -> GameModel.PACMAN_PEACH;
                case 5, 6 -> GameModel.PACMAN_APPLE;
                case 7, 8 -> GameModel.PACMAN_GRAPES;
                case 9, 10 -> GameModel.PACMAN_GALAXIAN;
                case 11, 12 -> GameModel.PACMAN_BELL;
                default -> GameModel.PACMAN_KEY;
            };
        };
    }

    /**
     * (Got this info from Reddit user <b>damselindis</b>, see
     * <a href="https://www.reddit.com/r/Pacman/comments/12q4ny3/is_anyone_able_to_explain_the_ai_behind_the/">Reddit</a>)
     * <p>
     * <cite>
     * The exact fruit mechanics are as follows: After 64 dots are consumed, the game spawns the first fruit of the level.
     * After 176 dots are consumed, the game attempts to spawn the second fruit of the level. If the first fruit is still
     * present in the level when (or eaten very shortly before) the 176th dot is consumed, the second fruit will not
     * spawn. Dying while a fruit is on screen causes it to immediately disappear and never return.
     * The type of fruit is determined by the level count - levels 1-7 will always have two cherries, two strawberries,
     * etc. until two bananas on level 7. On level 8 and beyond, the fruit type is randomly selected using the weights in
     * the following table:
     * </cite>
     * </p>
     * <table>
     * <tr>
     *   <th>Cherry</th>
     *   <th>Strawberry</th>
     *   <th>Peach</th>
     *   <th>Pretzel</th>
     *   <th>Apple</th>
     *   <th>Pear</th>
     *   <th>Banana</th>
     * </tr>
     * <tr align="right">
     *   <td>5/32</td>
     *   <td>5/32</td>
     *   <td>5/32</td>
     *   <td>5/32</td>
     *   <td>4/32</td>
     *   <td>4/32</td>
     *   <td>4/32</td>
     * </tr>
     * </table>
     */
    private byte nextMsPacManBonusSymbol() {
        return switch (levelNumber) {
            case 1 -> GameModel.MS_PACMAN_CHERRIES;
            case 2 -> GameModel.MS_PACMAN_STRAWBERRY;
            case 3 -> GameModel.MS_PACMAN_ORANGE;
            case 4 -> GameModel.MS_PACMAN_PRETZEL;
            case 5 -> GameModel.MS_PACMAN_APPLE;
            case 6 -> GameModel.MS_PACMAN_PEAR;
            case 7 -> GameModel.MS_PACMAN_BANANA;
            default -> {
                int random = randomInt(0, 320);
                if (random <  50) yield GameModel.MS_PACMAN_CHERRIES;
                if (random < 100) yield GameModel.MS_PACMAN_STRAWBERRY;
                if (random < 150) yield GameModel.MS_PACMAN_ORANGE;
                if (random < 200) yield GameModel.MS_PACMAN_PRETZEL;
                if (random < 240) yield GameModel.MS_PACMAN_APPLE;
                if (random < 280) yield GameModel.MS_PACMAN_PEAR;
                yield GameModel.MS_PACMAN_BANANA;
            }
        };
    }

    public boolean isBonusReached() {
        return switch (game.variant()) {
            case MS_PACMAN -> world().eatenFoodCount() == 64 || world().eatenFoodCount() == 176;
            case PACMAN -> world().eatenFoodCount() == 70 || world().eatenFoodCount() == 170;
        };
    }

    public Optional<Bonus> bonus() {
        return Optional.ofNullable(bonus);
    }

    public byte bonusSymbol(int index) {
        return bonusSymbols[index];
    }

    public void deactivateBonus() {
        if (bonus != null) {
            bonus.setInactive();
        }
    }

    private boolean checkPacEatsBonus(Bonus bonus) {
        if (bonus.state() == Bonus.STATE_EDIBLE && pac.sameTile(bonus.entity())) {
            bonus.setEaten(GameModel.BONUS_POINTS_SHOWN_TICKS);
            game.scorePoints(bonus.points());
            Logger.info("Scored {} points for eating bonus {}", bonus.points(), this);
            return true;
        }
        return false;
    }

    /**
     * Handles bonus achievement (public access for unit tests and level test).
     *
     * @param bonusIndex bonus index (0 or 1).
     */
    public void handleBonusReached(int bonusIndex) {
        if (bonusIndex < 0 || bonusIndex > 1) {
            throw new IllegalArgumentException("Bonus index must be 0 or 1 but is " + bonusIndex);
        }
        switch (game.variant()) {
            case MS_PACMAN -> {
                if (bonusIndex == 1 && bonus != null && bonus.state() != Bonus.STATE_INACTIVE) {
                    Logger.info("First bonus still active, skip second one");
                    return;
                }
                byte symbol = bonusSymbols[bonusIndex];
                int points = GameModel.BONUS_VALUES_MS_PACMAN[symbol] * 100;
                bonus = createMovingBonus(symbol, points, RND.nextBoolean());
                bonus.setEdible(TickTimer.INDEFINITE);
                publishGameEvent(game, GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
            }
            case PACMAN -> {
                byte symbol = bonusSymbols[bonusIndex];
                int points = GameModel.BONUS_VALUES_PACMAN[symbol] * 100;
                bonus = new StaticBonus(symbol, points);
                bonus.entity().setPosition(ArcadeWorld.BONUS_POSITION);
                bonus.setEdible(randomInt(9 * FPS, 10 * FPS));
                publishGameEvent(game, GameEventType.BONUS_ACTIVATED, bonus.entity().tile());
            }
        }
    }

    /**
     * The moving bonus enters the world at a random portal, walks to the house entry, takes a tour around the house and
     * finally leaves the world through a random portal on the opposite side of the world.
     * <p>
     * TODO: This is not the exact behavior as in the original Arcade game.
     **/
    private Bonus createMovingBonus(byte symbol, int points, boolean leftToRight) {
        var houseHeight = world.house().size().y();
        var houseEntryTile = tileAt(world.house().door().entryPosition());
        var portals = world.portals();
        var entryPortal = portals.get(RND.nextInt(portals.size()));
        var exitPortal = portals.get(RND.nextInt(portals.size()));
        var startPoint = leftToRight
            ? np(entryPortal.leftTunnelEnd())
            : np(entryPortal.rightTunnelEnd());
        var exitPoint = leftToRight
            ? np(exitPortal.rightTunnelEnd().plus(1, 0))
            : np(exitPortal.leftTunnelEnd().minus(1, 0));

        var route = new ArrayList<NavPoint>();
        route.add(startPoint);
        route.add(np(houseEntryTile));
        route.add(np(houseEntryTile.plus(0, houseHeight + 1)));
        route.add(np(houseEntryTile));
        route.add(exitPoint);
        route.trimToSize();

        var movingBonus = new MovingBonus(symbol, points);
        movingBonus.setWorld(world);
        movingBonus.setBaseSpeed(SPEED_AT_100_PERCENT);
        movingBonus.setRoute(route, leftToRight);
        Logger.info("Moving bonus created, route: {} ({})", route, leftToRight ? "left to right" : "right to left");
        return movingBonus;
    }
}