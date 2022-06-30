/*
MIT License

Copyright (c) 2022 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.games.pacman.ui.fx.sound;

import de.amr.games.pacman.model.common.GameSound;

/**
 * @author Armin Reichert
 *
 */
public class MsPacManGameSounds extends AbstractGameSounds {

	public MsPacManGameSounds() {
		//@formatter:off
		put(clips, GameSound.BONUS_EATEN,     "/de/amr/games/pacman/mspacman/sound/Fruit.mp3");
		put(clips, GameSound.CREDIT,          "/de/amr/games/pacman/mspacman/sound/Coin Credit.mp3");
		put(clips, GameSound.EXTRA_LIFE,      "/de/amr/games/pacman/mspacman/sound/Extra Life.mp3");
		put(clips, GameSound.GAME_READY,      "/de/amr/games/pacman/mspacman/sound/Start.mp3");
		put(clips, GameSound.GHOST_EATEN,     "/de/amr/games/pacman/mspacman/sound/Ghost.mp3");
		put(clips, GameSound.GHOST_RETURNING, "/de/amr/games/pacman/mspacman/sound/Ghost Eyes.mp3");
		put(clips, GameSound.INTERMISSION_1,  "/de/amr/games/pacman/mspacman/sound/They Meet Act 1.mp3");
		put(clips, GameSound.INTERMISSION_2,  "/de/amr/games/pacman/mspacman/sound/The Chase Act 2.mp3");
		put(clips, GameSound.INTERMISSION_3,  "/de/amr/games/pacman/mspacman/sound/Junior Act 3.mp3");
		put(clips, GameSound.PACMAN_MUNCH,    "/de/amr/games/pacman/mspacman/sound/Ms. Pac Man Pill.mp3");
		put(clips, GameSound.PACMAN_DEATH,    "/de/amr/games/pacman/mspacman/sound/Died.mp3");
		put(clips, GameSound.PACMAN_POWER,    "/de/amr/games/pacman/mspacman/sound/Scared Ghost.mp3");
		put(clips, GameSound.SIREN_1,         "/de/amr/games/pacman/mspacman/sound/Ghost Noise 1.mp3");
		put(clips, GameSound.SIREN_2,         "/de/amr/games/pacman/mspacman/sound/Ghost Noise 2.mp3");
		put(clips, GameSound.SIREN_3,         "/de/amr/games/pacman/mspacman/sound/Ghost Noise 3.mp3");
		put(clips, GameSound.SIREN_4,         "/de/amr/games/pacman/mspacman/sound/Ghost Noise 4.mp3");
		//@formatter:on
	}
}
