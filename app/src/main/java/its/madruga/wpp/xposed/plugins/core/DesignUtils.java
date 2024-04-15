package its.madruga.wpp.xposed.plugins.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DesignUtils {

    private static int primarySurfaceColor;
    private static int primaryTextColor;

    @SuppressLint("ResourceType")
    public static void initColors(Context context) {
        var resourceId = Utils.getID("Theme.Base", "style");
        int[] ids  = { android.R.attr.textColorPrimary, android.R.attr.windowBackground};
        TypedArray values = context.getTheme().obtainStyledAttributes(resourceId, ids);
        int primaryTextId = values.getResourceId(0, 0);
        int surfaceId = values.getResourceId(1, 0);
        setPrimaryTextColor(context.getColor(primaryTextId));
        setPrimarySurfaceColor(context.getColor(surfaceId));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getDrawable(int id) {
        return Utils.getApplication().getDrawable(id);
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getDrawableByName(String name) {
        var id = Utils.getID(name, "drawable");
        return Utils.getApplication().getDrawable(id);
    }

    public static Drawable coloredDrawable(Drawable drawable0, int color, BlendMode mode) {
        drawable0.setColorFilter(new BlendModeColorFilter(color, mode));
        return drawable0;
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable alphaDrawable(Drawable drawable, int primaryTextColor, BlendMode mode, int i) {
        Drawable coloredDrawable = DesignUtils.coloredDrawable(drawable, primaryTextColor, mode);
        coloredDrawable.setAlpha(i);
        return coloredDrawable;
    }

    public static Drawable createDrawable(String type) {
        if (type.equals("rc_dialog_bg")) {
            var shapeDrawable = new ShapeDrawable();
            shapeDrawable.getPaint().setColor(Color.BLACK);
            return shapeDrawable;
        }else if (type.equals("selector_bg")) {
            var border = Utils.dipToPixels(18.0f);
            ShapeDrawable selectorBg = new ShapeDrawable(new RoundRectShape(new float[]{border, border, border, border, border, border, border, border}, null, null));
            selectorBg.getPaint().setColor(Color.BLACK);
            return selectorBg;
        }else if (type.equals("rc_dotline_dialog")) {
            var border = Utils.dipToPixels(16.0f);
            ShapeDrawable shapeDrawable = new ShapeDrawable(new RoundRectShape(new float[]{border, border, border, border, border, border, border, border}, null, null));
            shapeDrawable.getPaint().setColor(0x28FFFFFF);
            return shapeDrawable;
        }

        return null;
    }

    // Colors

    public static int getPrimarySurfaceColor(Context context) {
        if (primarySurfaceColor == 0) {
            initColors(context);
        }
        return primarySurfaceColor;
    }

    public static void setPrimarySurfaceColor(int color) {
        primarySurfaceColor = color;
    }

    public static int getPrimaryTextColor(Context context) {
        if (primaryTextColor == 0) {
            initColors(context);
        }
        return primaryTextColor;
    }

    public static void setPrimaryTextColor(int color) {
        primaryTextColor = color;
    }


}
