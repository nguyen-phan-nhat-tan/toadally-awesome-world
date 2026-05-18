package simulation;

import java.util.Objects;

/**
 * Immutable snapshot of the entire world suitable for rendering.
 */
public final class WorldSnapshot {
    public final int width;
    public final int height;
    public final HexSnapshot[] cells; // length = width*height

    public WorldSnapshot(int width, int height, HexSnapshot[] cells) {
        this.width = width;
        this.height = height;
        this.cells = Objects.requireNonNull(cells, "cells");
    }

    public static WorldSnapshot from(World w) {
        int wdt = w.getWidth();
        int hgt = w.getHeight();
        HexSnapshot[] arr = new HexSnapshot[wdt * hgt];
        for (int y = 0; y < hgt; y++) {
            for (int x = 0; x < wdt; x++) {
                arr[y * wdt + x] = HexSnapshot.fromHexState(w.getHex(x, y));
            }
        }
        return new WorldSnapshot(wdt, hgt, arr);
    }

    public HexSnapshot getHex(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return HexSnapshot.fromHexState(null);
        return cells[y * width + x];
    }
}
