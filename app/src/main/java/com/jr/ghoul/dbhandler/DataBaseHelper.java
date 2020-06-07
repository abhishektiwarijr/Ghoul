package com.jr.ghoul.dbhandler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jr.ghoul.wrapper.AppInfo;

import java.io.File;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "rca.db";
    public static final int DATABASE_VERSION = 1;
    private Context myContext;
    private SQLiteDatabase sqLiteDatabase = getWritableDatabase();

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.myContext = context;
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        Log.w("Db oncrate called", "Db oncreate called");
        sQLiteDatabase.execSQL("create table apps (id integer primary key autoincrement, p_name text, a_name text);");
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        this.myContext.getDatabasePath("dest.sqLiteDatabase");
    }

    public boolean attach(File file, boolean z) {
        try {
            this.sqLiteDatabase.execSQL("attach database ? as sqLiteDatabase", new String[]{file.getAbsolutePath()});
            if (!z) {
                this.sqLiteDatabase.delete("apps", null, null);
            }
            this.sqLiteDatabase.execSQL("INSERT INTO apps (p_name, a_name) SELECT  p_name, a_name FROM sqLiteDatabase.apps");
            return file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<AppInfo> isExist(List<AppInfo> list, List<AppInfo> list2) {
        list2.clear();
        for (AppInfo appInfo : list) {
            String sb = "select * from  apps where p_name ='" + appInfo.packageName + "'";
            Cursor rawQuery = this.sqLiteDatabase.rawQuery(sb, null);
            if (rawQuery != null && rawQuery.moveToFirst()) {
                do {
                    list2.add(appInfo);
                } while (rawQuery.moveToNext());
                rawQuery.close();
            }
        }
        return list2;
    }
}
