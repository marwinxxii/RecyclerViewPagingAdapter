package com.a6v.pagingadapter.rx2;

import android.view.View;

import com.a6v.pagingadapter.IPagingAdapter;
import com.a6v.pagingadapter.ProgressShownListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.MainThreadDisposable;

public final class RxPagingAdapter {
  private RxPagingAdapter() {
    throw new AssertionError("No instances.");
  }

  public static Observable<Object> progressShown(final IPagingAdapter adapter) {
    return Observable.create(new ObservableOnSubscribe<Object>() {
      @Override
      public void subscribe(final ObservableEmitter<Object> emitter) throws Exception {
        adapter.setProgressShownListener(new ProgressShownListener() {
          @Override
          public void onProgressShown() {
            if (!emitter.isDisposed()) {
              emitter.onNext("");
            }
          }
        });
        emitter.setDisposable(new MainThreadDisposable() {
          @Override
          protected void onDispose() {
            adapter.removeProgressShownListener();
          }
        });
      }
    });
  }

  public static Observable<Object> messageClicks(final IPagingAdapter adapter) {
    return Observable.create(new ObservableOnSubscribe<Object>() {
      @Override
      public void subscribe(final ObservableEmitter<Object> emitter) throws Exception {
        adapter.setMessageClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (!emitter.isDisposed()) {
              emitter.onNext("");
            }
          }
        });
        emitter.setDisposable(new MainThreadDisposable() {
          @Override
          protected void onDispose() {
            adapter.removeMessageClickListener();
          }
        });
      }
    });
  }
}
