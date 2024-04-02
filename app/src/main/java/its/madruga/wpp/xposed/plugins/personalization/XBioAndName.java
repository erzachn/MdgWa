package its.madruga.wpp.xposed.plugins.personalization;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.wpp.xposed.models.XHookBase;

public class XBioAndName extends XHookBase {
    public XBioAndName(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() {
        var showName = prefs.getBoolean("shownamehome", false);
        var showBio = prefs.getBoolean("showbiohome", false);
        var methodHook = new MethodHook(showName, showBio);
        if (!showName && !showBio) return;
        XposedHelpers.findAndHookMethod("com.whatsapp.HomeActivity", loader, "onCreate", Bundle.class, methodHook);
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Show Name and Bio";
    }

    public static class MethodHook extends XC_MethodHook {
        private final boolean showName;
        private final boolean showBio;

        public MethodHook(boolean showName, boolean showBio) {
            this.showName = showName;
            this.showBio = showBio;
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            var homeActivity = (Activity) param.thisObject;
            var toolbar = homeActivity.findViewById(homeActivity.getResources().getIdentifier("toolbar", "id", homeActivity.getPackageName()));
            var logo = toolbar.findViewById(toolbar.getResources().getIdentifier("toolbar_logo", "id", homeActivity.getPackageName()));
            var parent = (LinearLayout) logo.getParent();
            var startup_prefs = homeActivity.getSharedPreferences("startup_prefs", Context.MODE_PRIVATE);
            var mainPrefs = homeActivity.getSharedPreferences(homeActivity.getPackageName() + "_preferences_light", Context.MODE_PRIVATE);

            var mTitle = new TextView(homeActivity);
            mTitle.setText(showName ? startup_prefs.getString("push_name", "WhatsApp") : "WhatsApp");
            mTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
            mTitle.setTextSize(20f);
            mTitle.setTextColor(0xffffffff);
            parent.addView(mTitle);

            if (showBio) {
                var mSubtitle = new TextView(homeActivity);
                mSubtitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
                mSubtitle.setText(mainPrefs.getString("my_current_status", ""));
                mSubtitle.setTextSize(12f);
                mSubtitle.setTextColor(0xffffffff);
                mSubtitle.setMarqueeRepeatLimit(-1);
                mSubtitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                mSubtitle.setSingleLine();
                mSubtitle.setSelected(true);
                parent.addView(mSubtitle);

            } else {
                mTitle.setGravity(Gravity.CENTER);
            }
            parent.removeView(logo);
        }
    }
}
