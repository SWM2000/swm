package com.korail.motrex.launcher.model;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Parcelable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.extractor.mp3.Mp3Extractor;
import com.korail.motrex.launcher.R;
import com.korail.motrex.launcher.page.ExoPlayerActivity;
import com.motrex.ktx.mqtt.MovieInfo;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

/**
 * The {@code ContentsAdapter} class is responsible for creating and managing the views that display movie content
 * in a {@link RecyclerView}. Each item in the RecyclerView represents a movie, with details such as title, genre, play time,
 * director, actors, and a poster image. When a user clicks on a movie, the app navigates to the {@link ExoPlayerActivity} 
 * to play the selected movie.
 */
public class ContentsAdapter extends RecyclerView.Adapter<ContentsAdapter.ViewHolder> {

    // Tag for logging purposes
    private static final String TAG = ContentsAdapter.class.getSimpleName();

    // The list of movie data to be displayed in the RecyclerView
    private ArrayList<MovieInfo> localDataSet;

    /**
     * Inner class ViewHolder representing each individual movie item view in the RecyclerView.
     * It holds references to the movie's text and image views, as well as event handlers for clicking the item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        
        // URL for the movie video to be played
        private String movieUrl = "";

        // URL for the movie's poster image
        private String posterUrl = "";

        // TextViews for displaying various movie details
        private TextView textKorTitle;
        private TextView textEngTitle;
        private TextView textGenre;
        private TextView textPlayTime;
        private TextView textGrade;
        private TextView textActors;
        private TextView textDirector;
        private TextView textStory;

        // ImageView for displaying the movie poster
        private ImageView imageView;

        // Reference to the root view of the item layout
        private View view;

        /**
         * Constructor for the ViewHolder. Initializes the UI components of the item view.
         * Also sets an OnClickListener that navigates to {@link ExoPlayerActivity} when the item is clicked.
         *
         * @param itemView The view for the individual movie item
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            // Set click listener for playing the movie when clicked
            itemView.setOnClickListener(v -> {
                int pos = getAbsoluteAdapterPosition();
                Log.d(TAG, "Element " + pos + " , getBindingAdapterPosition() : " + getBindingAdapterPosition());
                Log.d(TAG, "url : " + movieUrl + ", poster : " + posterUrl);

                // Create an intent to start ExoPlayerActivity and pass the movie URL
                Intent intent = new Intent(v.getContext(), ExoPlayerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("videoURL", movieUrl);
                v.getContext().startActivity(intent);  // Start the ExoPlayerActivity to play the movie
            });

            // Bind the text and image views to their corresponding layout elements
            textKorTitle = itemView.findViewById(R.id.textKorTitle);
            textEngTitle = itemView.findViewById(R.id.textEngTitle);
            textGenre = itemView.findViewById(R.id.textGenre);
            textPlayTime = itemView.findViewById(R.id.textPlayTime);
            textGrade = itemView.findViewById(R.id.textGrade);
            textActors = itemView.findViewById(R.id.textActors);
            textDirector = itemView.findViewById(R.id.textDirector);
            textStory = itemView.findViewById(R.id.textStory);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    /**
     * Constructor for the adapter, which initializes the movie data set.
     *
     * @param dataSet The list of movies to be displayed
     */
    public ContentsAdapter (ArrayList<MovieInfo> dataSet) {
        localDataSet = dataSet;  // Store the list of movie data
    }

    /**
     * Called when the RecyclerView needs a new {@link ViewHolder} to represent an item.
     * Inflates the layout for the individual movie items and returns a new ViewHolder.
     *
     * @param parent The parent view group into which the new view will be added
     * @param viewType The view type of the new view
     * @return A new ViewHolder representing the movie item
     */
    @NonNull
    @Override
    public ContentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the movie item layout and return a new ViewHolder instance
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contents_item, parent, false);

        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method binds the data to the view.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position
     * @param position The position of the item within the adapter's data set
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ContentsAdapter.ViewHolder holder, int position) {
        // Set the movie URL and poster URL for the current item
        holder.movieUrl = localDataSet.get(position).getRtspUrl();
        holder.posterUrl = localDataSet.get(position).getImageUrl();

        // Set the Korean title, or "None" if it is empty
        if(!Objects.equals(localDataSet.get(position).getTitleKor(), "")) {
            holder.textKorTitle.setText(localDataSet.get(position).getTitleKor());
        } else {
            holder.textKorTitle.setText("None");
        }
        holder.textKorTitle.setText(localDataSet.get(position).getTitleKor());
		// Set the English title in parentheses, or leave it blank if it is empty
        if(!Objects.equals(localDataSet.get(position).getTitleEng(), "")) {
            holder.textEngTitle.setText("(" + localDataSet.get(position).getTitleEng() + ")");
        } else {
            holder.textEngTitle.setText("");
        }

        // Set the genre, play time, and grade for the movie
        holder.textGenre.setText(localDataSet.get(position).getGenre());
        holder.textGrade.setText(localDataSet.get(position).getGrade());
        holder.textPlayTime.setText(localDataSet.get(position).getPlayTime());
		// Set the director's name, or leave it blank if it is empty
        if(!Objects.equals(localDataSet.get(position).getDirector(), "")) {
            holder.textDirector.setText("감독: " + localDataSet.get(position).getDirector());
        } else {
            holder.textDirector.setText("");
        }

        // Set the actors' names, or leave it blank if it is empty
        if(!Objects.equals(localDataSet.get(position).getActors(), "")) {
            holder.textActors.setText("출연: " + localDataSet.get(position).getActors());
        } else {
            holder.textActors.setText("");
        }

        // Set the movie story/description
        holder.textStory.setText(localDataSet.get(position).getStory());

        // Load the movie's poster image using Glide library
        Glide.with(holder.view).load(holder.posterUrl).into(holder.imageView);
    }

    /**
     * Returns the total number of items in the data set.
     *
     * @return The size of the movie data set
     */
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}