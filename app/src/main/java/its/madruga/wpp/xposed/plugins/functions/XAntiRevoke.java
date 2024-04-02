package its.madruga.wpp.xposed.plugins.functions;

import static its.madruga.wpp.xposed.plugins.core.XMain.mApp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.AsyncTask;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import its.madruga.wpp.xposed.Unobfuscator;
import its.madruga.wpp.xposed.models.XHookBase;
import its.madruga.wpp.xposed.plugins.core.XMain;

public class XAntiRevoke extends XHookBase {

    private static HashSet<String> messageRevokedList = new HashSet<>();
    @SuppressLint("StaticFieldLeak")
    private static Activity mConversation;
    private static SharedPreferences mShared;
    private static Field fieldMessageKey;

    public XAntiRevoke(ClassLoader loader, XSharedPreferences preferences) {
        super(loader, preferences);
    }

    private void isMRevoked(Object objMessage, TextView dateTextView, String antirevokeType) {
        if (dateTextView == null) return;
        var fieldMessageDetails = XposedHelpers.getObjectField(objMessage, fieldMessageKey.getName());
        var messageKey = (String) XposedHelpers.getObjectField(fieldMessageDetails, "A01");
        var stripJID = stripJID(getJidAuthor(objMessage));
        if (messageRevokedList.isEmpty()) {
            String[] currentRevokedMessages = getRevokedMessages(objMessage);
            if (currentRevokedMessages == null) currentRevokedMessages = new String[]{""};
            Collections.addAll(messageRevokedList, currentRevokedMessages);
        }
        if (messageRevokedList != null && messageRevokedList.contains(messageKey)) {
            var antirevokeValue = prefs.getInt(antirevokeType, 0);
            if (antirevokeValue == 1) {
                // Text
                var newTextData = "Deleted Message" + " | " + dateTextView.getText();
                dateTextView.setText(newTextData);
            } else if (antirevokeValue == 2) {
                // Icon
                var iconId = mApp.getResources().getIdentifier("msg_status_client_revoked", "drawable", mApp.getPackageName());
                var drawable = mApp.getDrawable(iconId);
                drawable.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP));
                dateTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                dateTextView.setCompoundDrawablePadding(5);
            }
        } else {
            dateTextView.setCompoundDrawables(null, null, null, null);
            var revokeNotice = "Deleted Message" + " | ";
            var dateText = dateTextView.getText().toString();
            if (dateText.contains(revokeNotice)) {
                dateTextView.setText(dateText.replace(revokeNotice, ""));
            }
        }
    }

    @Override
    public void doHook() throws Exception {
        mShared = mApp.getSharedPreferences(mApp.getPackageName() + "_mdgwa_preferences", Context.MODE_PRIVATE);
        var antirevoke = prefs.getInt("antirevoke", 0);
        var antirevokestatus = prefs != null ? prefs.getInt("antirevokestatus", 0) : 0;

        var onStartMethod = Unobfuscator.loadAntiRevokeOnStartMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(onStartMethod));

        var onResumeMethod = Unobfuscator.loadAntiRevokeOnResumeMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(onResumeMethod));

        var convChatField = Unobfuscator.loadAntiRevokeConvChatField(loader);
        logDebug(Unobfuscator.getFieldDescriptor(convChatField));

        var chatJidField = Unobfuscator.loadAntiRevokeChatJidField(loader);
        logDebug(Unobfuscator.getFieldDescriptor(chatJidField));

        var antiRevokeMessageMethod = Unobfuscator.loadAntiRevokeMessageMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(antiRevokeMessageMethod));

        var classThreadMessage = Unobfuscator.loadThreadMessageClass(loader);
        logDebug("Class: " + classThreadMessage);

        fieldMessageKey = Unobfuscator.loadAntiRevokeMessageKeyField(loader);
        logDebug(Unobfuscator.getFieldDescriptor(fieldMessageKey));

        var bubbleMethod = Unobfuscator.loadAntiRevokeBubbleMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(bubbleMethod));

        var unknownStatusPlaybackMethod = Unobfuscator.loadUnknownStatusPlaybackMethod(loader);
        logDebug(Unobfuscator.getMethodDescriptor(unknownStatusPlaybackMethod));

        var statusPlaybackField = Unobfuscator.loadStatusPlaybackField(loader);
        logDebug(Unobfuscator.getFieldDescriptor(statusPlaybackField));

        XposedBridge.hookMethod(onStartMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                mConversation = (Activity) param.thisObject;
                var chatField = XposedHelpers.getObjectField(mConversation, convChatField.getName());
                var chatJidObj = XposedHelpers.getObjectField(chatField, chatJidField.getName());
                setCurrentJid(stripJID(getRawString(chatJidObj)));
            }
        });

        XposedBridge.hookMethod(onResumeMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                mConversation = (Activity) param.thisObject;
                var chatField = XposedHelpers.getObjectField(mConversation, convChatField.getName());
                var chatJidObj = XposedHelpers.getObjectField(chatField, chatJidField.getName());
                setCurrentJid(stripJID(getRawString(chatJidObj)));
            }
        });

        if (antirevoke != 0 || antirevokestatus != 0) {

            XposedBridge.hookMethod(antiRevokeMessageMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    var objMessage = classThreadMessage.cast(param.args[0]);
                    var fieldMessageDetails = XposedHelpers.getObjectField(objMessage, fieldMessageKey.getName());
                    var fieldIsFromMe = XposedHelpers.getBooleanField(fieldMessageDetails, "A02");
                    if (!fieldIsFromMe) {
                        if (antiRevoke(objMessage) != 0) param.setResult(true);
                    }
                }
            });

        }
        XposedBridge.hookMethod(bubbleMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                var objMessage = param.args[2];
                var dateTextView = (TextView) param.args[1];
                isMRevoked(objMessage, dateTextView, "antirevoke");
            }
        });

        XposedBridge.hookMethod(unknownStatusPlaybackMethod, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                var obj = param.args[1];
                var objMessage = param.args[0];
                Object objView = statusPlaybackField.get(obj);
                Field[] textViews = Arrays.stream(statusPlaybackField.getType().getDeclaredFields()).filter(f -> f.getType() == TextView.class).toArray(Field[]::new);
                if (textViews == null) {
                    log("Could not find TextView");
                    return;
                }
                int dateId = XMain.mApp.getResources().getIdentifier("date", "id", "com.whatsapp");
                for (Field textView : textViews) {
                    TextView textView1 = (TextView) XposedHelpers.getObjectField(objView, textView.getName());
                    if (textView1 == null || textView1.getId() == dateId) {
                        logDebug("textView = " + textView.getName());
                        logDebug("keyMessage = " + param.args[0]);
                        isMRevoked(objMessage, textView1, "antirevokestatus");
                        break;
                    }
                }
            }
        });

    }

    @NonNull
    @Override
    public String getPluginName() {
        return "Anti Revoke";
    }

    private static void saveRevokedMessage(String authorJid, String messageKey, Object objMessage) {
        String newRevokedMessages;
        String[] revokedMessagesArray = getRevokedMessages(objMessage);
        if (revokedMessagesArray != null) {
            HashSet<String> newRevokedMessagesArray = new HashSet<>();
            Collections.addAll(newRevokedMessagesArray, revokedMessagesArray);
            newRevokedMessagesArray.add(messageKey);
            messageRevokedList = newRevokedMessagesArray;
            newRevokedMessages = Arrays.toString(newRevokedMessagesArray.toArray());
        } else {
            newRevokedMessages = "[" + messageKey + "]";
            messageRevokedList = new HashSet<>(Collections.singleton(messageKey));
        }
        mShared.edit().putString(authorJid + "_revoked", newRevokedMessages).apply();
    }

    private int antiRevoke(Object objMessage) {
        var messageKey = (String) XposedHelpers.getObjectField(objMessage, "A01");
        var stripJID = stripJID(getJidAuthor(objMessage));
        var revokeboolean = stripJID.equals("status") ? prefs.getInt("antirevokestatus", 0) : prefs.getInt("antirevoke", 0);
        if (revokeboolean == 0) return revokeboolean;
        if (!messageRevokedList.contains(messageKey)) {
            try {
                AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
                    saveRevokedMessage(stripJID, messageKey, objMessage);
                    try {
                        if (mConversation != null && getCurrentJid().equals(stripJID)) {

                            mConversation.runOnUiThread(() -> {
                                if (mConversation.hasWindowFocus()) {
                                    mConversation.startActivity(mConversation.getIntent());
                                    mConversation.overridePendingTransition(0, 0);
                                } else {
                                    mConversation.recreate();
                                }
                            });
                        }
                    } catch (Exception e) {
                        XposedBridge.log(e.getMessage());
                    }
                });
            } catch (Exception e) {
                XposedBridge.log(e.getMessage());
            }
        }
        return revokeboolean;
    }

    private static String[] getRevokedMessages(Object objMessage) {
        String stripJID = stripJID(getJidAuthor(objMessage));
        try {
            String revokedsString = mShared.getString(stripJID + "_revoked", "");
            if (revokedsString.isEmpty()) {
                return null;
            } else return StringToStringArray(revokedsString);
        } catch (Exception e) {
            XposedBridge.log(e.getMessage());
            return null;
        }
    }

    private static String stripJID(String str) {
        try {
            return (str.contains("@g.us") || str.contains("@s.whatsapp.net") || str.contains("@broadcast")) ? str.substring(0, str.indexOf("@")) : str;
        } catch (Exception e) {
            XposedBridge.log(e.getMessage());
            return str;
        }
    }

    private static String getJidAuthor(Object objMessage) {
        Object fieldMessageDetails = XposedHelpers.getObjectField(objMessage, fieldMessageKey.getName());
        Object fieldMessageAuthorJid = XposedHelpers.getObjectField(fieldMessageDetails, "A00");
        if (fieldMessageAuthorJid == null) return "";
        else return getRawString(fieldMessageAuthorJid);
    }

    private static String getRawString(Object objJid) {
        if (objJid == null) return "";
        else return (String) XposedHelpers.callMethod(objJid, "getRawString");
    }

    private static void setCurrentJid(String jid) {
        if (jid == null || mShared == null) return;
        mShared.edit().putString("jid", jid).apply();
    }

    private static String getCurrentJid() {
        if (mShared == null) return "";
        else return mShared.getString("jid", "");
    }

    private static String[] StringToStringArray(String str) {
        try {
            return str.substring(1, str.length() - 1).replaceAll("\\s", "").split(",");
        } catch (Exception unused) {
            return null;
        }
    }

}
