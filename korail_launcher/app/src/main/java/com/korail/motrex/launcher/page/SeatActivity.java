package com.korail.motrex.launcher.page;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.app.Activity;
import com.korail.motrex.launcher.R;

/**
 * The SeatActivity class is responsible for displaying the seat-related information based on 
 * the incoming intent data and managing the playback of a ringtone or alarm sound. 
 * Depending on the seat (A or B), different views will be made visible, 
 * and a corresponding sound will be played in a loop until the activity is destroyed.
 */
public class SeatActivity extends Activity {

    // TextViews representing seats A and B
    TextView seatA;
    TextView seatB;

    // Uri representing the ringtone or alarm sound to be played
    Uri toneUri;

    // Ringtone object for playing the selected sound
    Ringtone ringtone;

    /**
     * The onCreate method is called when the activity is first created. 
     * It initializes the UI, retrieves the seat information from the intent, and plays the appropriate sound.
     *
     * @param savedInstanceState The saved instance state for restoring activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for the activity
        setContentView(R.layout.activity_seat);

        // Bind the seat TextViews to the corresponding views in the layout
        seatA = findViewById(R.id.seatA);
        seatB = findViewById(R.id.seatB);

        // Get the intent that started this activity
        Intent i = getIntent();

        // Retrieve the seat information passed from MainActivity
        String seat = i.getStringExtra(MainActivity.SEAT);

        // If seat information is available, set up the view visibility and tone accordingly
        if (seat != null) {
            switch (seat) {
                // Case for seat A: Make seat A's view visible and set the alarm sound as the tone
                case "A":
                    seatA.setVisibility(View.VISIBLE);  // Display seat A view
                    toneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);  // Use alarm sound for seat A
                    break;

                // Case for seat B: Make seat B's view visible and set the ringtone sound as the tone
                case "B":
                    seatB.setVisibility(View.VISIBLE);  // Display seat B view
                    toneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);  // Use ringtone sound for seat B
                    break;

                // Default case if seat is neither A nor B: No action needed
                default:
                    break;
            }

            // Create a Ringtone instance to play the selected tone
            ringtone = RingtoneManager.getRingtone(this, toneUri);

            // Ensure the ringtone is set to loop (play repeatedly)
            ringtone.setLooping(true);

            // Play the ringtone in a separate thread to avoid blocking the UI
            new Thread(() -> ringtone.play()).start();
        }
    }

    /**
     * The onDestroy method is called when the activity is about to be destroyed.
     * It stops the ringtone from playing to prevent it from continuing after the activity is closed.
     */
    @Override
    protected void onDestroy() {
        // Stop the ringtone when the activity is destroyed
        ringtone.stop();

        // Call the superclass's onDestroy method to complete the destruction process
        super.onDestroy();
    }
}