package cgeo.geocaching.unifiedmap.tileproviders;

import cgeo.geocaching.storage.LocalStorage;
import cgeo.geocaching.unifiedmap.LayerHelper;
import cgeo.geocaching.unifiedmap.mapsforgevtm.MapsforgeVtmFragment;

import android.net.Uri;

import androidx.core.util.Pair;

import java.io.File;
import java.util.Collections;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.oscim.layers.tile.bitmap.BitmapTileLayer;
import org.oscim.map.Map;
import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.UrlTileSource;
import org.oscim.tiling.source.bitmap.BitmapTileSource;

/**
 * VTM (Mapsforge VTM) online tile provider.
 * Supports multiple mirror hosts and the {@code {-Y}} placeholder (inverted Y axis,
 * used by Tencent Maps).
 */
class AbstractMapsforgeVTMOnlineTileProvider extends AbstractMapsforgeVTMTileProvider {

    private String tilePath;
    private final String[] tileUrls;

    AbstractMapsforgeVTMOnlineTileProvider(final String name, final Uri uri, final String tilePath, final int zoomMin, final int zoomMax, final Pair<String, Boolean> mapAttribution) {
        this(name, uri, tilePath, zoomMin, zoomMax, mapAttribution, new String[]{uri.toString()});
    }

    AbstractMapsforgeVTMOnlineTileProvider(final String name, final Uri uri, final String tilePath, final int zoomMin, final int zoomMax, final Pair<String, Boolean> mapAttribution, final String[] tileUrls) {
        super(name, uri, zoomMin, zoomMax, mapAttribution);
        this.tilePath = tilePath;
        this.tileUrls = tileUrls;
    }

    protected void setTilePath(final String tilePath) {
        this.tilePath = tilePath;
    }

    @Override
    public void addTileLayer(final MapsforgeVtmFragment fragment, final Map map) {
        fragment.addLayer(LayerHelper.ZINDEX_BASEMAP, getBitmapTileLayer(map));
    }

    public BitmapTileLayer getBitmapTileLayer(final Map map) {
        final OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        final Cache cache = new Cache(new File(LocalStorage.getExternalPrivateCgeoDirectory(), "tiles"), 20 * 1024 * 1024);
        httpBuilder.cache(cache);
        final BitmapTileSource tileSource = BitmapTileSource.builder()
                .url(tileUrls)
                .tilePath(tilePath)
                .zoomMax(zoomMax)
                .zoomMin(zoomMin)
                .build();
        if (tilePath.contains("{-Y}")) {
            // Tencent Maps uses an inverted Y axis (y = 2^z - 1 - y)
            tileSource.setUrlFormatter((source, tile) -> TilePathFormatter.format(tilePath, tile.tileX, tile.tileY, tile.zoomLevel));
        }
        tileSource.setHttpEngine(new OkHttpEngine.OkHttpFactory(httpBuilder));
        tileSource.setHttpRequestHeaders(Collections.singletonMap("User-Agent", "cgeo-android"));
        return new BitmapTileLayer(map, tileSource);
    }

}
