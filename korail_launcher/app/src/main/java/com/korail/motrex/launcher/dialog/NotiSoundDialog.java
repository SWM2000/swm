package com.korail.motrex.launcher.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.korail.motrex.launcher.R;
import com.korail.motrex.launcher.page.MainActivity;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Objects;

/**
 * The {@code NotiSoundDialog} class represents a custom dialog for sound notifications within the launcher app.
 * This dialog provides an interface for showing earphone direction instructions based on the device's IP address
 * and orientation settings. The dialog also handles user interaction and dismisses itself upon a click event.
 * <p>
 * It uses reflection to access system properties like device orientation and identifies the earphone jack's direction
 * (front or back) based on the system's IP address. This dialog is only displayed when the system is in a "turned on" state.
 * </p>
 */
public class NotiSoundDialog extends Dialog implements View.OnClickListener {

    // Tag for logging
    private static final String TAG = "NotiSoundiDialog";

    // Reference to a TextView to display audio-related messages (currently not in use)
    TextView audioText;

    /**
     * Constructor for the {@code NotiSoundDialog} class.
     * It sets up the dialog's layout and defines basic properties such as the background.
     *
     * @param context The context in which the dialog is being created, typically the parent activity.
     */
    public NotiSoundDialog(Context context) {
        super(context);

        // Remove the default title bar from the dialog
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set the custom layout for the notification dialog
        setContentView(R.layout.dialog_layout_sound_noti);

        // Set the dialog's background to be transparent
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set up the layout for the notification sound and define a click listener for it
        RelativeLayout notiLayout = findViewById(R.id.noti_sound_layout);
        notiLayout.setOnClickListener(this);
    }

    /**
     * Overriding the {@code onStop()} method to add logging when the dialog is stopped or closed.
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "NotiSoundiDialog : onStop()");
    }

    /**
     * Overriding the {@code onStart()} method to initialize UI elements and display the appropriate earphone image
     * based on the device's current IP address and orientation settings.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Only display the content if the system is in the "turned on" state
        if (MainActivity.isTurnOn) {
            Log.d(TAG, "NotiSoundiDialog : onStart()");

            // Retrieve the local IP address's last octet to determine the earphone direction
            int ipHost = getLocalIpAddress();
            boolean isLeft = ipHost % 2 == 0;  // Determines whether the earphone direction is left or right
            Log.d(TAG, "ip : " + ipHost);

            try {
                // Use reflection to access the system property for device orientation
                Method get = Class.forName("android.os.SystemProperties").getMethod("get", String.class, String.class);
                get.setAccessible(true);
                String strDirection = (String) get.invoke(null, "persist.direction", "1");

                // Set up the ImageView to display the correct earphone direction
                ImageView imageView = (ImageView) findViewById(R.id.earphones);

                // Determine the image to display based on the system's orientation and IP address
                if (ipHost < 15) {
                    if("1".equals(strDirection)) {
                        if (isLeft) {
                            imageView.setImageResource(R.drawable.earjack_back);  // Display front earphone image if left
                        } else {
                            imageView.setImageResource(R.drawable.earjack_front);   // Display back earphone image if right
                        }
                    } else if("2".equals(strDirection)) {
                        if (isLeft) {
                            imageView.setImageResource(R.drawable.earjack_front);  // Display front earphone image if left
                        } else {
                            imageView.setImageResource(R.drawable.earjack_back);   // Display back earphone image if right
                        }
                    }
                } else if (ipHost > 140) {
                    if ("1".equals(strDirection)) {
                        if (isLeft) {
                            imageView.setImageResource(R.drawable.earjack_front);  // Display front earphone image if left
                        } else {
                            imageView.setImageResource(R.drawable.earjack_back);   // Display back earphone image if right
                        }
                    } else if ("2".equals(strDirection)) {
                        if (isLeft) {
                            imageView.setImageResource(R.drawable.earjack_back);   // Display back earphone image if left
                        } else {
                            imageView.setImageResource(R.drawable.earjack_front);  // Display front earphone image if right
                        }
                    }
                } else {
                    if ("1".equals(strDirection)) {
                        if (isLeft) {
                            imageView.setImageResource(R.drawable.earjack_front);  // Display front earphone image if left
                        } else {
                            imageView.setImageResource(R.drawable.earjack_back);   // Display back earphone image if right
                        }
                    } else if ("2".equals(strDirection)) {
                        if (isLeft) {
                            imageView.setImageResource(R.drawable.earjack_back);   // Display back earphone image if left
                        } else {
                            imageView.setImageResource(R.drawable.earjack_front);  // Display front earphone image if right
                        }
                    }
                }
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    }

    /**
     * This method retrieves the local IP address of the device by iterating through network interfaces.
     * Specifically, it looks for the "eth0" interface and fetches the last octet of its IP address, which is used to determine
     * the earphone's position (left or right).
     *
     * @return The last octet of the local IP address as an integer, or -1 if it cannot be determined.
     */
    public static int getLocalIpAddress() {
        try {
            // Iterate through all network interfaces
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                Log.d(MainActivity.TAG, "NetworkInterface name : " + intf.getName() + ", NetworkInterface display name : " + intf.getDisplayName());

                // Only check the "eth0" interface for IP address
                if (intf.getName().equals("eth0")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();

                        // Ensure the address is not a loopback address and is an IPv4 address
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            Log.d(MainActivity.TAG, "getLocalIpAddress: " + inetAddress.getHostAddress());

                            // Split the IP address into octets and return the last one
                            String[] temp = Objects.requireNonNull(inetAddress.getHostAddress()).split("[.]");
                            return Integer.parseInt(temp[temp.length - 1]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log any exceptions encountered during the IP address retrieval process
            Log.e(MainActivity.TAG, Objects.requireNonNull(e.getMessage()));
        }
        return -1;  // Return -1 if the IP address cannot be determined
    }

    /**
     * Handles click events within the dialog. Dismisses the dialog when a click is detected.
     *
     * @param v The view that triggered the click event.
     */
    @Override
    public void onClick(View v) {
        dismiss();  // Close the dialog when clicked
    }

    /**
     * Handles window focus changes to maintain the fullscreen immersive mode when the dialog is in focus.
     *
     * @param hasFocus Whether the window has focus or not.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        
        // Ensure that the system stays in fullscreen immersive mode when the window is in focus
        if (MainActivity.isTurnOn && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}