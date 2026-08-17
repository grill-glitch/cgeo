package cgeo.geocaching.ui;

import cgeo.geocaching.R;
import cgeo.geocaching.utils.CacheTypeColorScheme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Reusable builder for the "property card" layout used on the cache detail page and
 * on the map cache preview popup: one rounded card per property with the value in
 * large bold text on top and the label below, colored with the cache-type seed palette.
 */
public final class CacheDetailCardBuilder {

    private CacheDetailCardBuilder() {
        // utility class
    }

    /** Create a horizontal row container for property cards. */
    @NonNull
    public static LinearLayout createPropertyCardRow(@NonNull final Context context) {
        final LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        // NOTE: setPadding takes PIXELS - convert dp so the vertical gap matches the
        // horizontal margin between the cards (3dp + 3dp each side)
        final int pad = Math.round(3 * context.getResources().getDisplayMetrics().density);
        row.setPadding(0, pad, 0, pad);
        return row;
    }

    /**
     * Build a single property card: large bold value on top, label below, optional stars.
     *
     * @param scheme the cache-type seed color scheme, or null to fall back to the theme attrs
     */
    @NonNull
    public static View createPropertyCard(@NonNull final LayoutInflater inflater, @NonNull final ViewGroup parent,
                                          @NonNull final CharSequence label, @NonNull final CharSequence value,
                                          @Nullable final RatingBar stars, @Nullable final CacheTypeColorScheme scheme) {
        final View card = inflater.inflate(R.layout.cache_detail_property_card, parent, false);
        ((TextView) card.findViewById(R.id.property_value)).setText(value);
        ((TextView) card.findViewById(R.id.property_label)).setText(label);
        // card background + stars follow the cache-type seed palette (keep the rounded corners)
        if (scheme != null) {
            final float radius = card.getResources().getDimension(R.dimen.card_corner_radius);
            final GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(radius);
            bg.setColor(scheme.surfaceContainer);
            card.setBackground(bg);
            final RatingBar cardStars = card.findViewById(R.id.property_stars);
            cardStars.setProgressTintList(ColorStateList.valueOf(scheme.primaryContainer));
            cardStars.setProgressBackgroundTintList(ColorStateList.valueOf(scheme.onSurface));
        }
        if (stars != null && stars.getVisibility() == View.VISIBLE) {
            final RatingBar cardStars = card.findViewById(R.id.property_stars);
            cardStars.setNumStars(stars.getNumStars());
            cardStars.setRating(stars.getRating());
            cardStars.setVisibility(View.VISIBLE);
        }
        return card;
    }

    @NonNull
    public static CharSequence labelOf(@Nullable final View row) {
        return row == null ? "" : ((TextView) row.findViewById(R.id.name)).getText();
    }

    @NonNull
    public static CharSequence valueOf(@Nullable final View row) {
        return row == null ? "" : ((TextView) row.findViewById(R.id.value)).getText();
    }

    @Nullable
    public static RatingBar starsOf(@Nullable final View row) {
        return row == null ? null : row.findViewById(R.id.stars);
    }
}
