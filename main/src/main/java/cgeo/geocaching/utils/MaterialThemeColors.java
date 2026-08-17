package cgeo.geocaching.utils;

import cgeo.geocaching.R;

import android.content.Context;
import android.content.res.TypedArray;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.color.MaterialColors;

/**
 * Bridge between the legacy c:geo static palette (R.color.colorBackground,
 * R.color.colorText, R.color.colorAccent, ...) and the Material 3 semantic
 * color roles.
 * <p>
 * On Android 12+ with DynamicColors applied, the theme attributes
 * (colorPrimary, colorSurface, colorOnSurface, ...) carry wallpaper-derived
 * values; on older devices (or when dynamic color is unavailable) they fall
 * back to the static Material3 baseline palette defined by the theme.
 * <p>
 * Use these helpers instead of {@code ContextCompat.getColor(ctx, R.color.xxx)}
 * whenever the color should follow theming. The legacy R.color values remain as
 * a last-resort fallback.
 */
public final class MaterialThemeColors {

    private MaterialThemeColors() {
        // utility class
    }

    /** Background of the whole window / screen. */
    @ColorInt
    public static int background(@NonNull final Context context) {
        return getColor(context, com.google.android.material.R.attr.colorSurface, R.color.colorBackground);
    }

    /** Elevated / dialog / card background. */
    @ColorInt
    public static int backgroundElevated(@NonNull final Context context) {
        return getColor(context, com.google.android.material.R.attr.colorSurfaceContainer, R.color.colorBackgroundDialog);
    }

    /** Selected / pressed background tint. */
    @ColorInt
    public static int backgroundSelected(@NonNull final Context context) {
        return getColor(context, com.google.android.material.R.attr.colorSurfaceContainerHighest, R.color.colorBackgroundSelected);
    }

    /** Primary text color. */
    @ColorInt
    public static int textPrimary(@NonNull final Context context) {
        return getColor(context, com.google.android.material.R.attr.colorOnSurface, R.color.colorText);
    }

    /** Secondary / hint text color. */
    @ColorInt
    public static int textSecondary(@NonNull final Context context) {
        return getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, R.color.colorTextHint);
    }

    /** Accent / brand color (buttons, links, active states). */
    @ColorInt
    public static int primary(@NonNull final Context context) {
        return getColor(context, androidx.appcompat.R.attr.colorPrimary, R.color.colorAccent);
    }

    /** Accent container (filled chips, selected backgrounds). */
    @ColorInt
    public static int primaryContainer(@NonNull final Context context) {
        return getColor(context, com.google.android.material.R.attr.colorPrimaryContainer, R.color.colorAccent);
    }

    /** Text on accent-colored surfaces. */
    @ColorInt
    public static int onPrimary(@NonNull final Context context) {
        return getColor(context, com.google.android.material.R.attr.colorOnPrimary, R.color.text_icon);
    }

    /** Bottom navigation / tab bar background. */
    @ColorInt
    public static int navigationBar(@NonNull final Context context) {
        return getColor(context, com.google.android.material.R.attr.colorSurfaceContainer, R.color.colorBackgroundTabBar);
    }

    /** Separators / dividers / outlines. */
    @ColorInt
    public static int separator(@NonNull final Context context) {
        return getColor(context, com.google.android.material.R.attr.colorOutlineVariant, R.color.colorSeparator);
    }

    /** App bar / toolbar background (follows the primary color when colored). */
    @ColorInt
    public static int actionBar(@NonNull final Context context) {
        return getColor(context, com.google.android.material.R.attr.colorSurface, R.color.colorBackgroundActionBar);
    }

    @ColorInt
    public static int getColor(@NonNull final Context context, @AttrRes final int attr, @ColorInt final int fallback) {
        final Integer resolved = resolveAttribute(context, attr);
        return resolved != null ? resolved : fallback;
    }

    @Nullable
    public static Integer resolveAttribute(@NonNull final Context context, @AttrRes final int attr) {
        final TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        try {
            return ta.hasValue(0) ? ta.getColor(0, 0) : null;
        } finally {
            ta.recycle();
        }
    }

    /** Convenience for views: MaterialColors.getColor(view, attr, fallback). */
    @ColorInt
    public static int getColorFromView(@NonNull final android.view.View view, @AttrRes final int attr, @ColorInt final int fallback) {
        return MaterialColors.getColor(view, attr, fallback);
    }
}