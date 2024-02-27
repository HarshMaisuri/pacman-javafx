package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.NavigationPoint;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.NavigationPoint.np;

/**
 * The worlds from the original Arcade games.
 *
 * @author Armin Reichert
 */
public interface ArcadeWorld {

    byte TILES_X = 28;
    byte TILES_Y = 36;

    byte[][] PACMAN_MAP = {
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1},
        {1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1},
        {1, 4, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 4, 1},
        {1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1},
        {1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1},
        {1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1},
        {1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1},
        {1, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 1},
        {1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1},
        {0, 0, 0, 0, 0, 1, 3, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 3, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 3, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 3, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 3, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 3, 1, 0, 0, 0, 0, 0},
        {1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1},
        {2, 2, 2, 2, 2, 2, 3, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 3, 2, 2, 2, 2, 2, 2},
        {1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1},
        {0, 0, 0, 0, 0, 1, 3, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 3, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 3, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 3, 1, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 1, 3, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 3, 1, 0, 0, 0, 0, 0},
        {1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1},
        {1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1},
        {1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1},
        {1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1},
        {1, 4, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 0, 0, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 4, 1},
        {1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1},
        {1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1},
        {1, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 1},
        {1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1},
        {1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1},
        {1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
    };

    List<Vector2i> PACMAN_RED_ZONE = List.of(
        v2i(12, 14), v2i(15, 14),
        v2i(12, 26), v2i(15, 26));

    byte[][][] MS_PACMAN_MAPS = {
        {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 1},
            {1, 4, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 4, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1},
            {1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1},
            {0, 0, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 0, 0},
            {1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1},
            {2, 2, 2, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 2, 2, 2},
            {1, 1, 1, 3, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 3, 1, 1, 1},
            {0, 0, 1, 3, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 3, 1, 0, 0},
            {0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 1, 0, 0},
            {0, 0, 1, 3, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 3, 1, 0, 0},
            {0, 0, 1, 3, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 3, 1, 0, 0},
            {0, 0, 1, 3, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 3, 1, 0, 0},
            {0, 0, 1, 3, 1, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 1, 3, 1, 0, 0},
            {1, 1, 1, 3, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 3, 1, 1, 1},
            {2, 2, 2, 3, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 3, 2, 2, 2},
            {1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1},
            {0, 0, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 0, 0},
            {0, 0, 1, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 1, 1, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 1, 0, 0},
            {0, 0, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 0, 0},
            {1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1},
            {1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 4, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 4, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        },

        {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {2, 2, 2, 2, 2, 2, 0, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 0, 2, 2, 2, 2, 2, 2},
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 1, 1, 1, 1, 1},
            {1, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 1},
            {1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1},
            {1, 3, 1, 1, 3, 3, 3, 3, 3, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 3, 3, 3, 3, 3, 1, 1, 3, 1},
            {1, 3, 1, 1, 3, 1, 1, 1, 1, 0, 1, 1, 3, 3, 3, 3, 1, 1, 0, 1, 1, 1, 1, 3, 1, 1, 3, 1},
            {1, 3, 1, 1, 3, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 3, 1, 1, 3, 1},
            {1, 3, 3, 3, 3, 3, 3, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 3, 3, 3, 3, 3, 3, 1},
            {1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1},
            {1, 3, 3, 3, 3, 3, 3, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 3, 3, 3, 3, 3, 3, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 3, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 3, 3, 1, 1, 3, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 3, 1, 1, 3, 3, 3, 1},
            {1, 1, 1, 3, 1, 1, 3, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 3, 1, 1, 3, 1, 1, 1},
            {0, 0, 1, 3, 1, 1, 3, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 3, 1, 1, 3, 1, 0, 0},
            {0, 0, 1, 3, 1, 1, 3, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 3, 1, 1, 3, 1, 0, 0},
            {0, 0, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 0, 0},
            {0, 0, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 0, 0},
            {1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1},
            {2, 2, 2, 3, 3, 3, 3, 1, 1, 3, 3, 3, 0, 0, 0, 0, 3, 3, 3, 1, 1, 3, 3, 3, 3, 2, 2, 2},
            {1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1},
            {1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1},
            {1, 4, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 4, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        },

        {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1},
            {1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1},
            {1, 4, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 4, 1},
            {1, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 1},
            {1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1},
            {1, 3, 3, 3, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 3, 3, 3, 1},
            {1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1},
            {1, 1, 1, 1, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 1, 1, 1, 1},
            {2, 3, 3, 3, 3, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 3, 3, 3, 3, 2},
            {1, 3, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 3, 1},
            {1, 3, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 1, 1, 1, 3, 1},
            {1, 3, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 3, 1},
            {1, 3, 1, 1, 0, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0, 1, 1, 3, 1},
            {1, 3, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 3, 1},
            {1, 3, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 3, 1},
            {1, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 1},
            {1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1},
            {1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1},
            {1, 4, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 0, 0, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 4, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        },

        {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1},
            {1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1},
            {1, 4, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 4, 1},
            {1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1},
            {1, 3, 1, 1, 3, 3, 3, 3, 3, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 3, 3, 3, 3, 3, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 1},
            {1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1},
            {0, 0, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 0, 0},
            {0, 0, 1, 3, 3, 3, 3, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 3, 3, 3, 3, 1, 0, 0},
            {1, 1, 1, 0, 1, 1, 3, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 3, 1, 1, 0, 1, 1, 1},
            {2, 2, 2, 0, 1, 1, 3, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 3, 1, 1, 0, 2, 2, 2},
            {1, 1, 1, 1, 1, 1, 3, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 3, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 1},
            {2, 2, 2, 0, 1, 1, 3, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 3, 1, 1, 0, 2, 2, 2},
            {1, 1, 1, 0, 1, 1, 3, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 3, 1, 1, 0, 1, 1, 1},
            {0, 0, 1, 3, 3, 3, 3, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 3, 3, 3, 3, 1, 0, 0},
            {0, 0, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 0, 0},
            {0, 0, 1, 3, 1, 1, 3, 3, 3, 3, 0, 0, 0, 1, 1, 0, 0, 0, 3, 3, 3, 3, 1, 1, 3, 1, 0, 0},
            {0, 0, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 0, 0},
            {1, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 0, 1, 1, 0, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 1},
            {1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 0, 0, 0, 0, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 3, 1},
            {1, 3, 1, 1, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 1, 1, 3, 1},
            {1, 4, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 4, 1},
            {1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 3, 1},
            {1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        }
    };

