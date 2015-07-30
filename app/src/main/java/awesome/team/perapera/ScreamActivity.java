package awesome.team.perapera;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


/**
 * This is the main activity where one can create new topics and view old ones.
 */
public class ScreamActivity extends DrawerActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // DEBUG: The test topics
    private static List<Topic> myTopics;
    private static MyScreamsFragment myScreamsFragment;
    private static Location mLastLocation;
    // We use FragmentPagerAdapter derivative, which will keep every loaded fragment in memory.
    private SectionsPagerAdapter mSectionsPagerAdapter;
    // The ViewPager that will host the section contents.
    private ViewPager mViewPager;
    private ScreamsOpenHelper mDbHelper;
    // For locating the user
    private GoogleApiClient mGoogleApiClient;

    /* Set up SectionsPagerAdapter and ViewPager on create */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_scream);
        super.onCreate(savedInstanceState);

        myTopics = new ArrayList<>();
        mDbHelper = new ScreamsOpenHelper(getApplicationContext());

        // Create the adapter that will return a fragment for each of the three primary sections of
        // the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Set title of the activity so it will differ from the app name
        setTitle(R.string.title_activity_scream);

        // DEBUG Create a few topics for testing
        //CreateTestTopics();

        // Kick off the process of building a GoogleApiBla
        buildGoogleApiClient();
    }

    /* This is debug to create dummy sound files. */
    private void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();

        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    /* DEBUG: Copy the debug file to make creating new topics and posts possible.*/
    private void CreateSound() {
        File sdCard = Environment.getExternalStorageDirectory();
        File from = new File(sdCard, "/perapera/audioDebug.3gp");
        File to = new File(sdCard, "/perapera/audioRecordTest.3gp");
        try {
            copy(from, to);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /* DEBUG: Create a new RecordAudioTest file and then create debug topics and posts. */
    private void CreateTestTopics() {
        CreateSound();

        Topic t1 = new Topic("#tram #trash", "#death", mDbHelper);

        CreateSound();

        Topic t2 = new Topic("#party #hook", "#shit", mDbHelper);
        Topic[] tempTopics = new Topic[]{t1, t2};

        List<Topic> topics = new ArrayList<>();
        Collections.addAll(topics, tempTopics);
        for (int i = 0; i < 5; i++) {
            CreateSound();
            topics.get(0).addPost((new Post(topics.get(0).getId(), mDbHelper)).getId());
        }
        for (int i = 0; i < 10; i++) {
            CreateSound();
            topics.get(1).addPost((new Post(topics.get(1).getId(), mDbHelper)).getId());
        }
    }

    /* Inflate action bar. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scream, menu);

        return true;
    }

    /* Handle action bar item clicks here. The action bar will automatically handle clicks on the
    Home/Up button, so long as you specify a parent actLastivity in AndroidManifest.xml. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * A fragment containing a simple button for screaming.
     */
    public static class ScreamFragment extends Fragment {
        // The fragment argument representing the section number for this fragment.
        private static final String ARG_SECTION_NUMBER = "section_number";

        // Variables for sound control
        private ImageButton screamBtn = null;
        private Button listenBtn = null;
        private MediaControl mediaControl;

        // Variables for creating a new Topic
        private Button submitBtn = null;
        private EditText mEdit = null;


        /* Virtual constructor is needed for static class. */
        public ScreamFragment() {
        }

        /* Returns a new instance of this fragment for the given section number. */
        public static ScreamFragment newInstance(int sectionNumber) {
            ScreamFragment fragment = new ScreamFragment();
            Bundle args = new Bundle();

            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        /* Inflate the Layout when creating this fragment */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_scream, container, false);

            // TextField control
            mEdit = (EditText) rootView.findViewById(R.id.editTags);
            mEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!TextUtils.isEmpty(mEdit.getText().toString())
                            && listenBtn.isEnabled())
                        submitBtn.setEnabled(true);
                }
            });

            // Enable submit-Button when writing TODO force peps to use hash tags
            mEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!TextUtils.isEmpty(mEdit.getText().toString())
                            && listenBtn.isEnabled())
                        submitBtn.setEnabled(true);
                }
            });

            // Topic control
            submitBtn = (Button) rootView.findViewById(R.id.submit_button);
            submitBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    listenBtn.setEnabled(false);
                    submitBtn.setEnabled(false);

                    final Topic t = new Topic(mEdit.getText().toString(), "#test",
                            new ScreamsOpenHelper(getActivity().getApplicationContext()));

                    myTopics.add(t);
                    mEdit.setText("");
                    myScreamsFragment.refresh();

                    ConnectionManager cm = ConnectionManager.getInstance(getActivity());
                    JSONObject params = new JSONObject();
                    JSONObject loc = new JSONObject();
                    try {
                        params.put("_id", String.valueOf(t.getId()));
                        params.put("file", String.valueOf(t.getId()) + ".3gp");
                        params.put("title", String.valueOf(t.getTitle()));
                        loc.put("long", String.valueOf(mLastLocation.getLongitude())); // long has to be first!
                        loc.put("lat", String.valueOf(mLastLocation.getLatitude()));
                        params.put("loc", loc);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, cm.url + "/screams", params,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject o) {
                                    System.out.println(o.toString());
                                    // pDialog.hide();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                    //pDialog.hide();
                                }
                            });
                    cm.addToRequestQueue(postRequest);
                    cm.uploadFile("tag", cm.url + "/screams/" + String.valueOf(t.getId()) + "/file", new File(t.getSoundFile()), "partname", null, null, null, null);
                }
            });

            // Sound control
            screamBtn = (ImageButton) rootView.findViewById(R.id.scream_button);
            listenBtn = (Button) rootView.findViewById(R.id.listen_button);
            mediaControl = new MediaControl(screamBtn, listenBtn);
            mediaControl.addListener(new RecordingStoppedEventListener() {
                @Override
                public void recordingStopped() {
                    listenBtn.setEnabled(true);
                    screamBtn.setEnabled(true);
                    if (!TextUtils.isEmpty(mEdit.getText().toString()))
                        submitBtn.setEnabled(true);
                }
            });
            screamBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    screamBtn.setActivated(true);
                    if (mediaControl.isRecording) {
                        // Add feedback for user that screaming stopped here
                        screamBtn.setActivated(false);
                        listenBtn.setEnabled(true);
                    }
                    mediaControl.onRecord(!mediaControl.isRecording); // default is false
                }
            });
            listenBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mediaControl.onPlay(!mediaControl.isPlaying); // default is false
                    if (mediaControl.isPlaying)
                        screamBtn.setEnabled(false);
                    else
                        screamBtn.setEnabled(true);
                }
            });

            return rootView;
        }
    }

    /**
     * A fragment containing a simple list of own Screams.
     */
    public static class MyScreamsFragment extends ListFragment {
        // The fragment argument representing the section number for this fragment.
        private static final String ARG_SECTION_NUMBER = "section_number";

        /* Virtual constructor is needed for static class */
        public MyScreamsFragment() {
        }

        /* Returns a new instance of this fragment for the given section number. */
        public static MyScreamsFragment newInstance(int sectionNumber) {
            MyScreamsFragment fragment = new MyScreamsFragment();
            Bundle args = new Bundle();

            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        /* Associate with ListView and fill the List with content */
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            /// Read from database
            ScreamsOpenHelper mDbHelper = new ScreamsOpenHelper(getActivity().
                    getApplicationContext());
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

            // Define a projection that specifies which columns from the database you will actually
            // use after this query.
            String[] projection = {
                    ScreamsContract.TopicEntry.KEY_ID,
                    ScreamsContract.TopicEntry.KEY_TITLE
            };
            Cursor cursor = db.query(
                    ScreamsContract.TopicEntry.TOPICS_TABLE_NAME,// The table to query
                    projection,                                  // The columns to return
                    null,                                        // The columns for the WHERE clause
                    null,                                        // The values for the WHERE clause
                    null,                                        // don't group the rows
                    null,                                        // don't filter by row groups
                    null                                         // The sort order
            );

            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    myTopics.add(new Topic(cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("title"))));
                    cursor.moveToNext();
                }
            }

            // Create the ListView content
            String[] values = new String[myTopics.size()];

            for (int i = 0; i < myTopics.size(); i++)
                values[i] = myTopics.get(i).getTitle();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.
                    simple_list_item_1, values);

            setListAdapter(adapter);
            myScreamsFragment = this;
        }

        public void refresh() {
            // Create the ListView content
            String[] values = new String[myTopics.size()];

            for (int i = 0; i < myTopics.size(); i++)
                values[i] = myTopics.get(i).getTitle();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.
                    simple_list_item_1, values);

            setListAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        /* Start an activity when clicking on an Item */
        @Override
        public void onListItemClick(ListView l, View view, int position, long id) {
            Intent intent = new Intent(getActivity(), ViewScreamActivity.class);

            intent.putExtra("topic", myTopics.get(position));
            startActivity(intent);
        }
    }

    /**
     * A FragmentPagerAdapter that returns a fragment corresponding to one of the tabs.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        /* Constructor */
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /* getItem is called to instantiate the fragment for the given page. Return a Fragment
        (defined as a static inner class below). */
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ScreamFragment.newInstance(position + 1);
                case 1:
                    return MyScreamsFragment.newInstance(position + 1);
            }

            return ScreamFragment.newInstance(position + 1);
        }

        /* Returns the amount of total pages. */
        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        /* Returns the titles of the fragments. */
        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();

            switch (position) {
                case 0:
                    return getString(R.string.title_activity_scream).toUpperCase(l);
                case 1:
                    return getString(R.string.title_myScreams_fragment).toUpperCase(l);
            }

            return null;
        }
    }
}
