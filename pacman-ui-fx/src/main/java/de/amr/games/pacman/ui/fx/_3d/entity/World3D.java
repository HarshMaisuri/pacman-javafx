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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;

/**
 * 3D-model for the world in a game level. Creates walls/doors using information from the floor plan.
 * 
 * @author Armin Reichert
 */
public class World3D extends Group {

	private static class WallData {
		byte type;
		int x;
		int y;
		int numBricksX;
		int numBricksY;
		float brickSize;
		PhongMaterial baseMaterial;
		PhongMaterial topMaterial;
	}

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static final int MAZE_RESOLUTION = 4; // 1, 2, 4, 8 are allowed values
	private static final double FLOOR_THICKNESS = 0.1;

	public final DoubleProperty wallHeightPy = new SimpleDoubleProperty(this, "wallHeight", 2.0);
	public final DoubleProperty wallThicknessPy = new SimpleDoubleProperty(this, "wallThickness", 1.0);
	public final ObjectProperty<Image> floorTexturePy = new SimpleObjectProperty<>(this, "floorTexture");
	public final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK);
	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

	private final World world;
	private final Maze3DColors mazeColors;
	private final Group wallsGroup = new Group();
	private final Group doorsGroup = new Group();
	private final PointLight houseLighting;
	private Box floor;

	public World3D(World world, Maze3DColors mazeColors) {
		this.world = world;
		this.mazeColors = mazeColors;

		houseLighting = new PointLight();
		houseLighting.setColor(Color.GHOSTWHITE);
		houseLighting.setMaxRange(4 * TS);
		houseLighting.setTranslateX(world.ghostHouse().entryTile().x() * TS + TS);
		houseLighting.setTranslateY(world.ghostHouse().entryTile().y() * TS + 3 * TS);
		houseLighting.setTranslateZ(-TS);

		floorColorPy.addListener(py -> updateFloorMaterial());
		floorTexturePy.addListener(py -> updateFloorMaterial());
		buildFloor();
		buildMaze(MAZE_RESOLUTION);
		getChildren().addAll(floor, wallsGroup, doorsGroup, houseLighting);
	}

	public PointLight houseLighting() {
		return houseLighting;
	}

	private void buildFloor() {
		double width = (double) world.numCols() * TS - 1;
		double height = (double) world.numRows() * TS - 1;
		double depth = FLOOR_THICKNESS;
		floor = new Box(width, height, depth);
		floor.setTranslateX(0.5 * width);
		floor.setTranslateY(0.5 * height);
		floor.setTranslateZ(0.5 * depth);
		floor.drawModeProperty().bind(drawModePy);
		updateFloorMaterial();
	}

	private void updateFloorMaterial() {
		var material = coloredMaterial(floorColorPy.get());
		material.setDiffuseMap(floorTexturePy.get());
		floor.setMaterial(material);
	}

	private PhongMaterial coloredMaterial(Color diffuseColor) {
		var material = new PhongMaterial(diffuseColor);
		material.setSpecularColor(diffuseColor.brighter());
		return material;
	}

	private WallData createWallData(int resolution) {
		var wallData = new WallData();
		wallData.brickSize = (float) TS / resolution;
		wallData.baseMaterial = coloredMaterial(mazeColors.wallBaseColor());
		wallData.topMaterial = coloredMaterial(mazeColors.wallTopColor());
		return wallData;
	}

	private void buildMaze(int resolution) {
		var floorPlan = new FloorPlan(world, resolution);
		wallsGroup.getChildren().clear();
		addCorners(floorPlan, createWallData(resolution));
		addHorizontalWalls(floorPlan, createWallData(resolution));
		addVerticalWalls(floorPlan, createWallData(resolution));
//		transformMaze();
		var doors = world.ghostHouse().doorTiles().map(tile -> createDoor(tile, mazeColors.doorColor())).toList();
		doorsGroup.getChildren().setAll(doors);
		LOGGER.info("3D maze rebuilt (resolution=%d, wall height=%.2f)", floorPlan.getResolution(), wallHeightPy.get());
	}

