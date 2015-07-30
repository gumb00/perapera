package awesome.team.perapera;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Activity which contains multiple fragments and lets the user discover new topics or search for
 * topics with certain tags.
 */
public class DiscoverActivity extends DrawerActivity {
    private static JSONObject currentScreamJson = null;
    private static String currentScreamUrl = null;
    private static boolean displayJsonImmediately = false;

    // The PagerAdapter that will provide fragments for each of the sections. We use a
    // FragmentPagerAdapter derivative, which will keep every loaded fragment in memory. If this
    // becomes too memory intensive, it may be best to switch to a FragmentStatePagerAdapter.
    SectionsPagerAdapter mSectionsPagerAdapter;
    // The ViewPager that will host the section contents.
    ViewPager mViewPager;

    /* Create SectionsPagerAdapter when creating the view. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_discover);
        super.onCreate(savedInstanceState);

        // Create the adapter that will return a fragment for each of the three primary sections of
        // the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    /* Add items to the action bar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_discover, menu);
        return true;
    }

    /* Handle item clicks */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks on
        // the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    /**
     * The discover fragment which lets the user find a random post to answer.
     */
    public static class DiscoverFragment extends Fragment {
        /* The fragment argument representing the section number for this fragment. */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private MediaListen mediaListen = new MediaListen();
        private Button answerBtn = null;

        /* Virtual constructor */
        public DiscoverFragment() {
        }

        /* Returns a new instance of this fragment for the given section number. */
        public static DiscoverFragment newInstance(int sectionNumber) {
            DiscoverFragment fragment = new DiscoverFragment();
            Bundle args = new Bundle();

            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        /* Make the button listen to onClick-events upon creating the view. */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_discover, container, false);
            answerBtn = (Button) rootView.findViewById(R.id.discover_answer_button);
            final Button rndBtn = (Button) rootView.findViewById(R.id.discover_random_button);
            final Button nextBtn = (Button) rootView.findViewById(R.id.discover_next_button);

            // Load data
            final ConnectionManager cm = ConnectionManager.getInstance(getActivity());

