package cgeo.geocaching.utils;

import cgeo.geocaching.enumerations.CacheType;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

/**
 * Generates a Material 3 color scheme seeded from a cache type color, then applies
 * it to the cache detail UI (action bar, tab layout, pull-to-refresh, ...).
 * <p>
 * This is the "cache color as primary, dynamically derived" behavior: instead of
 * painting single elements with the raw cache type color, we derive the full M3
 * tonal palette (primary / onPrimary / primaryContainer / surfaceContainer / ...)
 * from it, mirroring what Material You does with the wallpaper seed.
 * <p>
 * The seed color keeps its identity: the cache type color IS the primary
 * (traditional=green, multi=orange, mystery=dark blue, earth=light blue, event=red).
 */
public final class CacheTypeColorScheme {

    @ColorInt
    public final int primary;
    @ColorInt
    public final int onPrimary;
    @ColorInt
    public final int primaryContainer;
    @ColorInt
    public final int onPrimaryContainer;
    @ColorInt
    public final int surfaceContainer;
    @ColorInt
    public final int onSurface;

    private CacheTypeColorScheme(@ColorInt final int primary, @ColorInt final int onPrimary,
                                 @ColorInt final int primaryContainer, @ColorInt final int onPrimaryContainer,
                                 @ColorInt final int surfaceContainer, @ColorInt final int onSurface) {
        this.primary = primary;
        this.onPrimary = onPrimary;
        this.primaryContainer = primaryContainer;
        this.onPrimaryContainer = onPrimaryContainer;
        this.surfaceContainer = surfaceContainer;
        this.onSurface = onSurface;
    }

    /**
     * Build a scheme from a seed color. The seed itself becomes the primary
     * (unchanged - the cache type color keeps its identity: traditional=green,
     * multi=orange, mystery=dark blue, earth=light blue, event=red, ...), and the
     * surrounding tones (container, surface) are derived from it via HSL.
     *
     * @param context any themed context (activity)
     * @param seed    the seed color (e.g. cache type color, or the grey disabled tone)
     */
    @NonNull
    public static CacheTypeColorScheme fromSeed(@NonNull final Context context, @ColorInt final int seed) {
        // keep the seed color as-is; do NOT harmonize with the theme primary, so the
        // cache type color keeps its identity
        final int primary = seed;
        final boolean dark = isDarkTheme(context);

        final float[] hsl = ColorUtils.getHslValues(primary);
        final float h = hsl[0];
        final float s = hsl[1];
        final float l = hsl[2];

        final int primaryContainer = hslToColor(h, s, clamp01(l + (dark ? 0.10f : 0.18f)));
        final int onPrimary = contrastOk(primary, Color.WHITE) ? Color.WHITE : Color.BLACK;
        final int onPrimaryContainer = contrastOk(primaryContainer, Color.BLACK) ? Color.BLACK : Color.WHITE;
        final int surfaceContainer = hslToColor(h, clamp01(s * 0.35f), dark ? 0.16f : 0.92f);
        final int onSurface = dark ? 0xFFE3E3E3 : 0xFF1A1A1A;

        return new CacheTypeColorScheme(primary, onPrimary, primaryContainer, onPrimaryContainer, surfaceContainer, onSurface);
    }

    /**
     * Convenience: scheme seeded from a cache type (see {@link #fromSeed}).
     * NOTE: CacheType.typeColor is a color RESOURCE id - resolve it first.
     */
    @NonNull
    public static CacheTypeColorScheme fromCacheType(@NonNull final Context context, @NonNull final CacheType cacheType) {
        return fromSeed(context, context.getResources().getColor(cacheType.typeColor));
    }

    /** Theme-default scheme (no cache type): read the M3 attributes. */
    @NonNull
    public static CacheTypeColorScheme fromTheme(@NonNull final Context context) {
        final int primary = attrColor(context, androidx.appcompat.R.attr.colorPrimary, 0xFFF5981D);
        final int onPrimary = attrColor(context, com.google.android.material.R.attr.colorOnPrimary, Color.WHITE);
        final int primaryContainer = attrColor(context, com.google.android.material.R.attr.colorPrimaryContainer, primary);
        final int onPrimaryContainer = attrColor(context, com.google.android.material.R.attr.colorOnPrimaryContainer, Color.BLACK);
        final int surfaceContainer = attrColor(context, com.google.android.material.R.attr.colorSurfaceContainer, 0xFF2A2A2A);
        final int onSurface = attrColor(context, com.google.android.material.R.attr.colorOnSurface, 0xFFE3E3E3);
        return new CacheTypeColorScheme(primary, onPrimary, primaryContainer, onPrimaryContainer, surfaceContainer, onSurface);
    }

    private static int attrColor(@NonNull final Context context, final int attr, @ColorInt final int fallback) {
        final TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(attr, tv, true) && tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return tv.data;
        }
        return fallback;
    }

    private static boolean isDarkTheme(@NonNull final Context context) {
        final int uiMode = context.getResources().getConfiguration().uiMode;
        return (uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    private static boolean contrastOk(@ColorInt final int a, @ColorInt final int b) {
        return ColorUtils.getContrastRatio(a, b) >= 3.0;
    }

    private static int hslToColor(final float h, final float s, final float l) {
        final float[] hsl = {h, clamp01(s), clamp01(l)};
        return ColorUtils.getColorFromHslValues(hsl);
    }

    private static float clamp01(final float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
