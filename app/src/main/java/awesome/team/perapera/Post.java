package awesome.team.perapera;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * This class represents each Post. It is used in Topic. Created by gumb on 12/14/14. .
 */
public class Post {
    // The ID of this post
    private int id;

    // The sound file name of this post
    private String soundFile;


    /* *
    * Constructor
    */
    public Post(int topicId, ScreamsOpenHelper mDbHelper) {
        File sdCard = Environment.getExternalStorageDirectory();
        File from = new File(sdCard, "/perapera/audioRecordTest.3gp");

        id = System.identityHashCode(this);

        File to = new File(sdCard, "/perapera/" + Integer.toString(id) + ".3gp");

        if (!from.renameTo(to))
            Log.e("RenameTest", "Failed to rename!");
        soundFile = to.toString();

        /// Insert into database
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ScreamsContract.PostEntry.KEY_ID, id);
        values.put(ScreamsContract.PostEntry.KEY_TOPIC_ID, topicId);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                ScreamsContract.PostEntry.POSTS_TABLE_NAME,
                null,
                values);
    }

    /* *
    * Constructor from DB
    */
    public Post(int id) {
        File sdCard = Environment.getExternalStorageDirectory();
        File to = new File(sdCard, "/perapera/" + Integer.toString(id) + ".3gp");

        this.id = id;
        soundFile = to.toString();
    }

    /* Returns the sound file. */
    public String getSoundFile() {
        return soundFile;
    }

    /* Returns the id. */
    public int getId() {
        return id;
    }
}