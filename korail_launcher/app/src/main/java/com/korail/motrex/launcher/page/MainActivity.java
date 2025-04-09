package com.korail.motrex.launcher.page;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.LocaleList;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.korail.motrex.launcher.BuildConfig;
import com.korail.motrex.launcher.R;
import com.korail.motrex.launcher.dialog.NotiSoundDialog;
import com.korail.motrex.launcher.dialog.PassDialog;
import com.motrex.ktx.mqtt.IRemoteService;
import com.motrex.ktx.mqtt.IRemoteServiceCallback;
import com.motrex.ktx.mqtt.MovieInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * MainActivity serves as the central hub for managing user interactions, system settings, and external services.
 * It is responsible for interacting with various components such as remote services, displaying time, handling user input, 
 * and controlling external devices like screens. This activity also manages UI updates and interactions, such as changing the locale 
 * or setting the seat number based on network interfaces.
 */
public class MainActivity extends BaseActivity {

    public static final String TAG = "launcher";  // Log tag for debugging purposes
    private static final String TC1 = "TC1";
    private static final String TC2 = "TC2";
    public static final String DIRECTION = "direction";
    private TextView text_date_01;  // TextView for displaying the current date
    private TextView text_date_02;  // TextView for displaying the current time
    private TextView text_call_crew;  // TextView for displaying the call crew status
    private String local_str;  // Stores the current locale of the device (e.g., "ko" for Korean)
    private long back_time;  // Used to track time for managing hidden button interactions
    private int count;  // Tracks the number of times a hidden button is pressed
    private int hidden_state = -1;  // Tracks the current state of hidden button interactions (used for accessing hidden features)
    private PassDialog passDialog;  // Custom dialog used for password-protected actions
    public static String seatStr = "A";  // Stores the seat number for the current user (default is "A")
    public static final String SEAT = "seatStr";  // Key used for saving and retrieving the seat number in SharedPreferences
    public static final String REGISTER = "register";
    private static final String ACTION_CONFIG_TIME_CHANGED = "com.korail.motrex.CONFIG_TIME_CHANGED";
    private static final String ACTION_TOPIC_SUCCESS = "com.korail.motrex.action.TOPIC_SUCCESS";  // Action for successfully subscribing to an MQTT topic
    private static final String ACTION_CALLCREW_ACK = "com.korail.motrex.action.CALLCREW_ACK";  // Action for acknowledging a crew call
    private static final String ACTION_CALLCREW_CANCEL = "com.korail.motrex.action.CALLCREW_CANCEL";  // Action for canceling a crew call
    private static final String ACTION_EHT_IP_RESET = "com.korail.motrex.action.EHT_IP_RESET";
    private static final String ACTION_SET_IP = "com.korail.motrex.SET_IP";
    protected IRemoteService remoteService;  // Interface for communicating with remote services (e.g., controlling external devices)
    ServiceConnection serviceConnection;  // Object responsible for managing the connection to the remote service
    private boolean showSoundDlg = true;  // Flag indicating whether the notification sound dialog should be shown
    private boolean isCallCrew = false;  // Flag indicating whether a crew call is currently active
    private NotiSoundDialog notiSoundDialog;  // Custom dialog for controlling notification sound settings
    private List<MovieInfo> movie;  // List to store movie information retrieved from the remote service
    private String strMovies;  // String representation of movie information
    private static final File screenGpioFile = new File(BuildConfig.SCREEN_GPIO_PATH);  // File path for controlling the screen's GPIO pin
    public static boolean isTurnOn = true;  // Flag indicating whether the screen is turned on or off
    private static String strDirection = TC1;
    private static boolean isRegister = false;
    private static boolean isConfig = false;
    private int retryConnectionCount = 0;
    private int syncTimeCount = 0;
    private static boolean isSyncTime = false;

    private MediaPlayer mediaPlayer;

