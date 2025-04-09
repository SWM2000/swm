package com.korail.motrex.launcher.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.korail.motrex.launcher.BuildConfig;
import com.korail.motrex.launcher.page.MainActivity;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

/**
 * AppUpdateService is a background service responsible for managing the update process of the application.
 * It handles downloading of update files, processing those files, and communicating with FTP servers.
 * The service runs in the background and provides download progress and update checks.
 */
public class AppUpdateService extends Service {
    
    // Variables for storing various version numbers related to different components of the app
    int img = 0;
    int launcher = 0;
    int rtp = 0;
    int setting = 0;
    int youtube = 0;
    int blacklist = 0;
    int etc = 0;
    
    // The name of the file that contains update version information
    private static final String UPDATE_FILE_NAME = "uFileVerInfo.ini";

    /**
     * The onBind method is required for binding a service but isn't used in this context,
     * as this is a background service that doesn't need binding to any particular component.
     *
     * @param intent The intent that was used to bind to this service
     * @return null since binding is not required
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * The onCreate method is called when the service is first created.
     * It initializes version information by accessing shared preferences from another app package
     * and starts a new thread to handle the download and processing of update files.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.d(MainActivity.TAG, "onCreate()");

        try {
            // Create a context for accessing shared preferences from another package (com.korail.motrex.update)
            Context other = createPackageContext("com.korail.motrex.update", 0);
            SharedPreferences pref = other.getSharedPreferences("update", Context.MODE_PRIVATE);

            // Retrieve stored version numbers for various components of the app
            img = pref.getInt("img", -1);
            launcher = pref.getInt("launcher", -1);
            rtp = pref.getInt("rtp", -1);
            setting = pref.getInt("setting", -1);
            youtube = pref.getInt("youtube", -1);
            blacklist = pref.getInt("blacklist", -1);
            etc = pref.getInt("etc", -1);
        } catch (Exception e) {
            // Handle any exceptions related to creating the package context or accessing shared preferences
            Log.d("createPackageContext", "onCreate() - Exception occurred");
        }
        
        // Start a new thread to handle the download of update files
        DownThread mdownThread = new DownThread();
        mdownThread.start();  // Start the download thread
    }

    /**
     * The onStartCommand method is called when the service is explicitly started using startService().
     * This method starts the service in the foreground, showing a notification to indicate that the service is running.
     *
     * @param intent The intent that started the service
     * @param flags Additional flags for controlling how the service is started
     * @param startId The unique identifier for this specific start request
     * @return The return value determines what happens if the system kills the service (default behavior).
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(MainActivity.TAG, "onStartCommand() startId :" + startId);

        // Start the service in the foreground to prevent it from being killed by the system
        startForeground(1, new Notification());

        // Create a notification to indicate the service is running
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("")  // Empty title for this notification
                .setContentText("")   // Empty content
                .build();

        // Display the notification and immediately cancel it (since it's not meant to persist)
        nm.notify(startId, notification);
        nm.cancel(startId);  // Cancel the notification right after showing it

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * The onDestroy method is called when the service is stopped or destroyed.
     * This method logs the destruction of the service.
     */
    @Override
    public void onDestroy() {
        Log.d(MainActivity.TAG, "onDestroy()");  // Log that the service has been destroyed
        super.onDestroy();
    }

    /**
     * DownThread is a subclass of Thread that handles the download and processing of update files.
     * It deletes old update files, initiates new downloads, processes the downloaded data, and connects to an FTP server.
     */
    class DownThread extends Thread {
        @Override
        public void run() {
            // Create a file object for the update file in the external storage directory
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator, UPDATE_FILE_NAME);
            
            // Attempt to delete any existing version of the update file
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                Log.e(MainActivity.TAG, "IOException - Files.delete() - Unable to delete existing update file");
            }

