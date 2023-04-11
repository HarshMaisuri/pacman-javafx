/*
MIT License

Copyright (c) 2023 Armin Reichert

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

package de.amr.games.pacman.ui.fx.app;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._3d.Model3D;
import de.amr.games.pacman.ui.fx._3d.entity.PacModel3D;
import de.amr.games.pacman.ui.fx.util.Picker;
import javafx.beans.binding.Bindings;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.PhongMaterial;

/**
 * @author Armin Reichert
 */
public class AppResources {

	private static final Logger LOG = LogManager.getFormatterLogger();

	public static PacModel3D MODEL3D_PAC;
	public static final String MESH_ID_EYES = "Sphere.008_Sphere.010_grey_wall";
	public static final String MESH_ID_HEAD = "Sphere_yellow_packman";
	public static final String MESH_ID_PALATE = "Sphere_grey_wall";

	public static Model3D MODEL3D_GHOST;
	public static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
	public static final String MESH_ID_GHOST_EYE_BALLS = "Sphere.009_Sphere.036_white";
	public static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";

	public static Model3D MODEL3D_PELLET;
	public static final String MESH_ID_PELLET = "Fruit";

	public static final String KEY_NO_TEXTURE = "No Texture";

	public static final String VOICE_HELP = "sound/common/press-key.mp3";
	public static final String VOICE_AUTOPILOT_OFF = "sound/common/autopilot-off.mp3";
	public static final String VOICE_AUTOPILOT_ON = "sound/common/autopilot-on.mp3";
	public static final String VOICE_IMMUNITY_OFF = "sound/common/immunity-off.mp3";
	public static final String VOICE_IMMUNITY_ON = "sound/common/immunity-on.mp3";

	private static final String[] READY_TEXTS = { "LET'S GO BRANDON!", "GHOST LIVES MATTER!", "YELLOW MAN BAD!",
			"KILL ME TOO!" };

	private static ResourceBundle messageBundle;
	private static Picker<String> messagePickerCheating;
	private static Picker<String> messagePickerLevelComplete;
	private static Picker<String> messagePickerGameOver;
	private static Image iconPacManGame;
	private static Image iconMsPacManGame;
	private static Map<String, PhongMaterial> textures = new LinkedHashMap<>();

	public static void load() {
		LOG.info("Loading application resources...");
		var start = System.nanoTime();

		messageBundle = ResourceBundle.getBundle("assets.texts.messages");
		messagePickerCheating = ResourceMgr.createPicker(messageBundle, "cheating");
		messagePickerLevelComplete = ResourceMgr.createPicker(messageBundle, "level.complete");
		messagePickerGameOver = ResourceMgr.createPicker(messageBundle, "game.over");

		MODEL3D_PAC = new PacModel3D("model3D/pacman.obj");
		MODEL3D_GHOST = new Model3D("model3D/ghost.obj");
		MODEL3D_PELLET = new Model3D("model3D/12206_Fruit_v1_L3.obj");

		loadFloorTexture("Chrome", "chrome");
		loadFloorTexture("Grass", "grass");
		loadFloorTexture("Hexagon", "hexagon");
		loadFloorTexture("Knobs & Bumps", "knobs");
		loadFloorTexture("Pavement", "pavement");
		loadFloorTexture("Plastic", "plastic");
		loadFloorTexture("Wood", "wood");

		iconPacManGame = ResourceMgr.image("icons/pacman.png");
		iconMsPacManGame = ResourceMgr.image("icons/mspacman.png");

		var duration = System.nanoTime() - start;
		LOG.info("Loading application resources done (%.2f seconds).", duration / 1_000_000_000f);
	}

	private static void loadFloorTexture(String key, String textureName) {
		var material = new PhongMaterial();
		textures.put(key, material);
		material.setBumpMap(ResourceMgr.image("graphics/textures/%s-bump.jpg".formatted(textureName)));
		material.setDiffuseMap(ResourceMgr.image("graphics/textures/%s-diffuse.jpg".formatted(textureName)));
		material.diffuseColorProperty().bind(Env.d3_floorColorPy);
		material.specularColorProperty()
				.bind(Bindings.createObjectBinding(Env.d3_floorColorPy.get()::brighter, Env.d3_floorColorPy));
	}

	public static PhongMaterial texture(String key) {
		return textures.get(key);
	}

	public static String[] textureKeys() {
		return textures.keySet().toArray(String[]::new);
	}

	public static String randomTextureKey() {
		var keys = AppResources.textureKeys();
		return textureKeys()[U.randomInt(0, keys.length)];
	}

	public static Image appIcon(GameVariant variant) {
		return switch (variant) {
		case MS_PACMAN -> iconMsPacManGame;
		case PACMAN -> iconPacManGame;
		default -> throw new IllegalArgumentException("Unknown game variant '%s'".formatted(variant));
		};
	}

	/**
	 * Builds a resource key from the given key pattern and arguments and reads the corresponding message from the
	 * messages resource bundle.
	 * 
	 * @param keyPattern message key pattern
	 * @param args       arguments merged into key pattern
	 * @return message text for composed key or string indicating missing text
	 */
	public static String message(String keyPattern, Object... args) {
		try {
			var pattern = messageBundle.getString(keyPattern);
			return MessageFormat.format(pattern, args);
		} catch (Exception x) {
			LOG.error("No text resource found for key '%s'", keyPattern);
			return "missing{%s}".formatted(keyPattern);
		}
	}

	public static String pickCheatingMessage() {
		return messagePickerCheating.next();
	}

	public static String pickGameOverMessage() {
		return messagePickerGameOver.next();
	}

	public static String pickLevelCompleteMessage(int levelNumber) {
		return "%s%n%n%s".formatted(messagePickerLevelComplete.next(), message("level_complete", levelNumber));
	}

	public static AudioClip voiceMessage(String voiceMessageKey) {
		var url = ResourceMgr.urlFromRelPath(voiceMessageKey);
		if (url != null) {
			return new AudioClip(url.toExternalForm());
		}
		throw new IllegalArgumentException("Unknown voice message key '%s'".formatted(voiceMessageKey));
	}

	public static String randomReadyText() {
		return READY_TEXTS[U.randomInt(0, READY_TEXTS.length)];
	}
}