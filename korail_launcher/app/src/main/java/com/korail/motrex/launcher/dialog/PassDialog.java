package com.korail.motrex.launcher.dialog;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.korail.motrex.launcher.R;
import com.korail.motrex.launcher.page.AudioTestActivity;
import com.korail.motrex.launcher.page.MainActivity;

/**
 * The {@code PassDialog} class represents a custom password dialog that allows users to input a passcode
 * and perform various actions based on the passcode entered. The dialog is used in the launcher app
 * to provide access to administrative settings, diagnostic tools, and audio testing functionalities.
 * 
 * <p>The dialog is designed to handle multiple predefined passcodes, each triggering a different action
 * within the app. For example, an admin passcode grants access to system settings, while a diagnostic
 * passcode launches a diagnostic application. Invalid passcodes are rejected with a Toast notification.
 * </p>
 * 
 * Created by the Motrex development team for use in Korail's launcher app.
 */
public class PassDialog extends Dialog implements View.OnClickListener {

    // EditText where the user enters the passcode
    private EditText edit_pass;

    /**
     * Constructor for the {@code PassDialog} class.
     * Initializes the dialog's UI components, sets the layout, and sets the dialog properties.
     *
     * @param context The context in which the dialog is being created, typically the parent activity.
     */
    public PassDialog(Context context) {
        super(context);

        // Remove the default title bar from the dialog
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set the custom layout for the password dialog
        setContentView(R.layout.dialog_layout_pass);

        // Set the dialog's background to be transparent
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set up the "OK" button and its click listener
        TextView btn01 = findViewById(R.id.btn01);
        btn01.setOnClickListener(this);

        // Reference to the EditText for password input
        edit_pass = findViewById(R.id.edit_pass);
    }

    /**
     * Shows the password dialog, resets the password field, and brings up the keyboard for user input.
     * 
     * @param title Unused in the current implementation, but can be used for future features.
     * @param id Unused in the current implementation, but can be used for future features.
     */
    public void show(String title, int id) {
        // Clear any previous input in the password field
        edit_pass.setText("");

        // Show the soft keyboard for password input
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(edit_pass, InputMethodManager.SHOW_IMPLICIT);

        // Request focus for the EditText field so the user can start typing immediately
        edit_pass.requestFocus();
        // Display the dialog
        show();
    }

    /**
     * Handles click events for the buttons inside the dialog.
     * This method checks the passcode entered by the user and performs actions based on the code.
     * 
     * @param v The view that triggered the click event.
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();

        // Handle button clicks based on the view's ID
        switch (id) {
            case R.id.btn01: {
				// Check if the entered password matches one of the predefined passcodes
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edit_pass.getWindowToken(), 0);
                Intent intent = getContext().getPackageManager().getLaunchIntentForPackage("com.korail.motrex.setting");
                intent.putExtra(MainActivity.SEAT, MainActivity.seatStr);
                intent.putExtra(MainActivity.DIRECTION, MainActivity.getDirection());
                intent.putExtra(MainActivity.REGISTER, MainActivity.getIsRegister());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  // Add flag to start the activity in a new task

                if ("12345678".equals(edit_pass.getText().toString())) {
                    intent.putExtra("level", "admin");
                    getContext().startActivity(intent);
                } else if ("1111".equals(edit_pass.getText().toString())) {
                    intent.putExtra("level", "user");
                    getContext().startActivity(intent);
                } else if ("7788".equals(edit_pass.getText().toString())) {
					// Diagnostic access: Launch the diagnostic tool
                    intent.putExtra("level", "manager");
                    getContext().startActivity(intent);
                } else {
                    // Invalid passcode: Show an error message to the user
                    Toast.makeText(getContext(), "관리자 비밀번호를 확인하세요", Toast.LENGTH_LONG).show();
                }

                // Dismiss the dialog after handling the button click
                dismiss();
            }
            break;
        }
    }

    @Override
    public void onBackPressed() {
        // Empty implementation to disable the back button functionality for this dialog
    }

}