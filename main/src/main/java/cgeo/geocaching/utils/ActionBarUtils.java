package cgeo.geocaching.utils;

import cgeo.geocaching.R;
import cgeo.geocaching.activity.AbstractActionBarActivity;
import cgeo.geocaching.settings.Settings;

import android.app.Activity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.color.MaterialColors;
import org.apache.commons.lang3.StringUtils;

public class ActionBarUtils {

    private ActionBarUtils() {
        // utility class
    }

    public static void toggleActionBar(@NonNull final AbstractActionBarActivity activity) {
        if (!Settings.getMapActionbarAutohide()) {
            return;
        }
        final View actionBar = activity.getActionBarView();
        if (actionBar == null) {
            return;
        }
        final boolean isShown = activity.actionBarIsShowing();
        activity.showActionBar(!isShown);

        // adjust system bars appearance, depending on action bar color and visibility
        ActionBarUtils.setSystemBarAppearance(activity);
    }

    public static void setSystemBarAppearance(@NonNull final Activity activity) {
        final Window currentWindow = activity.getWindow();
        final WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(currentWindow, currentWindow.getDecorView());

        final boolean isLightSkin = Settings.isLightSkin(activity);
        windowInsetsController.setAppearanceLightStatusBars(false);
        windowInsetsController.setAppearanceLightNavigationBars(isLightSkin);
    }

    public static void setSubtitle(@NonNull final AbstractActionBarActivity activity, @NonNull final CharSequence subtitleText) {
        final ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar == null || StringUtils.isEmpty(subtitleText)) {
            return;
        }

        final SpannableString titleString = getSpannedTitle(subtitleText, MaterialColors.getColor(activity, com.google.android.material.R.attr.colorOnSurfaceVariant, 0xFF000000));
        supportActionBar.setSubtitle(titleString);
    }

    public static void setTitle(@NonNull final AbstractActionBarActivity activity, @NonNull final CharSequence titleText) {
        final ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar == null || StringUtils.isEmpty(titleText)) {
            return;
        }

        // use the themed (dynamic) on-surface color so the title follows Material You
        final int titleColor = MaterialColors.getColor(activity, androidx.appcompat.R.attr.colorControlNormal, 0xFF000000);
        final SpannableString titleString = getSpannedTitle(titleText, titleColor);
        supportActionBar.setTitle(titleString);
    }

    // @todo remove after switching map ActionBar to Toolbar
    // workaround for colored ActionBar titles/subtitles
    // Checking for an existing span of the given class
    private static SpannableString getSpannedTitle(final CharSequence spanText, final int color) {
        // // If a Spanned is already present, check whether a ForegroundColorSpan covers the entire text
        if (TextUtils.hasSpanCoveringWholeText(spanText, ForegroundColorSpan.class)) {
            return new SpannableString(spanText);
        }

        // Create new span with actionbar text color
        final SpannableString titleString = new SpannableString(spanText);
        titleString.setSpan(new ForegroundColorSpan(color), 0, titleString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return titleString;
    }
}
