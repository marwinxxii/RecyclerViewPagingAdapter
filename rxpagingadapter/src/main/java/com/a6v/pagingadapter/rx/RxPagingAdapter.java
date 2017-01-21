package com.a6v.pagingadapter.rx;

import android.view.View;

import com.a6v.pagingadapter.PagingAdapter;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;

public final class RxPagingAdapter {
  private RxPagingAdapter() {
    throw new AssertionError("No instances.");
  }

  public static Observable<Void> progressShown(final PagingAdapter adapter) {
    return Observable.create(new Observable.OnSubscribe<Void>() {
      @Override
      public void call(final Subscriber<? super Void> subscriber) {
        adapter.setProgressShownListener(new PagingAdapter.ProgressShownListener() {
          @Override
          public void onProgressShown() {
            if (!subscriber.isUnsubscribed()) {
              subscriber.onNext(null);
            }
          }
        });
        subscriber.add(new MainThreadSubscription() {
          @Override
          protected void onUnsubscribe() {
            adapter.removeProgressShownListener();
          }
        });
      }
    });
  }

  public static Observable<Void> messageClicks(final PagingAdapter adapter) {
    return Observable.create(new Observable.OnSubscribe<Void>() {
      @Override
      public void call(final Subscriber<? super Void> subscriber) {
        adapter.setMessageClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (!subscriber.isUnsubscribed()) {
              subscriber.onNext(null);
            }
          }
        });
        subscriber.add(new MainThreadSubscription() {
          @Override
          protected void onUnsubscribe() {
            adapter.removeMessageClickListener();
          }
        });
      }
    });
  }
}
