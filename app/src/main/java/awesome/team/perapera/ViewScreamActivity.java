package awesome.team.perapera;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Detailed view of a topic.
 */
public class ViewScreamActivity extends Activity {
    List<Post> posts;
    ListView listView;
    private Topic topic;
    private MediaControl mediaControl;
    private MediaListen mediaListen;
    private ImageButton recordButton;
    private Button listenButton;
    private Button postButton;
    private String topicFile;
    private ScreamsOpenHelper mDbHelper;

    /* Fetches the topic and sets some view stuff. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_scream);

        // Set up DB helper
        mDbHelper = new ScreamsOpenHelper(this.getApplicationContext());

        // Contains all the data
        topic = getIntent().getExtras().getParcelable("topic");
        topicFile = topic.getSoundFile();

        TextView title = (TextView) findViewById(R.id.title);

        title.setText(topic.getTitle());
        listView = (ListView) findViewById(R.id.list_screams);
        createListContent();
        setupButtons();
    }

    private void setupButtons() {
        postButton = (Button) findViewById(R.id.post_button);
        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);

        listenButton = (Button) findViewById(R.id.listen_button);
        recordButton = (ImageButton) findViewById(R.id.record_button);
        mediaControl = new MediaControl(recordButton, listenButton);
        mediaListen = new MediaListen();
        recordButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Change buttons according to user input.
                mediaControl.onRecord(!mediaControl.isRecording); // Default is false
                if (mediaControl.isRecording) {
                    recordButton.setActivated(true);
                    listenButton.setEnabled(false);
                    postButton.setEnabled(false);
                } else {
                    recordButton.setActivated(false);
                    listenButton.setEnabled(true);
                    postButton.setEnabled(true);
                }
            }
        });
        listenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mediaControl.onPlay(!mediaControl.isPlaying); // Default is false
                if (mediaControl.isPlaying) {
                    recordButton.setEnabled(false);
                } else {
                    recordButton.setEnabled(true);
                }
            }
        });
        // TODO small fix here
        imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(v.isActivated()) {
                    v.setActivated(false);
                } else {
                    v.setActivated(true);
                }
                mediaListen.playPause(topicFile);
            }
        });
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Post newPost = new Post(topic.getId(), mDbHelper);

                postButton.setEnabled(false);
                listenButton.setEnabled(false);
                posts.add(newPost);
                refreshList();
            }
        });
    }

    /* Create the ListView content. */
    private void createListContent() {
        /// Read from database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database you will actually
        // use after this query.
        String[] projection = {
                ScreamsContract.PostEntry.KEY_ID
        };
        String selection = ScreamsContract.PostEntry.KEY_TOPIC_ID + " = ?";
        String[] selectionVal = {
                Integer.toString(topic.getId())
        };
        Cursor cursor = db.query(
                ScreamsContract.PostEntry.POSTS_TABLE_NAME,  // The table to query
                projection,                                  // The columns to return
                selection,                                   // The columns for the WHERE clause
                selectionVal,                                // The values for the WHERE clause
                null,                                        // don't group the rows
                null,                                        // don't filter by row groups
                null                                         // The sort order
        );
        posts = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                posts.add(new Post(cursor.getInt(cursor.getColumnIndex("id"))));
                cursor.moveToNext();
            }
        }
        refreshList();
    }

    private void refreshList() {
        String[] values = new String[posts.size()];
        final List<Post> fPosts = new ArrayList<>(posts);

        for (int i = 0; i < posts.size(); i++) {
            values[i] = "Answer " + Integer.toString(i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.
                simple_list_item_1, values);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final String soundFile = fPosts.get(position).getSoundFile();
                mediaListen.playPause(soundFile);
            }
        });
    }

    /* Deletes a topic from the internal memory */
    public void discardTopic() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define 'where' part of query.
        String selectionTopic = ScreamsContract.TopicEntry.KEY_ID + " LIKE ?";
        String selectionPosts = ScreamsContract.PostEntry.KEY_TOPIC_ID + " LIKE ?";

        // Specify arguments in placeholder order.
        String[] selectionArgs = {String.valueOf(topic.getId())};

        // Issue SQL statement.
        db.delete(ScreamsContract.TopicEntry.TOPICS_TABLE_NAME, selectionTopic, selectionArgs);
        db.delete(ScreamsContract.PostEntry.POSTS_TABLE_NAME, selectionPosts, selectionArgs);

        // Start the new Activity. This could be done later on using a ContentProvider
        Intent intent = new Intent(this, ScreamActivity.class);
        intent.putExtra("refresh", true);
        startActivity(intent);
    }

    /* Adds items to the action bar. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_scream, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /* Handles action bar item clicks. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on
        // the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_discard:
                discardTopic();

                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}