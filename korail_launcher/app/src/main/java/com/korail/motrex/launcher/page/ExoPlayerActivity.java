package com.korail.motrex.launcher.page;

import static com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL;

import android.content.Context;
import android.media.MediaCodec;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.BuildConfig;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.korail.motrex.launcher.R;

/**
 * The ExoPlayerActivity class is responsible for playing video content using ExoPlayer,
 * a media player component provided by Google that supports adaptive playback and various media formats.
 * This class demonstrates how to set up and manage ExoPlayer to stream content over a network, 
 * handle network connections, and manage player lifecycle events.
 */
public class ExoPlayerActivity extends AppCompatActivity {

    // Tag for logging purposes, helping in debugging or tracking events that occur in this activity.
    public static final String TAG = ExoPlayerActivity.class.getSimpleName();

    // A SimpleExoPlayerView is a UI component in the layout that will display the video using ExoPlayer.
    SimpleExoPlayerView exoPlayerView;

    // An instance of ExoPlayer is created to handle media playback, including video and audio.
    SimpleExoPlayer exoPlayer;

    // A default video URL is provided, which will be played unless overridden by the intent's data.
    String videoURL = "http://192.168.0.2:8980/contents/VOD/1032.mp4";  // Default video URL

    /**
     * The onCreate method is called when the activity is first created. 
     * It sets up the UI, initializes the ExoPlayer instance, and starts video playback.
     *
     * @param savedInstanceState The saved instance state for restoring activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the ExoPlayer layout from the activity_exo_player.xml file
        setContentView(R.layout.activity_exo_player);

        // Retrieve the video URL passed via the Intent (if any). If there is no URL passed, it uses the default URL.
        videoURL = getIntent().getStringExtra("videoURL");

        // Bind the ExoPlayerView from the layout to the exoPlayerView object
        exoPlayerView = findViewById(R.id.idExoPlayerVIew);

        // Set up a NetworkRequest to listen for Ethernet connection availability.
        // The activity monitors network connectivity and ensures that the Ethernet connection is used if available.
        NetworkRequest.Builder builder = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET);
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Register a network callback to bind the process to Ethernet when available.
        // This ensures that the app uses the Ethernet network for streaming if available.
        connectivityManager.registerNetworkCallback(builder.build(), new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(@NonNull Network network) {
                // Bind the process to the available network (Ethernet in this case).
                connectivityManager.bindProcessToNetwork(network);
                // Log the network availability and confirm that the process is bound to it.
                Log.d(TAG, "update onAvailable : " + network + ", binded = " + connectivityManager.getBoundNetworkForProcess());
            }
        });

        // Initialize the ExoPlayer and prepare it for video playback.
        // ExoPlayer requires setting up a data source, a media source, and linking it to the player view.
        try {
            // Set up bandwidth metering and track selection for the ExoPlayer.
            // BandwidthMeter helps in adapting the video quality based on network speed.
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));

            // Create an instance of ExoPlayer using the track selector, which decides how to adapt the quality of video playback.
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

            // Parse the video URL into a Uri object that can be passed to the media source.
            Uri videouri = Uri.parse(videoURL);

            // Set up a DataSource factory and ExtractorsFactory to retrieve and process the media source.
            // The DataSourceFactory specifies how to retrieve data from the given URL, and ExtractorsFactory helps interpret the media format.
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

            // Create a MediaSource that represents the media to be played. The ExtractorMediaSource is used for progressive streams (e.g., MP4).
            MediaSource mediaSource = new ExtractorMediaSource(videouri, dataSourceFactory, extractorsFactory, null, null);

            // Set the resize mode for the ExoPlayerView to fill the screen. This ensures the video fits the screen as much as possible.
            exoPlayerView.setResizeMode(RESIZE_MODE_FILL);

            // Attach the ExoPlayer to the ExoPlayerView, linking the player to the UI component.
            exoPlayerView.setPlayer(exoPlayer);

            // Prepare the ExoPlayer with the media source and start playback. 
            // The player is set to start playing the video as soon as it is ready.
            exoPlayer.prepare(mediaSource);
            exoPlayer.setPlayWhenReady(true);  // Start playback as soon as the media is ready
        } catch (Exception e) {
            // Log any exceptions encountered during player initialization to help in troubleshooting.
            Log.e("TAG", "Error : " + e.toString());
        }
    }

    /**
     * The onDestroy method is called when the activity is about to be destroyed.
     * This method is responsible for stopping and releasing the ExoPlayer to free up resources.
     * Releasing the player ensures that system resources like memory and media decoders are properly released.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Log the destruction of the activity for debugging or tracking activity lifecycle.
        Log.d(TAG, "onDestroy()");

        // Stop the ExoPlayer and release its resources to prevent memory leaks and free up media decoders.
        exoPlayer.stop();
        exoPlayer.release();
    }
}
