/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.ui.fx.scene;

import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx._2d.scene.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx._3d.model.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.sound.SoundManager;

/**
 * Scenes of the Ms. Pac-Man game variant.
 * 
 * @author Armin Reichert
 */
public class ScenesMsPacMan {

	public static final AbstractGameScene SCENES[][] = new AbstractGameScene[5][2];
	public static final Rendering2D_MsPacMan RENDERING = new Rendering2D_MsPacMan();
	public static final PacManModel3D MODEL_3D = GianmarcosModel3D.get();
	public static final SoundManager SOUNDS = new SoundManager();

	static {
		//@formatter:off
		SOUNDS.put(GameSounds.CREDIT,          "/mspacman/sound/Coin Credit.mp3");
		SOUNDS.put(GameSounds.EXTRA_LIFE,      "/mspacman/sound/Extra Life.mp3");
		SOUNDS.put(GameSounds.GAME_READY,      "/mspacman/sound/Start.mp3");
		SOUNDS.put(GameSounds.BONUS_EATEN,     "/mspacman/sound/Fruit.mp3");
		SOUNDS.put(GameSounds.PACMAN_MUNCH,    "/mspacman/sound/Ms. Pac Man Pill.mp3");
		SOUNDS.put(GameSounds.PACMAN_DEATH,    "/mspacman/sound/Died.mp3");
		SOUNDS.put(GameSounds.PACMAN_POWER,    "/mspacman/sound/Scared Ghost.mp3");
		SOUNDS.put(GameSounds.GHOST_EATEN,     "/mspacman/sound/Ghost.mp3");
		SOUNDS.put(GameSounds.GHOST_RETURNING, "/mspacman/sound/Ghost Eyes.mp3");
		SOUNDS.put(GameSounds.SIREN_1,   "/mspacman/sound/Ghost Noise 1.mp3");
		SOUNDS.put(GameSounds.SIREN_2,   "/mspacman/sound/Ghost Noise 2.mp3");
		SOUNDS.put(GameSounds.SIREN_3,   "/mspacman/sound/Ghost Noise 3.mp3");
		SOUNDS.put(GameSounds.SIREN_4,   "/mspacman/sound/Ghost Noise 4.mp3");
		SOUNDS.put(GameSounds.INTERMISSION_1,  "/mspacman/sound/They Meet Act 1.mp3");
		SOUNDS.put(GameSounds.INTERMISSION_2,  "/mspacman/sound/The Chase Act 2.mp3");
		SOUNDS.put(GameSounds.INTERMISSION_3,  "/mspacman/sound/Junior Act 3.mp3");
		//@formatter:on
	}

	public static void createScenes(PacManGameUI_JavaFX ui) {
		//@formatter:off
		SCENES[0][0] = 
		SCENES[0][1] = new MsPacMan_IntroScene(ui);
		SCENES[1][0] = 
		SCENES[1][1] = new MsPacMan_IntermissionScene1(ui);
		SCENES[2][0] = 
		SCENES[2][1] = new MsPacMan_IntermissionScene2(ui);
		SCENES[3][0] = 
		SCENES[3][1] = new MsPacMan_IntermissionScene3(ui);
		SCENES[4][0] = new PlayScene2D(ui, RENDERING, SOUNDS);
		SCENES[4][1] = new PlayScene3D(ui, MODEL_3D, SOUNDS);
		//@formatter:on
	}
}