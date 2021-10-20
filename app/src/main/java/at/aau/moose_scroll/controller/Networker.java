package at.aau.moose_scroll.controller;

import android.os.Vibrator;
import android.util.Log;

import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;

@SuppressWarnings("ALL")
public class Networker {
    private String cName = "Networker--";
    // -------------------------------------------------------------------------------
    private final String DESKTOP_IP = "143.205.114.167";
    private final int DESKTOP_PORT = 8000;
    private final long SUCCESS_VIBRATE_DUR = 500; // In milliseconds

    private static Networker instance;

    private Socket socket;
    private Observable<Object> incomningObservable; // Observing the incoming mssg.
    private ExecutorService executorService;

    private Vibrator vibrator;

    // -------------------------------------------------------------------------------

    // Action for incoming messages
    private Action actOnIncoming = new Action() {
        @Override
        public void run() throws Throwable {

        }
    };

    private Callable<String> connectCallable = new Callable<String>() {
        String TAG = cName + "connectCallable";

        @Override
        public String call() throws Exception {
            Log.d(TAG, "Opening socket to Expenvi...");
            socket = new Socket(DESKTOP_IP, DESKTOP_PORT);
            return "SUCCESS";
        }
    };

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
        executorService = Executors.newSingleThreadExecutor();

        // Subscribe to the incoming messages on the io thread
        incomningObservable = Observable
                .fromAction(actOnIncoming)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Connect to
     */
    public void connect() {
        String TAG = cName + "connect";

        try {
            Log.d(TAG, "Connecting to ExpenviScroll...");
            Future<String> connectFuture = executorService.submit(connectCallable);
            String connectResult = connectFuture.get();

            if (connectResult == "SUCCESS") {
                Log.d(TAG, "Connection successful!");
                vibrate(SUCCESS_VIBRATE_DUR);
            }
        } catch (ExecutionException e) {
            Log.d(TAG, "Problem connecting to ExpenviScroll. Trying agian...");
            connect();
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.d(TAG, "Problem connecting to ExpenviScroll. Trying agian...");
            connect();
            e.printStackTrace();
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
     * Set the vibrator (called from the MainActivity)
     * @param vib Vibrator from system
     */
    public void setVibrator(Vibrator vib) {
        vibrator = vib;
    }



}
