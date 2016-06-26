package com.a6v.pagingadapter.rx;

import android.view.View;

import com.a6v.pagingadapter.PagingAdapter;

import rx.Observable;
import rx.Subscriber;
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
        adapter.setOnLoadListener(new PagingAdapter.OnLoadListener() {
          @Override
          public void onLoadPage() {
            if (!subscriber.isUnsubscribed()) {
              subscriber.onNext(null);
            }
          }
        });
        subscriber.add(Subscriptions.create(new Action0() {
          @Override
          public void call() {
            adapter.removeOnLoadListener();
          }
        }));
      }
    });
  }

  public static Observable<Void> messageClicks(final PagingAdapter<?> adapter) {
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
        subscriber.add(Subscriptions.create(new Action0() {
          @Override
          public void call() {
            adapter.removeMessageClickListener();
          }
        }));
      }
    });
  }
}
