package at.aau.moose_scroll.controller;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class Networker {
    private String cName = "Networker--";

    private static Networker instance;

    private Observable<Object> incomningObservable;

    /**
     * Get the singletong instance
     * @return instance
     */
    public static Networker get() {
        if (instance == null) instance = new Networker();
        return instance;
    }


}
