package at.aau.moose_scroll.controller;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.rxjava3.annotations.SchedulerSupport;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

@SuppressWarnings("ALL")
public class Networker {
    private String cName = "Networker--";
    // -------------------------------------------------------------------------------
    public final String EXPENVI_IP = "192.168.178.34";
    public final int EXPENVI_PORT = 8000;

    private Socket socket;

    private static Networker instance;

    private Observable<Object> incomningObservable; // Observing the incoming mssg.

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
            socket = new Socket(EXPENVI_IP, EXPENVI_PORT);
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
        // Subscribe to the incoming messages on the io thread
        incomningObservable = Observable
                .fromAction(actOnIncoming)
                .subscribeOn(Schedulers.io());
    }

    public void connect() {
        String TAG = cName + "connect";

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> connectFuture = executorService.submit(connectCallable);

        try {
            Log.d(TAG, connectFuture.get());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }




}
