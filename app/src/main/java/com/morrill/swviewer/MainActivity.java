// MainActivity.java
// Displays a list of names for the specified star wars resource topic
package com.morrill.swviewer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // List of Topic objects representing the names of desired star wars topic
    private List<Topic> topicList = new ArrayList<>();
    public MediaPlayer mediaPlayer;
    // ArrayAdapter for binding Topic objects to a ListView
    private TopicArrayAdapter namesArrayAdapter;
    private ListView namesListView; // displays resource names
    private boolean musicPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        // create ArrayAdapter to bind topicList to the topicListView
        namesListView =  (ListView) findViewById(R.id.namesListView);
        namesArrayAdapter = new TopicArrayAdapter(this, topicList);

        namesListView.setAdapter(namesArrayAdapter);

        //play audio file
        playBackgroundMusic(this, R.raw.background_music);

        // configure FAB to hide keyboard and initiate web service request
        FloatingActionButton fab =
                (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get text from locationEditText and create web service URL
                EditText resourceEditText =
                        (EditText) findViewById(R.id.resourceEditText);
                URL url = createURL(resourceEditText.getText().toString());
                // hide keyboard and initiate a GetNamesTask to download
                // resource topic data fom swapi.co in a separate thread
                if (url != null) {
                    dismissKeyboard(resourceEditText);
                    GetNamesTask getResourceNamesTask = new GetNamesTask();
                    getResourceNamesTask.execute(url);
                }
                else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.invalid_url, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sw_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                if (musicPaused) {
                    playBackgroundMusic(this, R.raw.background_music);
                    Toast.makeText(this, "Background music ON", Toast.LENGTH_SHORT).show();
                }
                else {
                    stopBackgroundMusic();
                    Toast.makeText(this, "Background music OFF", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.item2:
                Toast.makeText(this, "Item 2 selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.item3:
                Toast.makeText(this, "Item 3 selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.subitem1:
                Toast.makeText(this, "Sub Item 1 selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.subitem2:
                Toast.makeText(this, "Sub Item 2 selected", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBackgroundMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playBackgroundMusic(this, R.raw.background_music);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        killBackgroundMusic();
    }

    public void playBackgroundMusic(final Context context, int rawSound) {
        mediaPlayer = MediaPlayer.create(context, rawSound);
        musicPaused = false;
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
            }
        });
        mediaPlayer.start();
    }

    public void stopBackgroundMusic() {
        musicPaused = true;
        if (mediaPlayer != null)
            mediaPlayer.pause();
    }

    public void killBackgroundMusic() {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // programmatically dismiss keyboard when user touches FAB
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // create swapi.co web service URL using resource topic
    private URL createURL(String resource_topic) {
        String baseURL = getString(R.string.web_service_url);

        try {
            // create URL for specified resource (people, planets, species, starships, vehicles)
            String urlString = baseURL + "/" + URLEncoder.encode(resource_topic, "UTF-8");
            return new URL(urlString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null; // URL was malformed
    }

    // makes the REST web service call to get resource data and
    // saves the data to a local HTML file
    private class GetNamesTask
            extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    }
                    catch (IOException e) {
                        Snackbar.make(findViewById(R.id.coordinatorLayout),
                                R.string.read_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    return new JSONObject(builder.toString());
                }
                else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.connect_error, Snackbar.LENGTH_LONG).show();
                }
            }
            catch (Exception e) {
                Snackbar.make(findViewById(R.id.coordinatorLayout),
                        R.string.connect_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }
            finally {
                connection.disconnect(); // close the HttpURLConnection
            }
            return null;
        }

        // process JSON response and update ListView
        @Override
        protected void onPostExecute(JSONObject topic) {
            convertJSONtoArrayList(topic); // repopulate topicList
            namesArrayAdapter.notifyDataSetChanged();
            namesListView.smoothScrollToPosition(0);
        }
    }

    // create Topic objects from JSONObject containing the resource names
    private void convertJSONtoArrayList(JSONObject topic) {
        topicList.clear(); // clear old resource topic data

        try {
            // get topic's "results" JSONArray
            JSONArray list = topic.getJSONArray("results");

            // convert each element of list to a Topic object
            for (int i = 0; i < list.length(); ++i) {
                JSONObject resource = list.getJSONObject(i); // get one resource topic's data

                // add new Topic object to topicList
                topicList.add(new Topic(resource.getString("name")));
            }
        }
        catch (JSONException e) {

            e.printStackTrace();
        }
    }
}
