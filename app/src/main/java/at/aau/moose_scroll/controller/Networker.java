package at.aau.moose_scroll.controller;

import static at.aau.moose_scroll.data.Consts.STRINGS.*;
import static at.aau.moose_scroll.data.Consts.INTS.*;

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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.aau.moose_scroll.data.Memo;
import at.aau.moose_scroll.tools.Logs;
import io.reactivex.rxjava3.core.Observable;

@SuppressWarnings("ALL")
public class Networker {
    private String NAME = "Networker/";
    //-------------------------------------------------------------------------------
    private final String DESKTOP_IP = "192.168.2.1";
//    private final String DESKTOP_IP = "192.168.178.34";
    private final int DESKTOP_PORT = 8000;
    private final long SUCCESS_VIBRATE_DUR = 500; // ms
    private final long CONN_THREAD_SLEEP_DUR = 2 * 1000; // ms

    private static Networker instance;

    private Socket mSocket;
    private Observable<Object> mIncomningObservable; // Observing the incoming mssg.
    private ExecutorService mExecutor;
    private PrintWriter mOutPW;
    private BufferedReader mInBR;
    private Vibrator mVibrator;
    private Handler mMainThreadHandler;

    private Memo mKeepAliveMssg = new Memo(CONNECTION, KEEP_ALIVE, "-", "-");

    // -------------------------------------------------------------------------------

    //-- Runnable for connecting to desktop
    private class ConnectRunnable implements Runnable {
        String TAG = NAME + "connectRunnable";

        @Override
        public void run() {
            Log.d(TAG, "Connecting to desktop...");
            while (mSocket == null) {
                try {
                    mSocket = new Socket(DESKTOP_IP, DESKTOP_PORT);

                    Log.d(TAG, "Connection successful!");
                    vibrate(SUCCESS_VIBRATE_DUR);

                    // Start the main activity part
                    Message closeDialogMssg = new Message();
                    closeDialogMssg.what = CLOSE_DLG;
                    mMainThreadHandler.sendMessage(closeDialogMssg);

                    // Create buffers
                    mInBR = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    mOutPW = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(mSocket.getOutputStream())),true);

                    // Send intro
                    sendMemo(new Memo(CONNECTION, INTRO, MOOSE, "-"));

                    // Start Keep Alive timer
                    keepAlive();

                    // Start receiving
                    mExecutor.execute(new InRunnable());

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
        String TAG = NAME + "OutRunnable";
        String message;

        public OutRunnable(String mssg) {
            message = mssg;
        }

        @Override
        public void run() {
            if (message != null && mOutPW != null) {
                mOutPW.println(message);
                mOutPW.flush();
                Log.d(TAG, message + " sent");
            } else {
                Log.d(TAG, "Problem in sending messages");
            }
        }
    }

    //-- Runnable for incoming messages
    private class InRunnable implements Runnable {
        String TAG = NAME + "InRunnable";

        @Override
        public void run() {
            Log.d(TAG, "Reading from server...");
            String mssg;
            while (mInBR != null) {
                try {
                    if ((mssg = mInBR.readLine()) != null) {
                        Log.d(TAG, "Message: " + mssg);
                        Memo memo = Memo.valueOf(mssg);
                        Logs.d(TAG, "Action: " + memo.getAction());
                        switch (memo.getAction()) {
                            case CONFIG: {
                                Actioner.get().config(memo);
                                break;
                            }

                            case LOG: {
                                Logger.get().setLogInfo(memo);

                                // Reset webView position
                                Actioner.get().webViewManualScrollTo(100000, 100000);
                            }
                        }

                    } else { // Connection is lost
                        resetConnection();
                        return;
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Problem in reading from server. Reseting connection...");
                    e.printStackTrace();
                    resetConnection();
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
        mExecutor = Executors.newCachedThreadPool();
    }

    /**
     * Connect to the desktop
     */
    public void connect() {
        String TAG = NAME + "connect";

        mExecutor.execute(new ConnectRunnable());
    }

    /**
     * Reset the connection
     */
    public void resetConnection() {
        mSocket = null;
        mOutPW = null;
        mInBR = null;
        connect();
    }

    /**
     * Start the keep alive Timer
     * to send a KA message to the server every minute
     */
    private void keepAlive() {
        Timer keepAliveTimer = new Timer();
        keepAliveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendMemo(mKeepAliveMssg);
            }
        }, 0, 60 * 1000);
    }

    /**
     * Send a memo to desktop
     * @param memo Memo
     */
    public void sendMemo(Memo memo) {
        final String TAG = NAME + "sendMemmo";
        mExecutor.execute(new OutRunnable(memo.toString()));
    }


    /**
     * Vibrate for millisec
     * @param millisec time in milliseconds
     */
    private void vibrate(long millisec) {
        if (mVibrator != null) mVibrator.vibrate(millisec);
    }

    /**
     * Set the main handler (to MainActivity)
     * @param mainHandler Handler to MainActivity
     */
    public void setMainHandler(Handler mainHandler) {
        mMainThreadHandler = mainHandler;
    }

    /**
     * Set the vibrator (called from the MainActivity)
     * @param vib Vibrator from system
     */
    public void setmVibrator(Vibrator vib) {
        mVibrator = vib;
    }



}
