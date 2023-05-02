/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.ui.fx.v3d.app;

import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.v3d.scene.Perspective;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

/**
 * Global stuff.
 * 
 * @author Armin Reichert
 */
public class Env {
//@formatter:off
	public static final ObjectProperty<Color>       mainSceneBgColorPy = new SimpleObjectProperty<>(Color.web("0x334bd3"));
	public static final BooleanProperty             showDebugInfoPy = new SimpleBooleanProperty(false);

	public static final BooleanProperty             simulationPausedPy = new SimpleBooleanProperty(false);
	public static final IntegerProperty             simulationStepsPy = new SimpleIntegerProperty(1);
	public static final IntegerProperty             simulationSpeedPy = new SimpleIntegerProperty(60);
	public static final BooleanProperty             simulationTimeMeasuredPy = new SimpleBooleanProperty(false);
	
	public static final BooleanProperty             wokePussyMode = new SimpleBooleanProperty(false); 

	public static final BooleanProperty             dashboardVisiblePy = new SimpleBooleanProperty(false);

	public static final BooleanProperty             pipVisiblePy = new SimpleBooleanProperty(false);
	public static final DoubleProperty              pipOpacityPy = new SimpleDoubleProperty(0.66);
	public static final DoubleProperty              pipSceneHeightPy = new SimpleDoubleProperty(World.TILES_Y * Globals.TS);

	public static final BooleanProperty             d3_axesVisiblePy = new SimpleBooleanProperty(false);
	public static final ObjectProperty<DrawMode>    d3_drawModePy = new SimpleObjectProperty<>(DrawMode.FILL);
	public static final BooleanProperty             d3_enabledPy = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Color>       d3_floorColorPy = new SimpleObjectProperty<>(Color.grayRgb(0x60));
	public static final StringProperty              d3_floorTexturePy = new SimpleStringProperty("Knobs & Bumps");
	public static final BooleanProperty             d3_floorTextureRandomPy = new SimpleBooleanProperty(false);
	public static final ObjectProperty<Color>       d3_lightColorPy = new SimpleObjectProperty<>(Color.GHOSTWHITE);
	public static final DoubleProperty              d3_mazeWallHeightPy = new SimpleDoubleProperty(1.75);
	public static final DoubleProperty              d3_mazeWallThicknessPy = new SimpleDoubleProperty(1.25);
	public static final BooleanProperty             d3_pacLightedPy = new SimpleBooleanProperty(true);
	public static final ObjectProperty<Perspective> d3_perspectivePy = new SimpleObjectProperty<>(Perspective.NEAR_PLAYER);
	public static final BooleanProperty             d3_energizerExplodesPy = new SimpleBooleanProperty(true);
	// experimental, not used yet 
	public static final BooleanProperty             d3_foodOscillationEnabledPy = new SimpleBooleanProperty(false);
//@formatter:on
}