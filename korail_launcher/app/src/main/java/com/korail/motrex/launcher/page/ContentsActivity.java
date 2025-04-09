package com.korail.motrex.launcher.page;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.korail.motrex.launcher.R;
import com.korail.motrex.launcher.model.ContentsAdapter;
import com.motrex.ktx.mqtt.MovieInfo;

import java.util.ArrayList;
import java.util.Objects;

/**
 * The {@code ContentsActivity} class is responsible for displaying a list of movies in a 
 * RecyclerView. It extends {@link AppCompatActivity}, which is a part of the Android Jetpack 
 * architecture components, and it serves as the entry point for the UI related to the list of 
 * movies passed to this activity.
 * 
 * This activity receives a list of movies via an Intent and logs each movie's title in Korean. 
 * The movies are then displayed in a RecyclerView using the {@link ContentsAdapter}, which binds 
 * the movie data to the views in the RecyclerView.
 * 
 * The {@code ContentsActivity} utilizes Android's RecyclerView with a LinearLayoutManager to 
 * efficiently handle large data sets (in this case, a list of movies) by recycling views and 
 * improving performance. The class demonstrates how to set up a RecyclerView, assign a layout 
 * manager, and provide the data to the adapter for display.
 */
public class ContentsActivity extends AppCompatActivity {

    // TAG for logging purposes. This TAG is used to identify log messages from this class.
    public static final String TAG = ContentsActivity.class.getSimpleName();

    /**
     * The {@code onCreate} method is the first method that is called when the activity is created. 
     * It sets up the UI for the activity by inflating the appropriate layout and initializes the 
     * RecyclerView for displaying the list of movies.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being 
     *                           shut down, this contains the data it most recently supplied in 
     *                           {@code onSaveInstanceState}. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view to the layout file "activity_contents", which contains the UI components.
        setContentView(R.layout.activity_contents);

        // Retrieve the list of movies passed from the previous activity through the Intent.
        // The list is passed as an ArrayList of MovieInfo objects.
        ArrayList<MovieInfo> movieList = getIntent().getParcelableArrayListExtra("movies");

        // Log each movie's Korean title to the console for debugging purposes.
        // The loop iterates over the movie list and logs the Korean title of each movie.
        for (int i = 0; i < Objects.requireNonNull(movieList).size(); i++) {
            Log.d(TAG, movieList.get(i).getTitleKor());  // Log the Korean title of each movie.
        }

        // Find the RecyclerView in the layout using its ID.
        RecyclerView recyclerView = findViewById(R.id.contentsRecyclerView);

        // Set up a LinearLayoutManager for the RecyclerView, which arranges the movie items 
        // in a vertical list. LinearLayoutManager is responsible for positioning items in the 
        // RecyclerView in either a vertical or horizontal scrolling list.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);  // Assign the layout manager to the RecyclerView.

        // Create an instance of the ContentsAdapter, passing the movie list to it.
        // The adapter is responsible for creating and binding the view holders in the RecyclerView.
        ContentsAdapter contentsAdapter = new ContentsAdapter(movieList);

        // Set the adapter for the RecyclerView to display the movie items.
        // The adapter binds the movie data to the views in the RecyclerView for display.
        recyclerView.setAdapter(contentsAdapter);
    }
}