package com.korail.motrex.launcher.page;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.app.Activity;
import com.korail.motrex.launcher.R;

/**
 * The {@code AudioTestActivity} class represents an activity in which users can initiate audio tests by selecting different seat options.
 * It allows the user to choose between two seat options (A and B), which will be stored in shared preferences and passed to the {@link SeatActivity}.
 * 
 * This activity demonstrates the use of buttons to modify shared preferences and trigger a new activity while passing along data.
 */
public class AudioTestActivity extends Activity {

    /**
     * The {@code onCreate} method is called when the activity is first created. It is responsible for initializing the activity.
     * In this case, it sets the content view to the layout defined in {@code activity_audio_test.xml}, which likely contains buttons for seat selection.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this contains the data it most recently supplied in {@code onSaveInstanceState}. 
     *                           Otherwise, it is {@code null}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for the activity, which contains buttons for selecting seat A or seat B.
        setContentView(R.layout.activity_audio_test);
    }

    /**
     * The {@code clickBtn} method is triggered when a button in the activity is clicked.
     * It identifies the clicked button based on its ID and performs actions such as updating shared preferences and starting a new activity.
     * The method uses the Android {@link SharedPreferences} system to store the selected seat (A or B), allowing the choice to persist across activities.
     * 
     * @param v The view (button) that was clicked. Its ID will be used to determine which action to take.
     */
    @SuppressLint("NonConstantResourceId") // Suppress lint warning about non-constant resource IDs in the switch statement
    public void clickBtn(View v) {
        // Get the ID of the clicked button
        int id = v.getId();

        // Create an intent to start the SeatActivity, which will display the selected seat
        Intent intent = new Intent(AudioTestActivity.this, SeatActivity.class);

        // Get the shared preferences for the "launcher" settings
        SharedPreferences pref = getSharedPreferences("launcher", Context.MODE_PRIVATE);
        // Create an editor to modify the shared preferences
        SharedPreferences.Editor editor = pref.edit();

        // Use a switch statement to determine which button was clicked and take action accordingly
        switch (id) {
            case R.id.button1:
                // If button 1 (seat A) was clicked, store "A" as the selected seat in shared preferences and the MainActivity class
                editor.putString(MainActivity.SEAT, "A");
                MainActivity.seatStr = "A";
                // Pass the selected seat to the intent that will launch SeatActivity
                intent.putExtra(MainActivity.SEAT, "A");
                break;
            case R.id.button2:
                // If button 2 (seat B) was clicked, store "B" as the selected seat in shared preferences and the MainActivity class
                editor.putString(MainActivity.SEAT, "B");
                MainActivity.seatStr = "B";
                // Pass the selected seat to the intent that will launch SeatActivity
                intent.putExtra(MainActivity.SEAT, "B");
                break;
            default:
                // Handle any other cases (if needed) in the default block
                break;
        }

        // Apply the changes made to the shared preferences
        editor.apply();

        // Start the SeatActivity, passing along the selected seat information
        this.startActivity(intent);
    }
}