package com.a6v.pagingadapter.rx.internal;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

public final class Operators {
  private Operators() {
    throw new AssertionError("No instances.");
  }

  static final Func2 returnFirstFunc2 = new Func2() {
    @Override
    public Object call(Object first, Object second) {
      return first;
    }
  };

  /**
   * Operator like sample, but with backpressure support.
   *
   * @param trigger sequence which signals when to take items
   * @param <T>     type of item
   * @return TODO
   */
  public static <T> Observable.Transformer<T, T> takeOneWhen(final Observable<?> trigger) {
    return new Observable.Transformer<T, T>() {
      @Override
      public Observable<T> call(Observable<T> source) {
        return Observable.zip(source, trigger, (Func2<T, Object, T>) returnFirstFunc2);
      }
    };
  }

  public static <T, R> Observable.Transformer<T, T> repeatLatestWhen(final Observable<R> trigger) {
    return new Observable.Transformer<T, T>() {
      @Override
      public Observable<T> call(Observable<T> source) {
        return Observable.combineLatest(
          source,
          trigger.startWith((R) null),//allow emitting first item
          (Func2<T, R, T>) returnFirstFunc2
        );
      }
    };
  }

  public static <T, R> Observable.Transformer<T, T> restartWhen(final Observable<R> restarts) {
    return new Observable.Transformer<T, T>() {
      @Override
      public Observable<T> call(final Observable<T> original) {
        return restarts
          .startWith((R) null)
          .switchMap(new Func1<R, Observable<? extends T>>() {
            @Override
            public Observable<? extends T> call(R r) {
              return original;
            }
          });
      }
    };
  }
}
