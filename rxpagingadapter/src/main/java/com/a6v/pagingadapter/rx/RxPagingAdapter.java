package com.a6v.pagingadapter.rx;

import android.view.View;

import com.a6v.pagingadapter.PagingAdapter;

import rx.Observable;
import rx.Subscriber;
import rx.android.MainThreadSubscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public final class RxPagingAdapter {
  private RxPagingAdapter() {
    throw new AssertionError("No instances");
  }

  public static Observable<Void> loadRequests(final PagingAdapter<?> adapter) {
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

  public static Observable<Void> messageClicks(final PagingAdapter<?> adapter) {
    return Observable.create(new Observable.OnSubscribe<Void>() {
      @Override
      public void call(final Subscriber<? super Void> subscriber) {
        adapter.setMessageClickListener(new PagingAdapter.MessageClickListener() {
          @Override
          public void onMessageClick() {
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
