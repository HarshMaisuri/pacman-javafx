@echo off
rem Builds the Pac-Man Java FX project
rem Git repositories "pacman-basic" and "pacman-javafx" must be locally available
rem Creates excutable jar file and deployable zip file
call mvn clean
pushd ..\..\pacman-basic\pacman-core
call mvn clean install
popd
call mvn clean install
call mvn -f deploy.xml install