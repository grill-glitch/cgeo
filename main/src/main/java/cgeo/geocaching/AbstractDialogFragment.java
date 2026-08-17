package cgeo.geocaching;

import cgeo.geocaching.activity.AbstractActivity;
import cgeo.geocaching.activity.AbstractNavigationBarMapActivity;
import cgeo.geocaching.activity.ActivityMixin;
import cgeo.geocaching.activity.INavigationSource;
import cgeo.geocaching.enumerations.CacheType;
import cgeo.geocaching.enumerations.LoadFlags;
import cgeo.geocaching.location.Geopoint;
import cgeo.geocaching.location.Units;
import cgeo.geocaching.log.LoggingUI;
import cgeo.geocaching.models.Geocache;
import cgeo.geocaching.sensors.GeoData;
import cgeo.geocaching.sensors.GeoDirHandler;
import cgeo.geocaching.settings.Settings;
import cgeo.geocaching.storage.DataStore;
import cgeo.geocaching.ui.CacheDetailCardBuilder;
import cgeo.geocaching.ui.CacheDetailsCreator;
import cgeo.geocaching.ui.CoordinatesFormatSwitcher;
import cgeo.geocaching.ui.ViewUtils;
import cgeo.geocaching.utils.CacheTypeColorScheme;
import cgeo.geocaching.utils.LocalizationUtils;
import cgeo.geocaching.utils.Log;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public abstract class AbstractDialogFragment extends Fragment implements CacheMenuHandler.ActivityInterface, INavigationSource {
    public static final int RESULT_CODE_SET_TARGET = Activity.RESULT_FIRST_USER;
    public static final int REQUEST_CODE_TARGET_INFO = 1;
    protected static final String GEOCODE_ARG = "GEOCODE";
    protected static final String WAYPOINT_ARG = "WAYPOINT";
    private static final String STATE_COORDINATE_FORMAT_POSITION = "coordinateFormatPosition";
    private final CompositeDisposable resumeDisposables = new CompositeDisposable();
    protected String geocode;
    protected CacheDetailsCreator details;
    protected Geocache cache;
    private TextView cacheDistance = null;
    private int coordinateFormatPosition = 0;
    private final GeoDirHandler geoUpdate = new GeoDirHandler() {

        @Override
        public void updateGeoData(final GeoData geo) {
            try {
                if (cacheDistance != null && cache != null && cache.getCoords() != null) {
                    cacheDistance.setText(Units.getDistanceFromKilometers(geo.getCoords().distanceTo(cache.getCoords())));
                    cacheDistance.bringToFront();
                }
                onUpdateGeoData(geo);
            } catch (final RuntimeException e) {
                Log.w("Failed to update location", e);
            }
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            coordinateFormatPosition = savedInstanceState.getInt(STATE_COORDINATE_FORMAT_POSITION, 0);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_COORDINATE_FORMAT_POSITION, coordinateFormatPosition);
    }

    @Override
    public void onStart() {
        super.onStart();
        geocode = getArguments().getString(GEOCODE_ARG);
    }

    protected void init() {
        cache = DataStore.loadCache(geocode, LoadFlags.LOAD_CACHE_OR_DB);

        if (cache == null) {
            ((AbstractActivity) requireActivity()).showToast(LocalizationUtils.getString(R.string.err_detail_cache_find));

            ((AbstractNavigationBarMapActivity) requireActivity()).sheetRemoveFragment();
            return;
        }

        geocode = cache.getGeocode();
    }

    @Override
    public void onResume() {
        super.onResume();
        // resume location access
        resumeDisposables.add(geoUpdate.start(GeoDirHandler.UPDATE_GEODATA));
        init();
    }


    @Override
    public void onPause() {
        resumeDisposables.clear();
        super.onPause();
    }

    protected final void addCacheDetails(final boolean showGeocode) {
        assert cache != null;

        // cache type
        final CacheDetailsCreator.NameValueLine typeLine = details.add(R.string.cache_type, cache.getType().getL10n());
        final View sizeRow = cache.showSize() ? details.addSize(cache) : null;

        if (showGeocode) {
            details.add(R.string.cache_geocode, cache.getShortGeocode());
        }
        details.addCacheState(cache);

        final TextView cacheDistanceView = details.addDistance(cache, cacheDistance);
        cacheDistance = cacheDistanceView;

        final View diffRow = details.addDifficulty(cache);
        final View terrainRow = details.addTerrain(cache);
        details.addEventDate(cache);

        // regroup the property rows into cards: top row [type, size, distance], second [difficulty, terrain]
        final LinearLayout detailsList = (LinearLayout) details.getParentView();
        if (typeLine != null && detailsList != null) {
            final View typeRow = typeLine.layout;
            final View distanceRow = cacheDistanceView != null ? (View) cacheDistanceView.getParent().getParent() : null;
            for (final View row : new View[]{typeRow, sizeRow, distanceRow, diffRow, terrainRow}) {
                if (row != null) {
                    detailsList.removeView(row);
                }
            }
            final LayoutInflater inflater = requireActivity().getLayoutInflater();
            final int seed = requireActivity().getResources().getColor(getCacheTypeColor(cache));
            final CacheTypeColorScheme scheme = CacheTypeColorScheme.fromSeed(requireActivity(), seed);
            final LinearLayout row1 = CacheDetailCardBuilder.createPropertyCardRow(requireActivity());
            row1.addView(CacheDetailCardBuilder.createPropertyCard(inflater, detailsList, CacheDetailCardBuilder.labelOf(typeRow), CacheDetailCardBuilder.valueOf(typeRow), CacheDetailCardBuilder.starsOf(typeRow), scheme));
            if (sizeRow != null) {
                row1.addView(CacheDetailCardBuilder.createPropertyCard(inflater, detailsList, CacheDetailCardBuilder.labelOf(sizeRow), CacheDetailCardBuilder.valueOf(sizeRow), CacheDetailCardBuilder.starsOf(sizeRow), scheme));
            }
            if (distanceRow != null) {
                row1.addView(CacheDetailCardBuilder.createPropertyCard(inflater, detailsList, CacheDetailCardBuilder.labelOf(distanceRow), CacheDetailCardBuilder.valueOf(distanceRow), CacheDetailCardBuilder.starsOf(distanceRow), scheme));
            }
            final LinearLayout row2 = CacheDetailCardBuilder.createPropertyCardRow(requireActivity());
            if (diffRow != null) {
                row2.addView(CacheDetailCardBuilder.createPropertyCard(inflater, detailsList, CacheDetailCardBuilder.labelOf(diffRow), CacheDetailCardBuilder.valueOf(diffRow), CacheDetailCardBuilder.starsOf(diffRow), scheme));
            }
            if (terrainRow != null) {
                row2.addView(CacheDetailCardBuilder.createPropertyCard(inflater, detailsList, CacheDetailCardBuilder.labelOf(terrainRow), CacheDetailCardBuilder.valueOf(terrainRow), CacheDetailCardBuilder.starsOf(terrainRow), scheme));
            }
            detailsList.addView(row1, 0);
            detailsList.addView(row2, 1);
        }

        // rating
        if (cache.getRating() > 0) {
            details.addRating(cache);
        }

        // favorite count
        final int favCount = cache.getFavoritePoints();
        if (favCount >= 0) {
            final int findsCount = cache.getFindsCount();
            if (findsCount > 0) {
                details.add(R.string.cache_favorite, LocalizationUtils.getPlainString(R.string.favorite_count_percent, favCount, (float) (favCount * 100) / findsCount));
            } else if (!cache.isEventCache()) {
                details.add(R.string.cache_favorite, LocalizationUtils.getPlainString(R.string.favorite_count, favCount));
            }
        }

        details.addBetterCacher(cache);
        final CoordinatesFormatSwitcher coordinateSwitcher = details.addCoordinates(cache.getCoords(), coordinateFormatPosition);
        if (coordinateSwitcher != null) {
            coordinateSwitcher.setOnPositionChangedListener(position -> coordinateFormatPosition = position);
        }

        // Latest logs
        details.addLatestLogs(cache);

        // more details
        final View view = getView();
        assert view != null;
        final Button buttonMore = view.findViewById(R.id.more_details);

        buttonMore.setOnClickListener(arg0 -> {
            CacheDetailActivity.startActivity(getActivity(), geocode);
            ((AbstractNavigationBarMapActivity) requireActivity()).sheetRemoveFragment();
        });

        /* Only working combination as it seems */
        registerForContextMenu(buttonMore);
    }

    /** The cache-type seed color resource (grey for archived/disabled caches), like the detail page uses. */
    private static int getCacheTypeColor(@NonNull final Geocache cache) {
        return (cache.isArchived() || cache.isDisabled()) ? R.color.cacheType_disabled : cache.getType().typeColor;
    }

    public final void showToast(final String text) {
        ActivityMixin.showToast(getActivity(), text);
    }

    /**
     * @param geo location
     */
    protected void onUpdateGeoData(final GeoData geo) {
        // do nothing by default
    }

    /**
     * Set the current popup coordinates as new navigation target on map
     */
    private void setAsTarget() {
        final TargetUpdateReceiver activity = (TargetUpdateReceiver) requireActivity();
        activity.onReceiveTargetUpdate(getTargetInfo());
        ((AbstractNavigationBarMapActivity) requireActivity()).sheetRemoveFragment();
    }

    public static void onCreatePopupOptionsMenu(final Toolbar toolbar, final INavigationSource navigationSource, final Geocache geocache) {
        final Menu menu = toolbar.getMenu();
        menu.clear();
        toolbar.inflateMenu(R.menu.cache_options);
        CacheMenuHandler.onPrepareOptionsMenu(menu, geocache, true);
        CacheMenuHandler.initDefaultNavigationMenuItem(menu, navigationSource);
        ViewUtils.extendMenuActionBarDisplayItemCount(toolbar.getContext(), menu);
        menu.findItem(R.id.menu_target).setVisible(true);
        LoggingUI.onPrepareOptionsMenu(menu, geocache);
    }

    public boolean onPopupOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == R.id.menu_target) {
            setAsTarget();
            return true;
        }

        if (CacheMenuHandler.onMenuItemSelected(item, this, cache, this::init, true)) {
            return true;
        }

        return LoggingUI.onMenuItemSelected(item, getActivity(), cache, dialog -> init());
    }

    protected void setToolbarBackgroundColor(@NonNull final Toolbar toolbar, @NonNull final View swipView, @Nullable final CacheType cacheType, final boolean isEnabled) {
        final Context context = toolbar.getContext();
        if (!Settings.useColoredActionBar(context)) {
            return;
        }

        final boolean isLightSkin = Settings.isLightSkin(context);
        final int actionbarColor = CacheType.getActionBarColor(context, cacheType, isEnabled, isLightSkin);
        final Drawable swipeBackground = swipView.getBackground();
        if (null != swipeBackground) {
            swipeBackground.mutate().setTint(actionbarColor);
        }
        toolbar.setBackgroundColor(actionbarColor);
    }

    protected abstract TargetInfo getTargetInfo();

    @Override
    public void navigateTo() {
        startDefaultNavigation();
    }

    @Override
    public void cachesAround() {
        final TargetInfo targetInfo = getTargetInfo();
        if (targetInfo == null || targetInfo.coords == null) {
            showToast(LocalizationUtils.getString(R.string.err_location_unknown));
            return;
        }
        CacheListActivity.startActivityCoordinates((AbstractActivity) getActivity(), targetInfo.coords, cache != null ? cache.getName() : null);
    }

    public interface TargetUpdateReceiver {
        void onReceiveTargetUpdate(TargetInfo targetInfo);
    }

    public static class TargetInfo implements Parcelable {

        public static final Parcelable.Creator<TargetInfo> CREATOR = new Parcelable.Creator<TargetInfo>() {
            @Override
            public TargetInfo createFromParcel(final Parcel in) {
                return new TargetInfo(in);
            }

            @Override
            public TargetInfo[] newArray(final int size) {
                return new TargetInfo[size];
            }
        };
        public final Geopoint coords;
        public final String geocode;

        public TargetInfo(final Geopoint coords, final String geocode) {
            this.coords = coords;
            this.geocode = geocode;
        }

        public TargetInfo(final Parcel in) {
            this.coords = in.readParcelable(Geopoint.class.getClassLoader());
            this.geocode = in.readString();
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeParcelable(coords, PARCELABLE_WRITE_RETURN_VALUE);
            dest.writeString(geocode);
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }
}
