/*
 * MIT License
 * 
 * Copyright (c) 2021-2023 Armin Reichert
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
module de.amr.games.pacman.ui.fx {

	requires transitive javafx.controls;
	requires transitive javafx.media;
	requires javafx.graphics;
	requires org.apache.logging.log4j;
	requires transitive de.amr.games.pacman;
	requires jimObjModelImporterJFX;

	exports de.amr.games.pacman.ui.fx.app;
	exports de.amr.games.pacman.ui.fx.dashboard;
	exports de.amr.games.pacman.ui.fx.input;
	exports de.amr.games.pacman.ui.fx.scene;
	exports de.amr.games.pacman.ui.fx.shell;
	exports de.amr.games.pacman.ui.fx.sound.common;
	exports de.amr.games.pacman.ui.fx.sound.mspacman;
	exports de.amr.games.pacman.ui.fx.sound.pacman;
	exports de.amr.games.pacman.ui.fx.util;
	exports de.amr.games.pacman.ui.fx._2d.rendering.common;
	exports de.amr.games.pacman.ui.fx._2d.rendering.mspacman;
	exports de.amr.games.pacman.ui.fx._2d.rendering.pacman;
	exports de.amr.games.pacman.ui.fx._2d.scene.common;
	exports de.amr.games.pacman.ui.fx._3d;
	exports de.amr.games.pacman.ui.fx._3d.animation;
	exports de.amr.games.pacman.ui.fx._3d.entity;
	exports de.amr.games.pacman.ui.fx._3d.scene;
	exports de.amr.games.pacman.ui.fx._3d.scene.cams;
}