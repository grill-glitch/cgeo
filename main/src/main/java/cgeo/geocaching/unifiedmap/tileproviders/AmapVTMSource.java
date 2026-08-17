package cgeo.geocaching.unifiedmap.tileproviders;

import cgeo.geocaching.R;
import cgeo.geocaching.unifiedmap.Gcj02CoordinateConverter;
import cgeo.geocaching.unifiedmap.MapCoordinateConverter;
import cgeo.geocaching.utils.LocalizationUtils;

import android.net.Uri;

import androidx.core.util.Pair;

/**
 * Amap (高德地图) tile source for the VTM (mapsforge-vtm) renderer.
 * Uses GCJ-02 coordinates, hence {@link Gcj02CoordinateConverter}.
 * No API key required (public tile service).
 */
class AmapVTMSource extends AbstractMapsforgeVTMOnlineTileProvider {

    AmapVTMSource() {
        super(LocalizationUtils.getPlainString(R.string.map_source_amap),
                Uri.parse("https://wprd01.is.autonavi.com"),
                "/appmaptile?lang=zh_cn&size=1&scale=1&style=10&x={X}&y={Y}&z={Z}",
                2, 20,
                new Pair<>(LocalizationUtils.getPlainString(R.string.map_attribution_amap), false),
                new String[]{"https://wprd01.is.autonavi.com", "https://wprd02.is.autonavi.com", "https://wprd03.is.autonavi.com", "https://wprd04.is.autonavi.com"});
    }

    @Override
    public MapCoordinateConverter getCoordinateConverter() {
        return Gcj02CoordinateConverter.INSTANCE;
    }
}
