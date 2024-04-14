package its.madruga.wpp.xposed.plugins.functions;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.wpp.xposed.Unobfuscator;
import its.madruga.wpp.xposed.models.XHookBase;

public class XAntiEditMessage extends XHookBase {
    public XAntiEditMessage(@NonNull ClassLoader loader, @NonNull XSharedPreferences preferences) {
        super(loader, preferences);
    }

    @Override
    public void doHook() throws Throwable {


        var originalMessageMethod = Unobfuscator.loadOriginalMessageMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(originalMessageMethod));
        var newMessageMethod = Unobfuscator.loadNewMessageMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(newMessageMethod));
        var setMessageMethod = Unobfuscator.loadSetMessageMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(setMessageMethod));

        XposedBridge.hookMethod(originalMessageMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var oNewMessage = param.args[0];
                var callObject = param.getResult();
                XposedBridge.hookMethod(setMessageMethod, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.thisObject == callObject) {
                            var origMessage = (String) newMessageMethod.invoke(oNewMessage);
                            var newMessage = param.args[0];
                            newMessage += "\n\n*Original message:*\n" + origMessage;
                            param.args[0] = newMessage;
                        }
                    }
                });
            }
        });
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Anti Edit Message";
    }
}