//	private void transformMaze() {
//		if (world instanceof MapBasedWorld mapWorld) {
//			wallsGroup.getChildren().forEach(wall -> {
//				WallData data = (WallData) wall.getUserData();
//				LOGGER.info("Wall data type is %d", data.type);
//				var tile = tileFromFloorPlanCoord(data.x, data.y);
//				switch (data.type) {
//				case FloorPlan.HWALL -> {
//					var tileAbove = tile.plus(Direction.UP.vector());
//					if (mapWorld.isWall(tileAbove)) {
//						wall.getTransforms().add(new Translate(0, -2));
//					} else {
////						wall.getTransforms().add(new Translate(0, 2));
//					}
//				}
//				case FloorPlan.VWALL -> {
//
//				}
//				case FloorPlan.CORNER -> {
//
//				}
//				}
//			});
//		}
//	}
//
//	private Vector2i tileFromFloorPlanCoord(int fx, int fy) {
//		return new Vector2i(fx / MAZE_RESOLUTION, fy / MAZE_RESOLUTION);
//	}

	public Stream<Door3D> doors() {
		return doorsGroup.getChildren().stream().map(Door3D.class::cast);
	}

	private Door3D createDoor(Vector2i tile, Color color) {
		var door = new Door3D(tile, color);
		door.doorHeightPy.bind(wallHeightPy);
		door.drawModeProperty().bind(drawModePy);
		return door;
	}

	private void addHorizontalWalls(FloorPlan floorPlan, WallData wallData) {
		wallData.type = FloorPlan.HWALL;
		wallData.numBricksY = 1;
		for (int y = 0; y < floorPlan.sizeY(); ++y) {
			wallData.x = -1;
			wallData.y = y;
			wallData.numBricksX = 0;
			for (int x = 0; x < floorPlan.sizeX(); ++x) {
				if (floorPlan.get(x, y) == FloorPlan.HWALL) {
					if (wallData.numBricksX == 0) {
						wallData.x = x;
					}
					wallData.numBricksX++;
				} else if (wallData.numBricksX > 0) {
					addCompositeWall(wallData);
					wallData.numBricksX = 0;
				}
			}
			if (wallData.numBricksX > 0 && y == floorPlan.sizeY() - 1) {
				addCompositeWall(wallData);
			}
		}
	}

	private void addVerticalWalls(FloorPlan floorPlan, WallData wallData) {
		wallData.type = FloorPlan.VWALL;
		wallData.numBricksX = 1;
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			wallData.x = x;
			wallData.y = -1;
			wallData.numBricksY = 0;
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.VWALL) {
					if (wallData.numBricksY == 0) {
						wallData.y = y;
					}
					wallData.numBricksY++;
				} else if (wallData.numBricksY > 0) {
					addCompositeWall(wallData);
					wallData.numBricksY = 0;
				}
			}
			if (wallData.numBricksY > 0 && x == floorPlan.sizeX() - 1) {
				addCompositeWall(wallData);
			}
		}
	}

	private void addCorners(FloorPlan floorPlan, WallData wallData) {
		wallData.type = FloorPlan.CORNER;
		wallData.numBricksX = 1;
		wallData.numBricksY = 1;
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.CORNER) {
					wallData.x = x;
					wallData.y = y;
					addCompositeWall(wallData);
				}
			}
		}
	}

	private void addCompositeWall(WallData wallData) {
		var base = switch (wallData.type) {
		case FloorPlan.HWALL -> horizontalWall(wallData);
		case FloorPlan.VWALL -> verticalWall(wallData);
		case FloorPlan.CORNER -> corner();
		default -> throw new IllegalStateException();
		};
		base.depthProperty().bind(wallHeightPy);
		base.translateZProperty().bind(wallHeightPy.multiply(-0.5));
		base.setMaterial(wallData.baseMaterial);

		var top = switch (wallData.type) {
		case FloorPlan.HWALL -> horizontalWall(wallData);
		case FloorPlan.VWALL -> verticalWall(wallData);
		case FloorPlan.CORNER -> corner();
		default -> throw new IllegalStateException();
		};
		double topHeight = 0.1;
		top.setDepth(topHeight);
		top.translateZProperty().bind(base.translateZProperty().subtract(wallHeightPy.add(topHeight + 0.1).multiply(0.5)));
		top.setMaterial(wallData.topMaterial);

		var wall = new Group(base, top);
		wall.setTranslateX((wallData.x + 0.5 * wallData.numBricksX) * wallData.brickSize);
		wall.setTranslateY((wallData.y + 0.5 * wallData.numBricksY) * wallData.brickSize);
		wall.setUserData(wallData);

		wallsGroup.getChildren().add(wall);
	}

	private Box horizontalWall(WallData wallData) {
		Box wall = new Box();
		// without ...+1 there are gaps. why?
		wall.setWidth((wallData.numBricksX + 1) * wallData.brickSize);
		wall.heightProperty().bind(wallThicknessPy);
		wall.drawModeProperty().bind(drawModePy);
		return wall;
	}

	private Box verticalWall(WallData wallData) {
		Box wall = new Box();
		wall.widthProperty().bind(wallThicknessPy);
		// without ...+1 there are gaps. why?
		wall.setHeight((wallData.numBricksY + 1) * wallData.brickSize);
		wall.drawModeProperty().bind(drawModePy);
		return wall;
	}

	private Box corner() {
		Box corner = new Box();
		corner.widthProperty().bind(wallThicknessPy);
		corner.heightProperty().bind(wallThicknessPy);
		corner.drawModeProperty().bind(drawModePy);
		return corner;
	}
}