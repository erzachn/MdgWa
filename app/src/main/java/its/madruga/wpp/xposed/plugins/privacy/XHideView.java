package its.madruga.wpp.xposed.plugins.privacy;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.wpp.xposed.Unobfuscator;
import its.madruga.wpp.xposed.models.XHookBase;

public class XHideView extends XHookBase {

    public XHideView(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Exception {

        var hideread = prefs.getBoolean("hideread", false);
        var hidereadstatus = prefs.getBoolean("hidestatusview", false);

        if (!hideread && !hidereadstatus) return;

        if (hideread) {

            Method methodHideViewCollection = Unobfuscator.loadHideViewCollectionMethod(loader);
            logDebug(Unobfuscator.getMethodDescriptor(methodHideViewCollection));

            var methodPlayerViewJid = Unobfuscator.loadHideViewAudioMethod(loader);
            logDebug(Unobfuscator.getMethodDescriptor(methodPlayerViewJid));

            var methodHideOnceViewMethod = Unobfuscator.loadHideOnceViewMethod(loader);
            logDebug(Unobfuscator.getMethodDescriptor(methodHideOnceViewMethod));

            XposedBridge.hookMethod(methodHideOnceViewMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (param.args[1] instanceof Boolean) {
                        param.args[1] = false;
                    }
                }
            });

            XposedBridge.hookMethod(methodHideViewCollection, XC_MethodReplacement.DO_NOTHING);
            XposedBridge.hookMethod(methodPlayerViewJid,XC_MethodReplacement.returnConstant(true));
        }

        if (hidereadstatus) {
            var methodHideViewJid = Unobfuscator.loadHideViewJidMethod(loader);
            logDebug(Unobfuscator.getMethodDescriptor(methodHideViewJid));
            XposedBridge.hookMethod(methodHideViewJid, XC_MethodReplacement.returnConstant(null));
        }
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Hide View";
    }

}
