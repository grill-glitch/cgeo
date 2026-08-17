package cgeo.geocaching.unifiedmap.tileproviders;

import cgeo.geocaching.R;
import cgeo.geocaching.utils.LocalizationUtils;

import android.net.Uri;

import androidx.core.util.Pair;

/**
 * Esri World Imagery (satellite) tile source for the VTM (mapsforge-vtm) renderer.
 * Public service, no API key required.
 */
class EsriSatelliteVTMSource extends AbstractMapsforgeVTMOnlineTileProvider {

    EsriSatelliteVTMSource() {
        super(LocalizationUtils.getPlainString(R.string.map_source_esri_satellite),
                Uri.parse("https://server.arcgisonline.com"),
                "/ArcGIS/rest/services/World_Imagery/MapServer/tile/{Z}/{Y}/{X}",
                2, 19,
                new Pair<>(LocalizationUtils.getPlainString(R.string.map_attribution_esri), false));
    }
}
