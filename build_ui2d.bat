@echo off
pushd pacman-ui-fx
call mvn clean install
popd
pushd pacman-ui-fx-jlink
call mvn clean install
popd