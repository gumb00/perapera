package awesome.team.perapera;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gumb on 12/14/14. This represents the first post of a topic and contains all of its
 * answers as posts.
 */
public class Topic implements Parcelable {
    /* This is mandatory when using parcels. It tells the intent how to parcel this object. */
    public static Creator<Topic> CREATOR = new Creator<Topic>() {
        @Override
        public Topic createFromParcel(Parcel parcel) {
            return new Topic(parcel);
        }

        @Override
        public Topic[] newArray(int i) {
            return new Topic[i];
        }
    };
    // The ID of the first post
    private int id;
    // The sound file name of the first post
    private String soundFile;
    // The heading of the first post;
    private String title;
    // The tags for the first post;
    private String tags;
    // All the other posts TODO Synchronize it?
    private List<Integer> posts;

    /* Normal constructor */
    public Topic(String title, String tags, ScreamsOpenHelper mDbHelper) {
        File sdCard = Environment.getExternalStorageDirectory();
        File from = new File(sdCard, "/perapera/audioRecordTest.3gp");

        id = System.identityHashCode(this);

        File to = new File(sdCard, "/perapera/" + Integer.toString(id) + ".3gp");

        if (!from.renameTo(to))
            Log.e("RenameTest", "Failed to rename!");
        soundFile = to.getAbsolutePath();
        this.title = title;
        this.tags = tags;
        posts = new ArrayList<>();

        /// Insert into database
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(ScreamsContract.TopicEntry.KEY_ID, id);
        values.put(ScreamsContract.TopicEntry.KEY_TITLE, title);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                ScreamsContract.TopicEntry.TOPICS_TABLE_NAME,
                ScreamsContract.TopicEntry.KEY_TITLE,
                values);
    }

    /* Constructor from DB. */
    public Topic(int id, String title) {
        File sdCard = Environment.getExternalStorageDirectory();

        this.id = id;

        File to = new File(sdCard, "/perapera/" + Integer.toString(id) + ".3gp");

        soundFile = to.getAbsolutePath();
        this.title = title;
        this.tags = title;
        posts = new ArrayList<>(); // TODO Posts will be loaded when opening a topic
    }

    /* Creates the object from a parcel (when it was passed to another view with an intent).*/
    public Topic(Parcel in) {
        id = in.readInt();
        posts = new ArrayList<>();
        soundFile = in.readString();
        title = in.readString();
        tags = in.readString();

        // Fill the list
        in.readList(posts, Post.class.getClassLoader());
    }

    /* Adds a new post to the list. */
    public void addPost(int postID) {
        posts.add(postID);
    }

    /* Returns the id. */
    public int getId() {
        return id;
    }

    /* Returns the title. */
    public String getTitle() {
        return title;
    }

    /* Returns the sound file. */
    public String getSoundFile() {
        return soundFile;
    }

    public List<Integer> getPosts() {
        return posts;
    }

    /* No idea what this is for. */
    @Override
    public int describeContents() {
        return 0;
    }

    /* Parcels it when passing it with an intent. */
    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(soundFile);
        parcel.writeString(title);
        parcel.writeString(tags);
        parcel.writeList(posts);
    }
}