            // Initiate the download of the version info file using PRDownloader
            PRDownloader.download(BuildConfig.FILE_VERSION_INFO_URL, Environment.getExternalStorageDirectory().getAbsolutePath(), UPDATE_FILE_NAME)
                    .build()
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            // If the download completes, open the file and read its contents
                            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator, UPDATE_FILE_NAME);
                            try (FileReader fr = new FileReader(file); BufferedReader buffer = new BufferedReader(fr)) {
                                // Log the first line of the file for debugging
                                Log.e(MainActivity.TAG, "VideoInfo line a : " + buffer.readLine());

                                // Read and log each subsequent line of the file
                                String s;
                                while ((s = buffer.readLine()) != null) {
                                    int i = Integer.parseInt(buffer.readLine());  // Read the next line as an integer version number
                                    Log.e(MainActivity.TAG, s + " VideoInfo  ---- : " + i);  // Log the component and its version number
                                }
                            } catch (FileNotFoundException e) {
                                Log.e(MainActivity.TAG, "FileNotFoundException : " + e.getMessage());  // Log file not found error
                            } catch (IOException e) {
                                Log.e(MainActivity.TAG, "IOException : " + e.getMessage());  // Log general I/O errors
                            }
                        }

                        @Override
                        public void onError(Error error) {
                            Log.d(MainActivity.TAG, "onError: " + error);  // Log any errors that occur during the download
                        }
                    });

            // Create an FTPSClient object to handle secure FTP connections
            FTPSClient mFTP = new FTPSClient();
            try {
                // Connect to the FTP server using credentials defined in the BuildConfig
                mFTP.connect(BuildConfig.FTPS_ADDRESS, 30821);  // Connect to the FTP server
                mFTP.login("test", "pinnet");  // Login using predefined username and password
                mFTP.setFileType(FTP.BINARY_FILE_TYPE);  // Set the transfer type to binary (for binary files)
                mFTP.setBufferSize(1024 * 1024);  // Set the buffer size to 1 MB
                mFTP.enterLocalPassiveMode();  // Enter passive mode for the FTP connection

                // Create a directory on the external storage to store APK files
                String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "apk";
                File dir = new File(sdPath);
                if (!dir.exists()) {
                    boolean result = dir.mkdir();  // Attempt to create the directory

                    if(result) {
                        Log.d(MainActivity.TAG, "mkdir() Success");  // Log the success of directory creation
                    }
                }

                // Attempt to retrieve the update file from the FTP server and save it locally
                boolean isSuccess;
                file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "apk" + File.separator, UPDATE_FILE_NAME);
                try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
                    isSuccess = mFTP.retrieveFile("/korail/uFileVerInfo.ini", bos);  // Retrieve the file from the FTP server
                }

                // If the file retrieval was successful, read and process the file
                if (isSuccess) {
                    String s;
                    try (FileReader fr = new FileReader(file); BufferedReader buffer = new BufferedReader(fr)) {
                        while ((s = buffer.readLine()) != null) {
                            Log.e(MainActivity.TAG, "VideoInfo line : " + s);  // Log each line of the file

                            if (s.equals("[img]")) {
                                int i = Integer.parseInt(buffer.readLine());  // Read the next line as an integer
                                Log.e(MainActivity.TAG, "VideoInfo  ---- : " + i);  // Log the integer value read from the file
                            }
                        }
                    }
                }
                
                // Disconnect from the FTP server once the file has been processed
                mFTP.disconnect();
            } catch (IOException e) {
                Log.e(MainActivity.TAG, Objects.requireNonNull(e.getMessage()));  // Log any I/O exceptions that occur during the FTP connection
            } finally {
                // Ensure the FTP connection is closed, even if an error occurs
                if(mFTP.isConnected()) {
                    try {
                        mFTP.disconnect();  // Attempt to disconnect from the FTP server
                    } catch (IOException e) {
                        Log.e(MainActivity.TAG, "finally " + Objects.requireNonNull(e.getMessage()));  // Log any errors that occur during disconnection
                    }
                }
            }
        }
    }
}