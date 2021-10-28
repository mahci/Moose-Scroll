package at.aau.moose_scroll.controller;

import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.aau.moose_scroll.data.Memo;
import io.reactivex.rxjava3.core.Observable;

import at.aau.moose_scroll.data.Consts.*;

@SuppressWarnings("ALL")
public class Networker {
    private String cName = "Networker--";
    // -------------------------------------------------------------------------------
    private final String DESKTOP_IP = "192.168.2.1";
    private final int DESKTOP_PORT = 8000;
    private final long SUCCESS_VIBRATE_DUR = 500; // ms
    private final long CONN_THREAD_SLEEP_DUR = 2 * 1000; // ms

    private static Networker instance;

    private Socket socket;
    private Observable<Object> incomningObservable; // Observing the incoming mssg.
    private ExecutorService executor;
    private PrintWriter outPW;
    private BufferedReader inBR;
    private Vibrator vibrator;
    private Handler mainThreadHandler;

    // -------------------------------------------------------------------------------

    //-- Runnable for connecting to desktop
    private class ConnectRunnable implements Runnable {
        String TAG = cName + "connectRunnable";

        @Override
        public void run() {
            Log.d(TAG, "Connecting to desktop...");
            while (socket == null) {
                try {
                    socket = new Socket(DESKTOP_IP, DESKTOP_PORT);

                    Log.d(TAG, "Connection successful!");
                    vibrate(SUCCESS_VIBRATE_DUR);

                    // Send a message to MainActivity (for dismissing the dialog)
                    sendToMain(INTS.CLOSE_DLG);

                    // Create buffers
                    inBR = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    outPW = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),true);

                    // Send intro
                    sendMemo(new Memo(STRINGS.INTRO, STRINGS.MOOSE));

                    // Start receiving
                    executor.execute(new InRunnable());

                } catch (ConnectException e) { // Server offline
                    Log.d(TAG, "Server not responding. Trying again in 2 sec.");
                    try {
                        Thread.sleep(CONN_THREAD_SLEEP_DUR);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    //-- Runnable for outgoing messages
    private class OutRunnable implements Runnable {
        String TAG = cName + "OutRunnable";
        String message;

        public OutRunnable(String mssg) {
            message = mssg;
        }

        @Override
        public void run() {
            if (message != null && outPW != null) {
                Log.d(TAG, "Sending message...");
                outPW.println(message);
                outPW.flush();
                Log.d(TAG, message + " sent");
            } else {
                Log.d(TAG, "Problem in sending messages");
            }
        }
    }

    //-- Runnable for incoming messages
    private class InRunnable implements Runnable {
        String TAG = cName + "InRunnable";

        @Override
        public void run() {
            Log.d(TAG, "Reading from server...");
            String mssg;
            while (inBR != null) {
                try {
                    mssg = inBR.readLine();

                    if (mssg != null) { // Connection is lost
                        Log.d(TAG, "Message: " + mssg);
                    } else {
                        connect(); // Reconnect
                        return;
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Problem in reading from server");
                    connect();
                    e.printStackTrace();
                }
            }
        }
    }

    // -------------------------------------------------------------------------------

    /**
     * Get the singletong instance
     * @return instance
     */
    public static Networker get() {
        if (instance == null) instance = new Networker();
        return instance;
    }

    /**
     * Constructor
     */
    private Networker() {
        // Init the ExecuterService for running the threads
        executor = Executors.newCachedThreadPool();
    }

    /**
     * Connect to
     */
    public void connect() {
        String TAG = cName + "connect";

        executor.execute(new ConnectRunnable());
    }

    /**
     * Send a memo to desktop
     * @param memo Memo
     */
    public void sendMemo(Memo memo) {
        executor.execute(new OutRunnable(memo.toString()));
    }

    /**
     * Send a message to MainHanlder (MainActivity)
     * @param code int code to send
     */
    private void sendToMain(int code) {
        if (mainThreadHandler != null) {
            Message mssg = new Message();
            mssg.what = code;
            mainThreadHandler.sendMessage(mssg);
        }
    }

    /**
     * Vibrate for millisec
     * @param millisec time in milliseconds
     */
    private void vibrate(long millisec) {
        if (vibrator != null) vibrator.vibrate(millisec);
    }

    /**
     * Set the main handler (to MainActivity)
     * @param mainHandler Handler to MainActivity
     */
    public void setMainHandler(Handler mainHandler) {
        mainThreadHandler = mainHandler;
    }

    /**
     * Set the vibrator (called from the MainActivity)
     * @param vib Vibrator from system
     */
    public void setVibrator(Vibrator vib) {
        vibrator = vib;
    }



}