    Vector2i HOUSE_POSITION = v2i(10, 15);
    Vector2i HOUSE_SIZE = v2i(8, 5);
    Vector2f HOUSE_SEAT_LEFT = halfTileRightOf(11, 17);
    Vector2f HOUSE_SEAT_MIDDLE = halfTileRightOf(13, 17);
    Vector2f HOUSE_SEAT_RIGHT = halfTileRightOf(15, 17);
    Door HOUSE_DOOR = new Door(v2i(13, 15), v2i(14, 15));

    Vector2i SCATTER_TARGET_LEFT_UPPER_CORNER = v2i(2, 0);
    Vector2i SCATTER_TARGET_RIGHT_UPPER_CORNER = v2i(25, 0);
    Vector2i SCATTER_TARGET_LEFT_LOWER_CORNER = v2i(0, 34);
    Vector2i SCATTER_TARGET_RIGHT_LOWER_CORNER = v2i(27, 34);


    NavigationPoint[] PACMAN_DEMO_LEVEL_ROUTE = {
        np(12, 26), np(9, 26), np(12, 32), np(15, 32), np(24, 29), np(21, 23),
        np(18, 23), np(18, 20), np(18, 17), np(15, 14), np(12, 14), np(9, 17),
        np(6, 17), np(6, 11), np(6, 8), np(6, 4), np(1, 8), np(6, 8),
        np(9, 8), np(12, 8), np(6, 4), np(6, 8), np(6, 11), np(1, 8),
        np(6, 8), np(9, 8), np(12, 14), np(9, 17), np(6, 17), np(0, 17),
        np(21, 17), np(21, 23), np(21, 26), np(24, 29), /* avoid moving up: */ np(26, 29),
        np(15, 32), np(12, 32), np(3, 29), np(6, 23), np(9, 23), np(12, 26),
        np(15, 26), np(18, 23), np(21, 23), np(24, 29), /* avoid moving up: */ np(26, 29),
        np(15, 32), np(12, 32), np(3, 29), np(6, 23)
    };

    static World createPacManWorld() {
        return createArcadeWorld(PACMAN_MAP);
    }

    static World createMsPacManWorld(int number) {
        if (number >= 1 && number <= 4) {
            return createArcadeWorld(MS_PACMAN_MAPS[number - 1]);
        }
        throw new IllegalArgumentException("Ms. Pac-Man world number must be 1..4 but is " + number);
    }

    static World createArcadeWorld(byte[][] map) {
        var world = new World(map);
        if (world.numCols() != TILES_X || world.numRows() != TILES_Y) {
            throw new IllegalArgumentException(
                String.format("Arcade map must have %d columns and %d rows but has %d columns and %d rows",
                    TILES_X, TILES_Y, world.numCols(), world.numRows()));
        }
        world.setHouse(createArcadeHouse());
        return world;
    }

    static House createArcadeHouse() {
        var house = new House();
        house.setMinTile(HOUSE_POSITION);
        house.setSize(HOUSE_SIZE);
        house.setDoor(HOUSE_DOOR);
        house.addSeat("left", HOUSE_SEAT_LEFT);
        house.addSeat("middle", HOUSE_SEAT_MIDDLE);
        house.addSeat("right", HOUSE_SEAT_RIGHT);
        return house;
    }

    /**
     * In Ms. Pac-Man, there are 4 maps used by the 6 mazes. Up to level 13, the mazes are:
     * <ul>
     * <li>Maze #1: pink maze, white dots (level 1-2)
     * <li>Maze #2: light blue maze, yellow dots (level 3-5)
     * <li>Maze #3: orange maze, red dots (level 6-9)
     * <li>Maze #4: dark blue maze, white dots (level 10-13)
     * </ul>
     * From level 14 on, the maze alternates every 4th level between maze #5 and maze #6.
     * <ul>
     * <li>Maze #5: pink maze, cyan dots (same map as maze #3)
     * <li>Maze #6: orange maze, white dots (same map as maze #4)
     * </ul>
     * <p>
     */
    static int mazeNumberMsPacMan(int levelNumber) {
        checkLevelNumber(levelNumber);
        return switch (levelNumber) {
            case 1, 2 -> 1;
            case 3, 4, 5 -> 2;
            case 6, 7, 8, 9 -> 3;
            case 10, 11, 12, 13 -> 4;
            default -> (levelNumber - 14) % 8 < 4 ? 5 : 6;  // alternate between maze #5 and #6 every 4th level
        };
    }

    static int mapNumberMsPacMan(int levelNumber) {
        checkLevelNumber(levelNumber);
        // from level 14, alternate between map #3 and #4 every 4th level
        return levelNumber <= 13 ? mazeNumberMsPacMan(levelNumber) : mazeNumberMsPacMan(levelNumber) - 2;
    }
}