            // TODO This does not work right now
            /*JSONObject params = new JSONObject();
            JSONObject loc = new JSONObject();
            try {
                loc.put("long", "51.0");
                loc.put("lat", "13.0");
                params.put("loc", loc);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/

            // create server request
            final JsonArrayRequest jsObjRequest = new JsonArrayRequest(cm.url
                    + "/screams/rnd/1", new Response.Listener<JSONArray>() {
                // response callback
                @Override
                public void onResponse(JSONArray o) {
                    try {
                        // set the current json
                        currentScreamJson = o.getJSONObject(0);

                        final String from = cm.url + "/screams/" + currentScreamJson.
                                getString("_id") + "/file";
                        final String to = Environment.getExternalStorageDirectory() +
                                "/perapera/" + currentScreamJson.getString("_id") + ".3gp";

                        File f = new File(to);
                        if (f.exists() && !f.isDirectory()) {
                            currentScreamUrl = to;
                            // if user already clicked the button, display automatically
                            if (displayJsonImmediately) {
                                displayJsonImmediately = false;
                                rndBtn.callOnClick();
                            }
                        } else {

                            DownloadFileFromURL dl = new DownloadFileFromURL() {
                                @Override
                                protected void onPostExecute(String s) {
                                    System.out.println("onPostExecute string: " + s);
                                    System.out.println(to);
                                    currentScreamUrl = to;

                                    // if user already clicked the button, display
                                    // automatically
                                    if (displayJsonImmediately) {
                                        displayJsonImmediately = false;
                                        rndBtn.callOnClick();
                                    }
                                }
                            };
                            dl.execute(from, to);
                        }
                        //System.out.println("Response: " + currentScreamJson.getString("title"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 2, 1));

            // add request to queue
            cm.addToRequestQueue(jsObjRequest);


            answerBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mediaListen.stopPlaying();
                    if (currentScreamJson != null && currentScreamUrl != null) {
                        try {
                            Intent intent = new Intent(getActivity(), ViewScreamActivity.class);
                            Topic t = new Topic(currentScreamJson.getInt("_id"), currentScreamJson.getString("title"));
                            intent.putExtra("topic", t);
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            nextBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mediaListen.stopPlaying();
                    nextBtn.setEnabled(false);
                    rndBtn.setText("Loading ...");
                    displayJsonImmediately = true; // start playback automatically

                    currentScreamJson = null;
                    currentScreamUrl = null;

                    // get a new scream
                    cm.addToRequestQueue(jsObjRequest);
                }
            });

            rndBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String title = null;

                    // check if we have a json to display
                    if (currentScreamJson != null && currentScreamUrl != null) {
                        // if yes, display it
                        try {
                            title = currentScreamJson.getString("title");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //if (title != null) { // not gonna happen

                        mediaListen.playPause(currentScreamUrl);

                        rndBtn.setText(title);
                        answerBtn.setEnabled(true);
                        nextBtn.setEnabled(true);

                        //myScreamsFragment.refresh(); TODO do we need this?
                        /*} else {
                            // json was faulty, kill it with fire
                            currentScreamJson = null;
                        }*/
                    } else { // if we dont have a json yet display it automatically when it arrives
                        displayJsonImmediately = true;
                        rndBtn.setText("Loading ...");
                    }
                }
            });

            return rootView;
        }

        @Override
        public void onPause() {
            mediaListen.stopPlaying();
            super.onPause();
        }

        // we need this shit because there is apparently no other way to get an event for when a fragment changes
        // http://stackoverflow.com/questions/9779397/detect-viewpager-tab-change-inside-fragment
        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);

            // Make sure that we are currently visible
            if (this.isVisible()) {
                // If we are becoming invisible, then...
                if (!isVisibleToUser) {
                    mediaListen.stopPlaying();
                }
            }
        }
    }

    /**
     * A fragment where it is possible to search for topics with certain tags.
     */
    public static class SearchFragment extends Fragment {
        // The fragment argument representing the section number for this fragment.
        private static final String ARG_SECTION_NUMBER = "section_number";

        /* Virtual constructor. */
        public SearchFragment() {
        }

        /* Returns a new instance of this fragment for the given section number. */
        public static SearchFragment newInstance(int sectionNumber) {
            SearchFragment fragment = new SearchFragment();
            Bundle args = new Bundle();

            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);

            return fragment;
        }

        /* Just inflate upon creating the view. */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_search, container, false);
        }
    }

    public static class PostJsonArrayRequest extends JsonRequest<JSONArray> {
        /**
         * Creates a new request.
         *
         * @param url           URL to fetch the JSON from
         * @param listener      Listener to receive the JSON response
         * @param errorListener Error listener, or null to ignore errors.
         */
        public PostJsonArrayRequest(String url, Response.Listener<JSONArray> listener,
                                    Response.ErrorListener errorListener) {
            super(Method.POST, url, null, listener, errorListener);
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            HashMap<String, String> params = new HashMap<>();

            params.put("long", "1.0");
            params.put("lat", "1.0");

            return params;
        }

        @Override
        protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString =
                        new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                return Response.success(new JSONArray(jsonString),
                        HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }
    }

    /**
     * A FragmentPagerAdapter that returns a fragment corresponding to one of the
     * tabs.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        /* Constructor */
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /* Return correct fragment according to position of the current view. */
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return DiscoverFragment.newInstance(position + 1);
                case 1:
                    return SearchFragment.newInstance(position + 1);
            }

            return DiscoverFragment.newInstance(position + 1);
        }

        /* Returns the total amount of pages */
        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        /* Return the names of the fragments */
        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();

            switch (position) {
                case 0:
                    return getString(R.string.title_activity_discover).toUpperCase(l);
                case 1:
                    return getString(R.string.discover_section2).toUpperCase(l);
            }

            return null;
        }
    }
}
