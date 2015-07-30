package awesome.team.perapera;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class contains all database methods. Created by gumb on 25.01.15.
 */
public class ScreamsOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "screams.db";
    private static final String POSTS_TABLE_CREATE =
            "CREATE TABLE " + ScreamsContract.PostEntry.POSTS_TABLE_NAME + " (" +
                    ScreamsContract.PostEntry.KEY_ID + " INTEGER PRIMARY KEY, " +
                    ScreamsContract.PostEntry.KEY_TOPIC_ID + " INTEGER REFERENCES " +
                    ScreamsContract.TopicEntry.TOPICS_TABLE_NAME + "(" +
                    ScreamsContract.TopicEntry.KEY_ID + "));";

    private static final String TOPICS_TABLE_CREATE =
            "CREATE TABLE " + ScreamsContract.TopicEntry.TOPICS_TABLE_NAME + " (" +
                    ScreamsContract.TopicEntry.KEY_ID + " INTEGER PRIMARY KEY, " +
                    ScreamsContract.TopicEntry.KEY_TITLE + " TEXT);";


    ScreamsOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(POSTS_TABLE_CREATE);
        db.execSQL(TOPICS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w(ScreamsOpenHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ScreamsContract.PostEntry.POSTS_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ScreamsContract.TopicEntry.TOPICS_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}