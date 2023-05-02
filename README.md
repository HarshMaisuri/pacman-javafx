# A JavaFX UI (2D + 3D) for Pac-Man and Ms. Pac-Man

## About this project

A JavaFX user interface for my UI-agnostic [Pac-Man / Ms. Pac-Man game](https://github.com/armin-reichert/pacman-basic) implementations. 

Both games can be played in 2D and 3D, you can switch between 2D and 3D by pressing key combination <kbd>Alt+3</kbd>.

## How to run the released version

### With Java runtime 18+ installed on your computer (Windows):
- Download the executable jar file `pacman-ui-fx-1.0-shaded.jar` from the [release folder](https://github.com/armin-reichert/pacman-javafx/releases). Start the application by double-clicking this file in the file explorer. Any help in creating executables for Mac-OS and Linux is appreciated.

### Without locally installed Java runtime (Windows):
- Download the zip file `pacman-javafx-tentackle-1.0-jlink.zip` from the release folder. Extract it somewhere and execute file `run.cmd` in folder `bin`.  

To build the executable by yourself on non-Windows systems, run the steps in the build script manually or create a shell script or whatever.

## How to build locally

Clone also repository [pacman-basic](https://github.com/armin-reichert/pacman-basic). Then

```
cd \path\to\git\pacman-javafx 
build.bat
```

This build script 
- runs a Maven build of the [pacman-core](https://github.com/armin-reichert/pacman-basic/tree/main/pacman-core) project (game logic and model),
- runs a Maven build of the [pacman-ui-fx](pacman-ui-fx) project (2D-only user interface),
- runs a Maven build of the [pacman-ui-fx]-3d(pacman-ui-fx-3d) project (2D+3D user interface),
- creates executable jar file `pacman-ui-fx-1.0-shaded.jar` (folder `pacman-javafx\pacman-ui-fx\target`),
- creates executable jar file `pacman-ui-fx-3d-1.0-shaded.jar` (folder `pacman-javafx\pacman-ui-fx-3d\target`),
- creates zip file `pacman-ui-fx-jlink.1.0-jlink.zip` (folder `pacman-javafx\pacman-ui-fx\target`).
- creates zip file `pacman-ui-fx-3d-jlink-1.0-jlink.zip` (folder `pacman-javafx\pacman-ui-fx-3d\target`).

## How to run the locally built version

If Java runtime 18+ is installed, you can run the executables jar files by double-clicking them. Alternatively, extract the zip file and execute the `run.cmd`file as described above. Or just execute the `run.bat`file or `run2d.bat` scripts.

## How to use (full version)

Starting the game and switching game variant:
- <kbd>V</kbd> Switch between Pac-Man and Ms. Pac-Man (only possible on intro screen)
- <kbd>5</kbd> Add credit ("insert coin")
- <kbd>1</kbd> Start game

Pac-Man steering:
- Pac-Man is steered using the cursor keys. When the dashboard is open, these keys are taken away by the JavaFX widgets. 
In that case, you can steer Pac-Man using key combination <kbd>CTRL</kbd>+cursor key.

General shortcuts:
- <kbd>F11</kbd> Enter fullscreen mode
- <kbd>Esc</kbd> Exit fullscreen mode
- <kbd>F1</kbd> or <kbd>Alt+B</kbd> Toggle dashboard
- <kbd>F2</kbd> Toggle picture-in-picture view
- <kbd>Alt+Z</kbd> Play all intermission scenes
- <kbd>Alt+3</kbd> Toggle using 2D/3D play scene

Play screen shortcuts:
- <kbd>Alt+LEFT</kbd> Select previous camera perspective
- <kbd>Alt+RIGHT</kbd> Select next camera perspective
- <kbd>Q</kbd>Quit play scene and show intro screen

Cheats:
  - <kbd>Alt+A</kbd> Toggle autopilot mode
  - <kbd>Alt+E</kbd> Eat all pills except the energizers
  - <kbd>Alt+I</kbd> Toggle immunity of player against ghost attacks
  - <kbd>Alt+L</kbd> Add 3 player lives
  - <kbd>Alt+N</kbd> Enter next game level
  - <kbd>Alt+X</kbd> Kill all ghosts outside of the ghosthouse 

## How it looks

![Play Scene](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/pacman-maze.png)

![Info Panel](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/left-info-panel.png)

![Info Panel](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/right-info-panel.png)

![Info Panel](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/3D-info-panel.png)

YouTube:

[![YouTube](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/thumbnail.jpg)](https://www.youtube.com/watch?v=_3iQ-PKXX6Y)
