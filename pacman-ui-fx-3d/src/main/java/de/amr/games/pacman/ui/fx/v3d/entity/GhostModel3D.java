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

package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.ui.fx.v3d.app.Game3d;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;

/**
 * @author Armin Reichert
 *
 */
public class GhostModel3D extends Model3D {

	public static final String MESH_ID_GHOST_DRESS = "Sphere.004_Sphere.034_light_blue_ghost";
	public static final String MESH_ID_GHOST_EYEBALLS = "Sphere.009_Sphere.036_white";
	public static final String MESH_ID_GHOST_PUPILS = "Sphere.010_Sphere.039_grey_wall";

	public GhostModel3D() {
		super(Game3d.resources.loader.urlFromRelPath("model3D/ghost.obj"));
	}
}