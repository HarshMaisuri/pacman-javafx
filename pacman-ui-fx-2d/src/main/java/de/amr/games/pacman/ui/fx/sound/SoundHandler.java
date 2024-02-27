/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.sound;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.PauseTransition;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class SoundHandler {

    private final Theme theme;
    protected AudioClip voiceClip;
    protected final Animation voiceClipExecution = new PauseTransition();

    public SoundHandler(Theme theme) {
        this.theme = theme;
    }

    public AudioClip audioClip(GameVariant gameVariant, String clipName) {
        var prefix = gameVariant == GameVariant.MS_PACMAN ? "mspacman." : "pacman.";
        return theme.audioClip(prefix + clipName);
    }

    public void onGameEvent(GameEvent event) {
        boolean demoLevel = event.game.level().isPresent() && event.game.level().get().isDemoLevel();
        var gameVariant = event.game.variant();
        switch (event.type) {
            case BONUS_EATEN -> {
                if (!demoLevel) {
                    audioClip(gameVariant, "audio.bonus_eaten").play();
                }
            }
            case CREDIT_ADDED -> audioClip(gameVariant, "audio.credit").play();
            case EXTRA_LIFE_WON -> {
                if (!demoLevel) {
                    audioClip(gameVariant, "audio.extra_life").play();
                }
            }
            case GHOST_EATEN -> {
                if (!demoLevel) {
                    audioClip(gameVariant, "audio.ghost_eaten").play();
                }
            }
            case HUNTING_PHASE_STARTED -> {
                if (event.game.level().isPresent() && !demoLevel) {
                    var level = event.game.level().get();
                    level.scatterPhase().ifPresent(phase -> ensureSirenStarted(gameVariant, phase));
                }
            }
            case INTERMISSION_STARTED -> {
                int intermissionNumber = 0;
                if (GameController.it().state() == GameState.INTERMISSION_TEST) {
                    intermissionNumber = GameController.it().intermissionTestNumber;
                } else if (event.game.level().isPresent()) {
                    intermissionNumber = event.game.level().get().intermissionNumber();
                }
                if (intermissionNumber > 0) {
                    if (gameVariant == GameVariant.MS_PACMAN) {
                        audioClip(gameVariant, "audio.intermission." + intermissionNumber).play();
                    } else {
                        var clip = audioClip(gameVariant, "audio.intermission");
                        int cycleCount = intermissionNumber == 1 || intermissionNumber == 3 ? 2 : 1;
                        clip.setCycleCount(cycleCount);
                        clip.play();
                    }
                }
            }
            case LEVEL_STARTED -> {
                if (!demoLevel) {
                    event.game.level().ifPresent(level -> {
                        if (level.number() == 1) {
                            audioClip(gameVariant, "audio.game_ready").play();
                        }
                    });
                }
            }
            case PAC_DIED -> {
                if (!demoLevel) {
                    audioClip(gameVariant, "audio.pacman_death").play();
                }
            }
            case PAC_FOUND_FOOD -> {
                if (!demoLevel) {
                    // TODO this does not sound as in the original game
                    ensureLoop(audioClip(gameVariant, "audio.pacman_munch"), AudioClip.INDEFINITE);
                }
            }
            case PAC_LOST_POWER -> {
                if (!demoLevel) {
                    audioClip(gameVariant, "audio.pacman_power").stop();
                    event.game.level().ifPresent(level -> ensureSirenStarted(gameVariant, level.huntingPhase() / 2));
                }
            }
            case PAC_GETS_POWER -> {
                if (!demoLevel) {
                    stopSirens(gameVariant);
                    var clip = audioClip(gameVariant, "audio.pacman_power");
                    clip.stop();
                    clip.setCycleCount(AudioClip.INDEFINITE);
                    clip.play();
                }
            }
            case STOP_ALL_SOUNDS -> stopAllSounds();
            default -> {
                Logger.debug("Game event {} not handled", event);
            }
        }
    }

    public void stopAllSounds() {
        theme.audioClips().filter(clip -> clip != voiceClip).forEach(AudioClip::stop);
        Logger.trace("All sounds stopped");
    }

    private void startSiren(GameVariant gameVariant, int sirenIndex) {
        stopSirens(gameVariant);
        var clip = audioClip(gameVariant, "audio.siren." + (sirenIndex + 1));
        clip.setCycleCount(AudioClip.INDEFINITE);
        clip.play();
    }

    private Stream<AudioClip> sirens(GameVariant gameVariant) {
        return IntStream.rangeClosed(1, 4).mapToObj(i -> audioClip(gameVariant, "audio.siren." + i));
    }

    /**
     * @param sirenIndex index of siren (0..3)
     */
    public void ensureSirenStarted(GameVariant gameVariant, int sirenIndex) {
        if (sirens(gameVariant).noneMatch(AudioClip::isPlaying)) {
            startSiren(gameVariant, sirenIndex);
        }
    }

    public void stopSirens(GameVariant gameVariant) {
        sirens(gameVariant).forEach(AudioClip::stop);
    }

    public void ensureLoop(AudioClip clip, int repetitions) {
        if (!clip.isPlaying()) {
            clip.setCycleCount(repetitions);
            clip.play();
        }
    }

    public void ensureLoopEndless(AudioClip clip) {
        ensureLoop(clip, AudioClip.INDEFINITE);
    }

    public void playVoice(String name) {
        playVoice(name, 0);
    }

    public void playVoice(String name, double delaySeconds) {
        if (voiceClip != null && voiceClip.isPlaying()) {
            return; // don't interrupt voice
        }
        Logger.trace("Voice will start in {} seconds", delaySeconds);
        voiceClip = theme.audioClip(name);
        voiceClipExecution.setDelay(Duration.seconds(delaySeconds));
        voiceClipExecution.setOnFinished(e -> {
            voiceClip.play();
            Logger.trace("Voice started");
        });
        voiceClipExecution.play();
    }

    public void stopVoice() {
        if (voiceClip != null && voiceClip.isPlaying()) {
            voiceClip.stop();
            Logger.trace("Voice stopped");
        }
        if (voiceClipExecution.getStatus() == Status.RUNNING) {
            voiceClipExecution.stop();
            Logger.trace("Scheduled voice clip stopped");
        }
    }
}