    /**
     * The onCreate method is called when the activity is first created.
     * It initializes the UI, sets up various system services, and registers broadcast receivers.
     * It also dynamically loads different layouts based on a system property.
     *
     * @param savedInstanceState The saved state of the activity, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        // Dynamically load the appropriate layout based on a system property
        try {
            Method get = Class.forName("android.os.SystemProperties").getMethod("getBoolean", String.class, boolean.class);
            get.setAccessible(true);
            if ((boolean) get.invoke(null, "motrex.show_usb_app", false)) {
                setContentView(R.layout.activity_main);  // Load layout for the main activity
            } else {
                setContentView(R.layout.activity_main2);  // Load alternative layout if the condition is false
            }
        } catch (NullPointerException | ClassNotFoundException | IllegalAccessException |
                 NoSuchMethodException | InvocationTargetException e) {
            Log.e(TAG, "Exception - onCreate() : " + e.getMessage());  // Log any exceptions that occur during the layout setup
        }

        // Initialize the MediaPlayer with the audio resource (audio_test) from the raw folder
        mediaPlayer = MediaPlayer.create(this, R.raw.silence_0_3s);

        // Set MediaPlayer to play audio once
        mediaPlayer.setLooping(false);

        // Set an OnCompletionListener to handle what happens when the audio finishes
        // In this case, no action is required, so the method body is empty
        mediaPlayer.setOnCompletionListener(mp -> {
        });

        // Set the time zone to "Asia/Seoul" using the AlarmManager
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setTimeZone("Asia/Seoul");

        // Initialize the TextViews for displaying the date and call crew status
        text_date_01 = findViewById(R.id.text_date_01);  // Get the TextView for displaying the date
        text_date_02 = findViewById(R.id.text_date_02);  // Get the TextView for displaying the time
        text_call_crew = findViewById(R.id.text_call_crew);  // Get the TextView for displaying the call crew status

        // Get the current locale of the device and start updating the time display
        local_str = getApplicationContext().getResources().getConfiguration().locale.getLanguage();
        timeHandler.sendMessage(timeHandler.obtainMessage(0));  // Start updating the time display

        // Initialize the custom dialogs for password entry and notification sounds
        passDialog = new PassDialog(this);  // Create a new password dialog
        passDialog.setCanceledOnTouchOutside(false);  // Prevent the dialog from being dismissed by touching outside
        notiSoundDialog = new NotiSoundDialog(this);  // Create a new notification sound dialog
        notiSoundDialog.setCanceledOnTouchOutside(false);  // Prevent the notification sound dialog from being dismissed by touch

        // Register broadcast receivers to handle system events such as time changes and crew call actions
        IntentFilter intentFilter = new IntentFilter();  // Create an intent filter to listen for specific actions
        intentFilter.addAction(ACTION_CONFIG_TIME_CHANGED);  // Action for updating the system time
        intentFilter.addAction(ACTION_TOPIC_SUCCESS);  // Action for successfully subscribing to an MQTT topic
        intentFilter.addAction(ACTION_CALLCREW_ACK);  // Action for acknowledging a crew call
        intentFilter.addAction(ACTION_CALLCREW_CANCEL);  // Action for canceling a crew call
        intentFilter.addAction(ACTION_SET_IP);
        registerReceiver(mConfigTimeChangedReceiver, intentFilter);  // Register the broadcast receiver

        // Retrieve the seat number from shared preferences and set it in the UI
        SharedPreferences pref = getSharedPreferences(TAG, Context.MODE_PRIVATE);
        setSeatStr(pref.getString(SEAT, ""));

        // Initialize the connection to the remote service
        initConnection();

        // Ethernet setting message
        connectionHandler.sendMessageDelayed(connectionHandler.obtainMessage(0), 210000);
    }

    // Ethernet connection handler
    private final Handler connectionHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            SharedPreferences pref = getSharedPreferences(TAG, Context.MODE_PRIVATE);
            String ip = pref.getString("ip", "");

            Log.d(TAG, "ip : " + ip + ", isRegister : " + isRegister);
            Log.d(TAG, "ip : " + ip + ", isConfig : " + isConfig);
            if(retryConnectionCount < 5 && !ip.isEmpty()) {
                if(!isRegister || !isConfig) {
                    ip = ip + "/24";
                    Intent intent = new Intent(ACTION_EHT_IP_RESET);
                    intent.putExtra("ip", ip);
                    sendBroadcast(intent);
                    connectionHandler.sendMessageDelayed(connectionHandler.obtainMessage(0), 5000);
                    retryConnectionCount++;
                    Log.d(TAG, "try register(reset Ip count) : " + retryConnectionCount);
                } else {
                    // Add a function for time synchronization after connection is established.
                    if(!isSyncTime) {
                        syncHandler.sendMessageDelayed(syncHandler.obtainMessage(0), 5000);
                    }
                }
            } else {
                Log.d(TAG, "checked connection! isRegister : " + isRegister);
                retryConnectionCount = 0;
            }
        }
    };

    // Time sync handler
    private final Handler syncHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            SharedPreferences pref = getSharedPreferences(TAG, Context.MODE_PRIVATE);
            String ip = pref.getString("ip", "");
            Log.d(TAG, "isSyncTime : " + isSyncTime);
            if(syncTimeCount < 5 && !isSyncTime) {
                ip = ip + "/24";
                Intent intent = new Intent(ACTION_EHT_IP_RESET);
                intent.putExtra("ip", ip);
                sendBroadcast(intent);
                syncHandler.sendMessageDelayed(syncHandler.obtainMessage(0), 5000);
                syncTimeCount++;
                Log.d(TAG, "try sync time : " + syncTimeCount);
            } else {
                Log.d(TAG, "sync success! isSyncTime" + isSyncTime);
                syncTimeCount = 0;
            }
        }
    };

    /**
     * Callback interface for receiving messages from the remote service.
     * This method handles the messages sent from the service and logs the results.
     */
    IRemoteServiceCallback mRemoteCallback = new IRemoteServiceCallback.Stub() {
        @Override
        public void messageCallback(String key, String msg) throws RemoteException {
            Log.d(TAG, "messageCallback : " + key + ", " + msg);  // Log the key and message received from the remote service
            if(key.equals("CONNECTED") && msg.equals("true")) {
                setIsRegister(true);
            }
        }
    };

