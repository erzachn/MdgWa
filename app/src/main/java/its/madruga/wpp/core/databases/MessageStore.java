package its.madruga.wpp.core.databases;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import de.robv.android.xposed.XposedBridge;

public class MessageStore extends DatabaseModel {
    public MessageStore(String dbName, ClassLoader loader) {
        super(dbName, loader);
    }

    public static String getMessageById(long id) {
        String message = "";
        try {
            Cursor cursor = database.getReadableDatabase().rawQuery("SELECT docid, c0content FROM message_ftsv2_content WHERE docid=\"" + id + "\"", null);
            cursor.moveToFirst();
            XposedBridge.log("Count: " + cursor.getCount());
            if(cursor.getCount() <= 0) {
                cursor.close();
                return "";
            }
            message = cursor.getString(1);
            cursor.close();
        }
        catch(Exception ignored) {
            XposedBridge.log(ignored);
        }
        return message;
    }

}