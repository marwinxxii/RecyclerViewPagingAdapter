package com.a6v.pagingadapter.rx;

import com.a6v.pagingadapter.PagingAdapter;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

public final class RxPager {
  private RxPager() {
    throw new AssertionError("No instances.");
  }

  public static Observable<Integer> observePages(final PagingAdapter<?> adapter,
    final int pageCount, Observable<Integer> reloadPages)
  {
    return observePages(
      RxPagingAdapter.loadRequests(adapter),
      RxPagingAdapter.messageClicks(adapter),
      reloadPages,
      pageCount
    );
  }

  /*Observable<?> can't be used for loadPageRequests because of startWith*/
  public static <T> Observable<Integer> observePages(
    final Observable<T> loadPageRequests,/*loadNextPage*/
    final Observable<?> repeatLatestPageRequests,/*reload current page*/
    Observable<Integer> restartFromPageRequests,/*refresh whole list*/
    final int pageCount
  )
  {
    return restartFromPageRequests.switchMap(new Func1<Integer, Observable<? extends Integer>>() {
      @Override
      public Observable<Integer> call(Integer startPage) {
        return repeatLatestWhen(
          takeOneWhen(
            Observable.range(startPage, pageCount),
            loadPageRequests.startWith((T) null)
          ),
          repeatLatestPageRequests
        );
      }
    });
  }

  /**
   * Operator like sample, but with backpressure support.
   *
   * @param source  where to request items
   * @param trigger sequence which signals when to take items
   * @param <T>     type of item
   * @return TODO
   */
  public static <T> Observable<T> takeOneWhen(Observable<T> source, Observable<?> trigger) {
    return source.zipWith(trigger, new ReturnFirstFunc2<T, Object>());
  }

  public static <T, R> Observable<T> repeatLatestWhen(Observable<T> source, Observable<R> trigger) {
    return Observable.combineLatest(
      source,
      trigger.startWith((R) null),//allow emitting first item
      new ReturnFirstFunc2<T, R>()
    );
  }

  private static class ReturnFirstFunc2<T1, T2> implements Func2<T1, T2, T1> {
    @Override
    public T1 call(T1 first, T2 second) {
      return first;
    }
  }
}
