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

import de.robv.android.xposed.XposedBridge;

public class DesignUtils {

    private static int primaryTextId;
    private static int primarySurfaceId;

    @SuppressLint("ResourceType")
    public static void initColors(Context context) {
        try {
            var resourceId = Utils.getID("Theme.Base", "style");
            int[] ids = {android.R.attr.textColorPrimary, android.R.attr.windowBackground};
            TypedArray values = context.getTheme().obtainStyledAttributes(resourceId, ids);
            primaryTextId = values.getResourceId(0, 0);
            primarySurfaceId = values.getResourceId(1, 0);
        }catch (Exception e) {
            XposedBridge.log("Error while getting colors: " + e);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable getDrawable(int id) {
        return Utils.getApplication().getDrawable(id);
    }


    public static Drawable getDrawableByName(String name) {
        var id = Utils.getID(name, "drawable");
        return DesignUtils.getDrawable(id);
    }

    public static Drawable coloredDrawable(Drawable drawable, int color, BlendMode mode) {
        drawable.setColorFilter(new BlendModeColorFilter(color, mode));
        return drawable;
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    public static Drawable alphaDrawable(Drawable drawable, int primaryTextColor, BlendMode mode, int i) {
        Drawable coloredDrawable = DesignUtils.coloredDrawable(drawable, primaryTextColor, mode);
        coloredDrawable.setAlpha(i);
        return coloredDrawable;
    }

    public static Drawable createDrawable(String type) {
        switch (type) {
            case "rc_dialog_bg" -> {
                var shapeDrawable = new ShapeDrawable();
                shapeDrawable.getPaint().setColor(Color.BLACK);
                return shapeDrawable;
            }
            case "selector_bg" -> {
                var border = Utils.dipToPixels(18.0f);
                ShapeDrawable selectorBg = new ShapeDrawable(new RoundRectShape(new float[]{border, border, border, border, border, border, border, border}, null, null));
                selectorBg.getPaint().setColor(Color.BLACK);
                return selectorBg;
            }
            case "rc_dotline_dialog" -> {
                var border = Utils.dipToPixels(16.0f);
                ShapeDrawable shapeDrawable = new ShapeDrawable(new RoundRectShape(new float[]{border, border, border, border, border, border, border, border}, null, null));
                shapeDrawable.getPaint().setColor(0x28FFFFFF);
                return shapeDrawable;
            }
        }
        return null;
    }

    // Colors
    public static int getPrimaryTextColor(Context context) {
        if (primaryTextId == 0) {
            initColors(context);
        }
        return context.getColor(primaryTextId);
    }

    public static int getPrimarySurfaceColor(Context context) {
        if (primarySurfaceId == 0) {
            initColors(context);
        }
        return context.getColor(primarySurfaceId);
    }

}
