package com.korail.motrex.launcher.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.korail.motrex.launcher.R;

/**
 * The {@code NotiDialog} class represents a custom notification dialog that is shown to the user for a limited amount of time.
 * This dialog is designed to automatically dismiss itself after a certain period or when clicked by the user.
 * The dialog is fullscreen and provides an immersive user experience without any system UI interference.
 */
public class NotiDialog extends Dialog implements View.OnClickListener {

    // Timer variable to control the countdown for automatic dismissal
    private int time_count = 10;

    /**
     * Constructor for creating an instance of {@code NotiDialog}.
     * It sets up the dialog layout, background, and click listeners for user interaction.
     *
     * @param context The context in which the dialog is being created, usually the parent activity.
     */
    public NotiDialog(Context context) {
        super(context);

        // Remove the default title bar from the dialog
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set the custom layout for the notification dialog
        setContentView(R.layout.dialog_layout_noti);

        // Set the background of the dialog to be transparent
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set up the main layout of the notification dialog and define a click listener
        RelativeLayout noti_layout = findViewById(R.id.noti_layout);
        noti_layout.setOnClickListener(this);
    }

    /**
     * Handles click events within the dialog.
     * If the user clicks anywhere within the dialog, it will immediately dismiss itself.
     *
     * @param v The view that triggered the click event.
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
		// Check which view was clicked (in this case, the main layout), and dismiss the dialog
        switch (id) {
            case R.id.noti_layout: {
                dismiss();
            }
            break;
        }
		// Dismiss the dialog regardless of which view was clicked
        dismiss();
    }

    /**
     * Override the {@code dismiss()} method to ensure that the handler is stopped and no further messages are processed.
     * This ensures the dialog does not linger after the countdown or a user interaction.
     */
    @Override
    public void dismiss() {
        super.dismiss();
        // Remove any pending messages in the handler to stop the countdown
        mfinish_Handler.removeMessages(0);
    }

    // Handler to control the countdown timer for auto-dismissal of the dialog
    private Handler mfinish_Handler = new Handler() {
        /**
         * The {@code handleMessage()} method is called every second to decrement the countdown timer.
         * If the countdown reaches zero, the dialog is automatically dismissed.
         *
         * @param msg The message containing information about the countdown timer.
         */
        public void handleMessage(Message msg) {
            // Decrement the countdown timer
            time_count--;

            // If the timer is still running, continue the countdown with a delay of 1 second
            if (time_count > 0) {
                mfinish_Handler.sendMessageDelayed(mfinish_Handler.obtainMessage(0), 1000);
            } else {
                // Once the countdown reaches zero, dismiss the dialog
                dismiss();
            }
        }
    };

    /**
     * This method ensures the dialog remains in fullscreen immersive mode by hiding system UI elements
     * such as the status bar and navigation bar. It is called whenever the window focus changes.
     *
     * @param hasFocus Indicates whether the window currently has focus or not.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // If the window gains focus, enter immersive fullscreen mode
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}