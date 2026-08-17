package cgeo.geocaching.unifiedmap;

import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.location.Viewport;

import java.util.function.Function;

/**
 * Converts between WGS-84 coordinates (as used by cgeo and geocaching) and the
 * coordinate system of a map tile source.
 * <br>
 * Chinese map providers (Amap, Tencent) use GCJ-02 coordinates, which are offset
 * from WGS-84 by a non-linear, government-mandated transformation. For those maps,
 * {@link Gcj02CoordinateConverter} must be used; all other providers are 1:1 and use {@link #IDENTITY}.
 */
public interface MapCoordinateConverter {

    MapCoordinateConverter IDENTITY = new MapCoordinateConverter() {
        @Override
        public Geopoint fromMap(final Geopoint gp) {
            return gp;
        }

        @Override
        public Geopoint toMap(final Geopoint gp) {
            return gp;
        }
    };

    /** converts a cgeo (WGS-84) coordinate to the coordinate system of the map */
    Geopoint toMap(Geopoint gp);

    /** converts a map coordinate back to a cgeo (WGS-84) coordinate */
    Geopoint fromMap(Geopoint gp);

    default Viewport toMap(final Viewport viewport) {
        return convertViewport(viewport, this::toMap);
    }

    default Viewport fromMap(final Viewport viewport) {
        return convertViewport(viewport, this::fromMap);
    }

    static Viewport convertViewport(final Viewport viewport, final Function<Geopoint, Geopoint> converter) {
        final Geopoint bl = converter.apply(viewport.bottomLeft);
        final Geopoint tl = converter.apply(new Geopoint(viewport.getLatitudeMax(), viewport.getLongitudeMin()));
        final Geopoint br = converter.apply(new Geopoint(viewport.getLatitudeMin(), viewport.getLongitudeMax()));
        final Geopoint tr = converter.apply(viewport.topRight);
        return new Viewport(
                Math.min(Math.min(bl.getLatitude(), tl.getLatitude()), Math.min(br.getLatitude(), tr.getLatitude())),
                Math.min(Math.min(bl.getLongitude(), tl.getLongitude()), Math.min(br.getLongitude(), tr.getLongitude())),
                Math.max(Math.max(bl.getLatitude(), tl.getLatitude()), Math.max(br.getLatitude(), tr.getLatitude())),
                Math.max(Math.max(bl.getLongitude(), tl.getLongitude()), Math.max(br.getLongitude(), tr.getLongitude())));
    }
}
