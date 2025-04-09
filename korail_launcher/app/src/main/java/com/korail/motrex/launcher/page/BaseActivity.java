package com.korail.motrex.launcher.page;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/**
 * The {@code BaseActivity} class is a base class that extends {@link AppCompatActivity}. 
 * It provides common functionality to be inherited by other activities in the application. 
 * In this case, it sets up immersive full-screen mode for its child activities, 
 * ensuring a consistent UI experience across different screens.
 *
 * This activity doesn't contain its own UI elements but focuses on controlling the system's 
 * UI visibility and behavior, such as hiding system bars and enabling immersive mode.
 *
 * Activities that inherit from {@code BaseActivity} will automatically have full-screen mode enabled.
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * The {@code onCreate} method is the first method called when the activity is created.
     * It sets the layout and initializes the user interface to be in full-screen immersive mode.
     * This ensures that the system UI (such as the status and navigation bars) are hidden for a more 
     * immersive experience, typically used for media or gaming applications.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut 
     *                           down, this contains the data it most recently supplied in 
     *                           {@code onSaveInstanceState}. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the window's decor view, which is the top-level view that contains the entire UI of the activity.
        View decoView = getWindow().getDecorView();

        // Define the UI options for immersive full-screen mode. 
        // These flags control how the system bars (status bar and navigation bar) behave.
        // - SYSTEM_UI_FLAG_LAYOUT_STABLE: Keeps the layout stable when the system UI is toggled.
        // - SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN: Allows the content to be drawn behind the status bar.
        // - SYSTEM_UI_FLAG_FULLSCREEN: Hides the status bar for full-screen content.
        // - SYSTEM_UI_FLAG_IMMERSIVE_STICKY: Enables immersive mode where system bars are hidden and 
        //   require a swipe gesture to show temporarily.
        final int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // Apply the UI options to the decor view, setting the activity in full-screen immersive mode.
        decoView.setSystemUiVisibility(uiOptions);
    }
}