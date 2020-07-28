package com.example.testscroll.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.DimenRes;

import com.example.testscroll.TestScrollApplication;

import java.lang.reflect.Field;

/**
 * @author foxcoder
 * @since 2020-07-27
 */
public class DisplayUtils {

    private static int sStatusBarHeight = -1;

    private DisplayUtils() {
    }

    /**
     * @return the width of screen, in pixel
     */
    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) TestScrollApplication.getApplication().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return -1;
        }
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        return point.x;
    }

    /**
     * @return the height of screen, in pixel
     */
    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) TestScrollApplication.getApplication().getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return -1;
        }
        Point point = new Point();
        wm.getDefaultDisplay().getRealSize(point);
        return point.y;
    }

    public static int getNavBarHeight(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        Point realSize = new Point();
        display.getSize(point);  // app绘制区域
        display.getRealSize(realSize);

        int statusBarHeight = getStatusBarHeightResource();

        int realNaviHeight;
        int naviBarHeight = getNaviHeightResource();
        int sum = point.y + statusBarHeight + naviBarHeight;
        int sum1 = point.y + naviBarHeight;
        int sum2 = point.y + statusBarHeight;

        if (realSize.y != sum && realSize.y != sum1 && realSize.y - sum2 > 0) {
            realNaviHeight = realSize.y - sum2;
        } else if (realSize.y == point.y) {
            realNaviHeight = 0;
        } else {
            realNaviHeight = naviBarHeight;
        }
        return realNaviHeight;
    }

    protected static int getNaviHeightResource() {
        Resources res = TestScrollApplication.getApplication().getResources();
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId != 0) {
            return res.getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    public static int getStatusBarHeightResource() {
        int result = 0;
        int resourceId = TestScrollApplication.getApplication().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = TestScrollApplication.getApplication().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * @return the density of screen
     */
    public static float getDensity() {
        return TestScrollApplication.getApplication().getResources().getDisplayMetrics().density;
    }

    public static float getFontDensity() {
        return TestScrollApplication.getApplication().getResources().getDisplayMetrics().scaledDensity;
    }

    /**
     * @return the screen density expressed as dots-per-inch
     */
    public static int getScreenDensityDpi() {
        return TestScrollApplication.getApplication().getResources().getDisplayMetrics().densityDpi;
    }

    /**
     * dp -> px
     */
    public static int dp2px(int dp) {
        return (int) (getDensity() * dp + 0.5);
    }

    /**
     * sp -> px
     */
    public static int sp2px(int sp) {
        return (int) (getFontDensity() * sp + 0.5f);
    }

    /**
     * px -> dp
     */
    public static int px2dp(int px) {
        return (int) (px / getDensity() + 0.5f);
    }

    /**
     * px -> sp
     */
    public static int px2sp(int px) {
        return (int) (px / getFontDensity() + 0.5f);
    }

    /**
     * Converts an unpacked complex data value holding a dimension to its final floating
     * point value. The two parameters <var>unit</var> and <var>value</var>
     * are as in {@link TypedValue#TYPE_DIMENSION}.
     *
     * @param value The value to apply the unit to.
     * @param unit  The unit to convert from.
     * @return The complex floating point value multiplied by the appropriate
     * metrics depending on its unit.
     */
    public static float applyDimension(final float value, final int unit) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        switch (unit) {
            case TypedValue.COMPLEX_UNIT_PX:
                return value;
            case TypedValue.COMPLEX_UNIT_DIP:
                return value * metrics.density;
            case TypedValue.COMPLEX_UNIT_SP:
                return value * metrics.scaledDensity;
            case TypedValue.COMPLEX_UNIT_PT:
                return value * metrics.xdpi * (1.0f / 72);
            case TypedValue.COMPLEX_UNIT_IN:
                return value * metrics.xdpi;
            case TypedValue.COMPLEX_UNIT_MM:
                return value * metrics.xdpi * (1.0f / 25.4f);
            default:
                return 0;
        }
    }

    /**
     * Get StatusBar Height.
     * Use 25dp if no status bar height found.
     */
    public static int getStatusBarHeight() {
        Resources resources = TestScrollApplication.getApplication().getResources();
        if (sStatusBarHeight == -1) {
            int sbar = 0;
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object obj = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = Integer.parseInt(field.get(obj).toString());
                sbar = resources.getDimensionPixelSize(x);
            } catch (Exception e1) {
                e1.printStackTrace();
            } finally {
                sStatusBarHeight = sbar;
            }

            try {
                //Use 25dp if no status bar height found
                if (sStatusBarHeight == 0) {
                    sStatusBarHeight = dp2px(25);
                }
            } catch (Exception ignored) {
            }
        }

        return sStatusBarHeight;
    }

    public static int getDimension(@DimenRes int id) {
        Resources res = TestScrollApplication.getApplication().getResources();
        return res.getDimensionPixelOffset(id);
    }

    public static boolean isTablet() {
        return TestScrollApplication.getApplication().getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    /**
     * @return the screen stable density without screen zoom
     */
    public static int getDeviceStableDensity() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            return DisplayMetrics.DENSITY_DEVICE_STABLE;
        } else {
            // old version, screen zoom not support
            return getScreenDensityDpi();
        }
    }
}
