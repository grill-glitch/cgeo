package cgeo.geocaching.unifiedmap.tileproviders;

/**
 * Formats a tile path pattern with the given tile coordinates.
 * <br>
 * Supported placeholders:
 * <ul>
 * <li>{@code {Z}} - zoom level</li>
 * <li>{@code {X}} - tile x</li>
 * <li>{@code {Y}} - tile y</li>
 * <li>{@code {-Y}} - inverted tile y ({@code 2^Z - 1 - Y}), as used by Tencent Maps</li>
 * </ul>
 */
final class TilePathFormatter {

    private TilePathFormatter() {
        // utility class
    }

    public static String format(final String pattern, final long tileX, final long tileY, final int zoomLevel) {
        return pattern
                .replace("{Z}", String.valueOf(zoomLevel))
                .replace("{X}", String.valueOf(tileX))
                .replace("{-Y}", String.valueOf((1 << zoomLevel) - tileY - 1))
                .replace("{Y}", String.valueOf(tileY));
    }
}
