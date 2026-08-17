package cgeo.geocaching.unifiedmap;

import cgeo.geocaching.location.Geopoint;

/**
 * Standard GCJ-02 (国测局坐标) transformation, as used by Chinese map providers
 * (Amap, Tencent) to offset WGS-84 coordinates.
 * <br>
 * The transformation applies only inside China; coordinates outside the
 * bounding box are returned unchanged. {@link #fromMap} inverts the offset
 * via fixed-point iteration.
 */
public final class Gcj02CoordinateConverter implements MapCoordinateConverter {

    public static final Gcj02CoordinateConverter INSTANCE = new Gcj02CoordinateConverter();

    private static final double EARTH_SEMIMAJOR_AXIS = 6378245.0d; // a (semi-major axis)
    private static final double ECCENTRICITY_SQUARED = 0.006693421622965943d; // ee = e^2
    private static final int INVERSE_ITERATIONS = 4;

    private Gcj02CoordinateConverter() {
        // singleton
    }

    @Override
    public Geopoint toMap(final Geopoint gp) {
        final double latitude = gp.getLatitude();
        final double longitude = gp.getLongitude();
        if (isOutsideChina(latitude, longitude)) {
            return gp;
        }
        final double dLat = transformLatitude(longitude - 105.0, latitude - 35.0);
        final double dLon = transformLongitude(longitude - 105.0, latitude - 35.0);
        final double radLat = Math.toRadians(latitude);
        final double magic = 1.0 - ECCENTRICITY_SQUARED * Math.sin(radLat) * Math.sin(radLat);
        final double sqrtMagic = Math.sqrt(magic);
        final double newLat = latitude + Math.toDegrees(dLat * sqrtMagic * magic / (6335552.717000426d));
        final double newLon = longitude + Math.toDegrees(dLon * sqrtMagic / (Math.cos(radLat) * EARTH_SEMIMAJOR_AXIS));
        return new Geopoint(newLat, newLon);
    }

    @Override
    public Geopoint fromMap(final Geopoint gp) {
        if (isOutsideChina(gp.getLatitude(), gp.getLongitude())) {
            return gp;
        }
        Geopoint candidate = gp;
        for (int i = 0; i < INVERSE_ITERATIONS; i++) {
            final Geopoint mapped = toMap(candidate);
            candidate = new Geopoint(candidate.getLatitude() + gp.getLatitude() - mapped.getLatitude(),
                    candidate.getLongitude() + gp.getLongitude() - mapped.getLongitude());
        }
        return candidate;
    }

    private static boolean isOutsideChina(final double latitude, final double longitude) {
        return longitude < 72.004 || longitude > 137.8347 || latitude < 0.8293 || latitude > 55.8271;
    }

    private static double transformLatitude(final double x, final double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLongitude(final double x, final double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }
}
