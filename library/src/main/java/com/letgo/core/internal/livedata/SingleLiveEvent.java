package com.letgo.core.internal.livedata;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class SingleLiveEvent<T> extends MutableLiveData<T> {
    private final AtomicBoolean mPending = new AtomicBoolean(false);

    private HashMap<Observer<? super T>, Observer<? super T>> observerHashMap =
            new HashMap<>();

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull final Observer<? super T> observer) {
        if (observerHashMap.containsKey(observer)) {
            return;
        }
        Observer<T> delegateObserver = new Observer<T>() {
            @Override
            public void onChanged(@Nullable T t) {
                if (mPending.compareAndSet(true, false)) {
                    observer.onChanged(t);
                }
            }
        };
        observerHashMap.put(observer, delegateObserver);
        super.observe(owner, delegateObserver);
    }

    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        if (observerHashMap.containsKey(observer)) {
            Observer<? super T> delegateObserver = observerHashMap.remove(observer);
            super.removeObserver(delegateObserver);
            return;
        }
        super.removeObserver(observer);
    }

    @MainThread
    public void setValue(@Nullable T t) {
        mPending.set(true);
        super.setValue(t);
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    public void call() {
        setValue(null);
    }
}

