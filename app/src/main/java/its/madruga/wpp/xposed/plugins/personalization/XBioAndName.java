package its.madruga.wpp.xposed.plugins.personalization;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

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

    public static String getBio(Activity activity) {
        return activity.getSharedPreferences(activity.getPackageName() + "_preferences_light", Context.MODE_PRIVATE).getString("my_current_status", ".");
    }

    public static String getName(Activity activity) {
        return activity.getSharedPreferences("startup_prefs", Context.MODE_PRIVATE).getString("push_name", "MdgWa");
    }

    @Override
    public void doHook() {
        var showName = prefs.getBoolean("shownamehome", false);
        var showBio = prefs.getBoolean("showbiohome", false);
        var methodHook = new MethodHook(showName, showBio);
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

            var actionBar = XposedHelpers.callMethod(param.thisObject, "getSupportActionBar");
            var homeActivity = (Activity) param.thisObject;
            var bio = getBio(homeActivity);
            var name = getName(homeActivity);
            XposedBridge.log("Bio: " + bio + ", Name: " + name);
            // 1 to set Title, 0 to set Summary
            var methods = Arrays.stream(actionBar.getClass().getDeclaredMethods()).filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0].equals(CharSequence.class)).toArray(Method[]::new);
            XposedBridge.log("ActionBar class: " + actionBar.getClass().getName());

            if (showName) {
                methods[1].invoke(actionBar, name);
                XposedBridge.log(methods[1].getName());
            }
            if (showBio) {
                methods[0].invoke(actionBar, bio);
                XposedBridge.log(methods[0].getName());
            }
            XposedBridge.hookMethod(methods[1], new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (showName) param.args[0] = name;
                }
            });
            XposedBridge.hookMethod(methods[0], new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (showBio) param.args[0] = bio;
                }
            });
        }
    }
}