    /**
     * Initializes the connection to the remote service that controls various external systems.
     * It binds to the service and sets up a callback for managing the connection state.
     */
    void initConnection() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                remoteService = null;  // Clear the reference to the remote service when it is disconnected
                Log.d("IRemote", "Binding - Service disconnected");
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                remoteService = IRemoteService.Stub.asInterface(service);  // Bind to the remote service
                try {
                    if (remoteService != null) {
                        remoteService.registerCallback(mRemoteCallback);
                        int flag = isTurnOn ? 1 : 0;  // Convert the screen state to an integer flag
                        remoteService.setTurnOn(flag);  // Set the screen state (on or off) through the remote service
                        Log.d(TAG, "setTurnOn() : " + flag);  // Log the screen state
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, Objects.requireNonNull(e.getMessage()));  // Log any exceptions that occur when interacting with the service
                }
                Log.d("IRemote", "Binding is done - Service connected");  // Log the successful connection to the remote service
            }
        };

        // Bind to the remote service if it hasn't already been connected
        if (remoteService == null) {
            Intent serviceIntent = new Intent();  // Create an intent to bind to the remote service
            serviceIntent.setPackage("com.motrex.ktx.mqtt");  // Set the package name for the remote service
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);  // Bind to the remote service
        }
    }

    /**
     * Sets the seat number for the current user. This method is static so it can be called from other components.
     *
     * @param value The new seat number to set.
     */
    public static void setSeatStr(String value) {
        seatStr = value;  // Update the seat number for the user
    }

    public static boolean getIsRegister() {
        return isRegister;
    }

    private static void setIsRegister(boolean flag) {
        isRegister = flag;
    }

    private static void setIsConfig(boolean flag) {
        isConfig = flag;
    }

    private static void setIsSyncTime(boolean falg) {
        isSyncTime = falg;
    }

    private static boolean getIsConfig() {
        return isConfig;
    }

    /**
     * BroadcastReceiver for handling various system events such as MQTT topic success, call crew actions, and Ethernet errors.
     * This receiver listens for specific broadcast actions and updates the UI or system state accordingly.
     */
    private final BroadcastReceiver mConfigTimeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Handle different actions based on the intent's action string
            if (Objects.equals(intent.getAction(), ACTION_TOPIC_SUCCESS)) {
                Log.d(TAG, "ACTION_TOPIC_SUCCESS");
                text_call_crew.setVisibility(VISIBLE);  // Show the call crew status in the UI
            } else if (Objects.equals(intent.getAction(), ACTION_CALLCREW_ACK)) {
                Log.d(TAG, "ACTION_CALLCREW_ACK");
                text_call_crew.setVisibility(GONE);  // Hide the call crew status
                isCallCrew = false;  // Reset the flag for call crew
            } else if (Objects.equals(intent.getAction(), ACTION_CALLCREW_CANCEL)) {
                Log.d(TAG, "ACTION_CALLCREW_CANCEL");
                text_call_crew.setVisibility(GONE);  // Hide the call crew status
                isCallCrew = false;  // Reset the flag for call crew
            } else if(Objects.equals(intent.getAction(), ACTION_SET_IP)) {
                Log.d(TAG, "ACTION_SET_IP");
                SharedPreferences pref = getSharedPreferences("launcher", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();  // Get the editor for SharedPreferences
                String strIp = intent.getStringExtra("ip");
                editor.putString("ip", strIp);  // Save the seat number in SharedPreferences
                editor.apply();  // Apply the changes to SharedPreferences
                Log.d(TAG, "SharedPreferences IP : " + strIp);
            } else {
                // If the action is for updating the system time, set the system clock
                if(isConfig) {
                    setIsSyncTime(true);
                    Log.d(TAG, "ACTION_CONFIG_TIME_CHANGED : setIsSyncTime(true)");
                    syncHandler.removeMessages(0);
                }
                setIsConfig(true);
                Log.d(TAG, "ACTION_CONFIG_TIME_CHANGED");
                SystemClock.setCurrentTimeMillis(intent.getLongExtra("time", System.currentTimeMillis()));
                timeHandler.sendMessage(timeHandler.obtainMessage(0));  // Re-trigger the time handler to update the UI
            }
        }
    };

    private void setAmpOn() {
        // Start playing the audio
        Log.d(TAG, "start player");
        mediaPlayer.start();
    }

    /**
     * Handles user interactions with various buttons in the UI. Depending on the button pressed, 
     * the method starts different activities or performs specific actions like making a crew call.
     *
     * @param view The view (button) that was clicked.
     */
    public void btnClick(View view) {
        switch (view.getId()) {
            case R.id.button: {
                // Handle the button click for "button"
            }
            break;
            case R.id.btn_01: {
//                setAmpOn();
                // Launch the RTSP app when btn_01 is clicked
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.korail.motrex.rtsp");  // Get the intent for the RTSP app
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);  // Start the RTSP app
            }
            break;

            // Additional cases handle launching other apps or triggering specific actions
            case R.id.btn_02: {
//                setAmpOn();
                // Launch the web app when btn_02 is clicked
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.korail.motrex.webapp");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);  // Start the web app
            }
            break;
            case R.id.btn_03: {
//                setAmpOn();
                // Launch the train app when btn_03 is clicked
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.twobeone.motrextrain");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);  // Start the train app
            }
            break;
            case R.id.btn_04: {
                // Launch the settings app when btn_04 is clicked
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.korail.motrex.setting");
                startActivity(intent);  // Start the settings app
            }
            break;
            case R.id.btn_05: {
                // Change the locale and set the seat number when btn_05 is clicked
                setLocale();  // Change the system locale
                setSeatNumber();  // Update the seat number display
            }
            break;
            case R.id.btn_06: {
                // Launch the game list app when btn_06 is clicked
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.mobilusauto.app.gamelist");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);  // Start the game list app
            }
            break;
//            case R.id.btn_08: {
//                Intent intent = getPackageManager().getLaunchIntentForPackage("com.ds.usbmusic");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//            }
//            break;
            case R.id.btn_img_02: {
//                try {
//                    if (!isCallCrew) {
//                        isCallCrew = remoteService.callCrew();  // Make a crew call through the remote service
//                        Log.d(TAG, "callCrew()");
//                    } else {
//                        Log.d(TAG, "callCrew() started!");
//                    }
//                } catch (RemoteException e) {
//                    Log.d(TAG, Objects.requireNonNull(e.getMessage()));
//                }
            }
            break;
            case R.id.hidden_btn_01: {  // Handle hidden button interactions
                if (hidden_state == -1) {
                    long aa = System.currentTimeMillis();  // Get the current time in milliseconds
                    if (aa - back_time < 3000) {  // Check if the time difference is less than 3 seconds
                        count++;
                    } else {
                        count = 0;
                        hidden_state = -1;  // Reset the hidden state
                    }
                    back_time = aa;  // Update the back time

                    if (count > 3) {  // Check if the button was pressed more than 3 times

                        hidden_state = 1;  // Set the hidden state to 1
                        count = 0;
                    }
                }
            }
            break;
            case R.id.hidden_btn_02: {
                if (hidden_state == 1) {  // Check if the hidden state is 1
                    long aa = System.currentTimeMillis();  // Get the current time in milliseconds
                    if (aa - back_time < 3000) {  // Check if the time difference is less than 3 seconds
                        count++;
                    } else {
                        count = 0;
                        hidden_state = -1;  // Reset the hidden state
                    }
                    back_time = aa;  // Update the back time

                    if (count > 3) {  // Check if the button was pressed more than 3 times
                        hidden_state = 2;  // Set the hidden state to 2
                        count = 0;
                    }
                }
            }
            break;
            case R.id.hidden_btn_03: {  // Handle hidden button interactions
                if (hidden_state == 2) {  // Check if the hidden state is 2
                    long aa = System.currentTimeMillis();  // Get the current time in milliseconds
                    if (aa - back_time < 3000) {  // Check if the time difference is less than 3 seconds
                        count++;
                    } else {
                        count = 0;
                        hidden_state = -1;  // Reset the hidden state
                    }
                    back_time = aa;  // Update the back time

                    if (count > 3) {  // Check if the button was pressed more than 3 times
                        hidden_state = -1;  // Reset the hidden state
                        count = 0;
                        passDialog.show("", 0);  // Show authentication dialog
                    }
                }
            }
            break;
            case R.id.hidden_btn_04: {  // Handle hidden button interactions
                long aa = System.currentTimeMillis();  // Get the current time in milliseconds
                if (aa - back_time < 3000) {  // Check if the time difference is less than 3 seconds
                    count++;
                } else {
                    count = 0;
                    hidden_state = -1;  // Reset the hidden state
                }
                back_time = aa;  // Update the back time

                if (count > 3) {  // Check if the button was pressed more than 3 times
                    hidden_state = -1;  // Reset the hidden state
                    count = 0;
                    passDialog.show("", 0);  // Show authentication dialog
                }
            }
            break;
//            case R.id.text_welcome:  // Handle the welcome text click
//                try {
//                    Log.d(TAG, "Request movie list!!");
//                    movie = remoteService.getMovieList();  // Get the list of movies from the remote service
//
//                    Intent intent = new Intent(this, ContentsActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    intent.putParcelableArrayListExtra("movies", (ArrayList<? extends Parcelable>) movie);  // Pass the movie list to the ContentsActivity
//                    startActivity(intent);  // Start the ContentsActivity to display the movie list
//                    Log.d(TAG, "movie count : " + movie.size());
//                } catch (RemoteException e) {  // Handle any exceptions that occur when getting the movie list
//                    Log.d(TAG, Objects.requireNonNull(e.getMessage()));
//                }
//                break;
            default:
                break;
        }
    }

    /**
     * Called when the activity is starting and prepares the activity for interaction.
     * Enables the Wi-Fi on the device when the activity starts.
     */
    @Override
    protected void onStart() {
        super.onStart();  // Call the superclass method
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);  // Get the Wi-Fi service
        wifiManager.setWifiEnabled(true);  // Ensure Wi-Fi is enabled when the activity starts
        Log.d(TAG, "onStart()");
    }

    /**
     * Called when the activity is resumed and prepares the activity to interact with the user.
     * Displays the notification sound dialog and updates the seat number in the UI.
     */
    @Override
    protected void onResume() {
        super.onResume();  // Call the superclass method
        setSeatNumber();  // Update the seat number display
        notiSoundDialog.show();  // Show the notification sound dialog
        Log.d(TAG, "onResume()");
    }

    /**
     * Called when the activity is stopped and the user is leaving the activity.
     * It stops any actions related to the notification sound dialog.
     */
    @Override
    protected void onStop() {
        super.onStop();  // Call the superclass method
        Log.d(TAG, "onStop()");
    }

    /**
     * Called when the activity is destroyed. Dismisses any dialogs and performs cleanup.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();  // Call the superclass method
        notiSoundDialog.dismiss();  // Dismiss the notification sound dialog
        Log.d(TAG, "onDestroy()");
        try {
            remoteService.unregisterCallback(mRemoteCallback);
        } catch (RemoteException e) {
            Log.d(TAG, "remoteService.unregisterCallback Exception : " + Objects.requireNonNull(e.getMessage()));
        }
    }

    /**
     * Updates the system locale by switching between English and Korean based on the current locale.
     * Also updates the wallpaper based on the selected language.
     */
    @SuppressLint("ResourceType")
    private void setLocale() {  // Set the locale based on the current language

        // Get the current locale and change it to the opposite language (English or Korean)
        String local_str = getApplicationContext().getResources().getConfiguration().getLocales().get(0).getLanguage();
        WallpaperManager mWallpaperManager = WallpaperManager.getInstance(getApplicationContext());  // Get the wallpaper manager
        String lang = "ko";  // Default to Korean
        if ("ko".equals(local_str)) {  // Check if the current locale is Korean
            lang = "en";  // Switch to English if the current locale is Korean
        }

        // Update the system locale and change the wallpaper accordingly
        LocaleList localeList = new LocaleList(new Locale(lang));
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);  // Get the activity manager

        try {
            if(lang.equals("en")) { mWallpaperManager.setResource(R.drawable.default_wallpaper_en); }  // Set the English wallpaper
            else { mWallpaperManager.setResource(R.drawable.default_wallpaper_ko); }  // Set the Korean wallpaper
            Method method1 = ActivityManager.class.getMethod("setDeviceLocales", LocaleList.class);  // Get the method to set the device locale
            method1.setAccessible(true);  // Allow access to the method
            method1.invoke(activityManager, localeList);  // Apply the new locale settings to the device
        } catch (Exception e) {  // Handle any exceptions that occur when setting the locale
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    /**
     * Override the onBackPressed method to disable the back button functionality.
     * This prevents the user from accidentally navigating away from the main activity.
     */
    @Override
    public void onBackPressed() {
        // Do nothing, disabling the back button functionality
    }

    /**
     * Helper method to parse a string array from the resources and return it as a SparseArray.
     * This method splits the strings based on the '|' delimiter and stores them in the SparseArray.
     *
     * @param stringArrayResourceId The resource ID of the string array.
     * @return A SparseArray containing the parsed strings.
     */
    private SparseArray<String> parseStringArray(int stringArrayResourceId) {
        String[] stringArray = getResources().getStringArray(stringArrayResourceId);
        SparseArray<String> outputArray = new SparseArray<String>(stringArray.length);  // Create a new SparseArray
        for (String entry : stringArray) {  // Iterate over the string array
            String[] splitResult = entry.split("\\|", 2);  // Split the string based on the '|' character
            outputArray.put(Integer.valueOf(splitResult[0]), splitResult[1]);  // Add the key-value pair to the SparseArray
        }
        return outputArray;  // Return the parsed SparseArray
    }

    /**
     * Handler for periodically updating the date and time displayed in the UI. This method formats 
     * the current system time and updates the corresponding TextViews to display it.
     */
    private final Handler timeHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {  // Handle the message from the handler
            try {
                // Get the current date and time
                Date date = new Date(System.currentTimeMillis());
                String formattedDate;  // Strings to store the formatted date and time
                String formattedTime;  // Strings to store the formatted date and time

                // Format the date and time based on the current locale (Korean or English)
                if ("ko".equals(local_str)) {
                    formattedDate = new SimpleDateFormat("MM월 dd일 (E)", Locale.KOREA).format(date);  // Format the date in Korean
                    formattedTime = new SimpleDateFormat("HH:mm", Locale.KOREA).format(date);  // Format the time in Korean
                } else {
                    formattedDate = new SimpleDateFormat("MMM dd EEE", Locale.ENGLISH).format(date);  // Format the date in English
                    formattedTime = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(date);  // Format the time in English
                }

                // Update the TextViews with the formatted date and time
                text_date_01.setText(formattedDate);
                text_date_02.setText(formattedTime);
            } catch (Exception e) {
                Log.d(TAG, "timeHandler : " + e.getMessage());
            }

            // Re-trigger the handler to update the time display after a delay
            timeHandler.sendMessageDelayed(timeHandler.obtainMessage(0), 500);
        }
    };

    /**
     * Called when the activity's window gains or loses focus. 
     * It ensures that the UI remains in immersive full-screen mode when the activity is in focus.
     *
     * @param hasFocus Whether the window is currently focused.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);  // Call the superclass method
        if (hasFocus) {  // Check if the window is in focus
            getWindow().getDecorView().setSystemUiVisibility(  // Set the system UI visibility flags
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE   // Keep the layout stable when the system UI is toggled
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  // Allow the content to be drawn behind the status bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN  // Hide the status bar for full-screen content
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);  // Set the UI to immersive full-screen mode
        }
    }

    public static String getDirection() {
        return strDirection;
    }

    /**
     * Helper method to set the seat number for the user. It determines the correct seat number based on 
     * network interface configurations and stores this information in SharedPreferences.
     */
    private void setSeatNumber() {
        boolean dir = true;  // Default to front-facing seat direction

        try {
            // Retrieve the system property to determine the seat direction (front or rear)
            Method get = Class.forName("android.os.SystemProperties").getMethod("get", String.class, String.class);
            get.setAccessible(true);  // Allow access to the method
            String str = (String) get.invoke(null, "persist.direction", "1");  // Get the system property value

            Log.d(TAG, "get persist.direction : " + str);

            dir = "1".equals(str);  // If "persist.direction" is "1", the direction is front; otherwise, it's rear
        } catch (Exception e) {  // Handle any exceptions that occur when retrieving the system property
            e.getStackTrace();
        }

        // Parse the appropriate seat number based on the network configuration (IP address)
        SparseArray<String> myStringArray;
        if (dir) {  // Check the seat direction
            strDirection = TC1;
            myStringArray = parseStringArray(R.array.ip_list);  // Use the "ip_list" resource for front-facing seats
        } else {
            strDirection = TC2;
            myStringArray = parseStringArray(R.array.re_ip_list);  // Use the "re_ip_list" resource for rear-facing seats
        }
        Log.d(TAG,"strDirection : " + strDirection);

        try {
            // Determine the user's seat number based on the device's IP address
            int index = -1;
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {  // Iterate over network interfaces
                NetworkInterface intf = en.nextElement();
                // Iterate over the IP addresses associated with the network interface
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();  // Get the current IP address
                    // Check if the IP address is not a loopback address and is an IPv4 address
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        // Check if the network interface name starts with "eth" (Ethernet)
                        if (intf.getName().startsWith("eth")) {
                            index = Integer.parseInt(inetAddress.getHostAddress().split("\\.")[3]);  // Extract the seat number from the IP address
                        }
                    }
                }
            }

            // If a valid seat number was found, update the UI and SharedPreferences
            if (index > 0) {
                // Update the UI and shared preferences with the seat number
                ((TextView) findViewById(R.id.text_ip)).setText(myStringArray.get(index));
                // Save the seat number in SharedPreferences
                SharedPreferences pref = getSharedPreferences("launcher", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();  // Get the editor for SharedPreferences
                editor.putString("seat", myStringArray.get(index));  // Save the seat number in SharedPreferences
                editor.apply();  // Apply the changes to SharedPreferences

                Log.d(TAG, "seat number : " + myStringArray.get(index));  // Log the seat number
                // Update the screen state based on the seat number
                String strGpio = myStringArray.get(index).isEmpty() ? "0" : "1";
                isTurnOn = !strGpio.equals("0");  // Update the screen state based on the seat number

                // If the remote service is connected, update the screen state
                try {
                    // Check if the remote service is connected
                    if (remoteService != null) {
                        int flag = isTurnOn ? 1 : 0;  // Convert the screen state to an integer flag
                        remoteService.setTurnOn(flag);  // Send the screen state to the remote service
                        Log.d(TAG, "setTurnOn() : " + flag);  // Log the screen state
                    }
                } catch (RemoteException e) {  // Handle any exceptions that occur when updating the screen state
                    Log.d(TAG, Objects.requireNonNull(e.getMessage()));  // Log any exceptions that occur when updating the screen state
                }

                // Write the seat number to a GPIO file for external control
                try {
                    // Write the seat number to the GPIO file
                    FileOutputStream outputStream = new FileOutputStream(screenGpioFile);
                    outputStream.write(strGpio.getBytes());  // Write the seat number to the GPIO file
                } catch (Exception e) {  // Handle any exceptions that occur when writing to the GPIO file
                    Log.d(TAG, e.toString() + Arrays.toString(e.getStackTrace()));  // Log any exceptions that occur when writing to the GPIO file
                }
            } else {
                // If the seat number could not be determined, retrieve it from SharedPreferences
                SharedPreferences pref = getSharedPreferences("launcher", Context.MODE_PRIVATE);
                String seat = pref.getString("seat", "1A");  // Get the seat number from SharedPreferences
                // Update the UI with the seat number
                ((TextView) findViewById(R.id.text_ip)).setText(seat);
            }
        } catch (Exception e) {  // Handle any exceptions that occur during the seat number determination
            Log.i(TAG, "setSeatNumber() : " + e);  // Log any exceptions that occur during the seat number determination
        }
    }
}