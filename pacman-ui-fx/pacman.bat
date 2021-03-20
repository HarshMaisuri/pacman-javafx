:: Usage: pacman.bat or pacman.bat -mspacman

::@ECHO OFF
@SETLOCAL

SET "JAVA_HOME=C:\Program Files\Java\jdk-15.0.2"
SET "JFX_LIB=C:\Program Files\Java\javafx-sdk-15.0.1\lib"
SET "MAVEN_REPOSITORY=C:\Users\armin\.m2\repository"
SET "PACMAN_LIB=..\..\pacman-basic\pacman-core\target\pacman-core-1.0.jar"
SET "PACMAN_UI_FX=.\target\pacman-ui-fx-1.0.jar"
SET APP=de.amr.games.pacman.ui.fx.app.PacManGameAppFX

SET "CLASSPATH=%PACMAN_LIB%;%PACMAN_UI_FX%"^
 ";%JFXLIB%\javafx.base.jar"^
 ";%JFXLIB%\javafx.controls.jar"^
 ";%JFXLIB%\javafx.graphics.jar"^
 ";%MAVEN_REPOSITORY%\org\openjfx\javafx-controls\15\javafx-controls-15.jar"^
 ";%MAVEN_REPOSITORY%\org\openjfx\javafx-controls\15\javafx-controls-15-win.jar"^
 ";%MAVEN_REPOSITORY%\org\openjfx\javafx-graphics\15\javafx-graphics-15.jar"^
 ";%MAVEN_REPOSITORY%\org\openjfx\javafx-graphics\15\javafx-graphics-15-win.jar"^
 ";%MAVEN_REPOSITORY%\org\openjfx\javafx-base\15\javafx-base-15.jar"^
 ";%MAVEN_REPOSITORY%\org\openjfx\javafx-base\15\javafx-base-15-win.jar"^
 ";%MAVEN_REPOSITORY%\com\interactivemesh\jimModelImporterJFX\0.0.1-SNAPSHOT\jimModelImporterJFX-0.0.1-SNAPSHOT.jar"

"%JAVA_HOME%"\bin\javaw.exe --module-path "%JFX_LIB%" --add-modules javafx.controls -cp %CLASSPATH% %APP% %*

@ENDLOCAL