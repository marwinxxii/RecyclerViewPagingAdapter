package com.a6v.pagingadapter.rx;

import com.a6v.pagingadapter.PagingAdapter;
import com.a6v.pagingadapter.rx.internal.Operators;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public final class RxPager {
  private RxPager() {
    throw new AssertionError("No instances.");
  }

  public static Observable<Integer> pagesWithRefresh(final PagingAdapter adapter,
    final int pageCount, Observable<Integer> reloadPages)
  {
    return pagesWithRefresh(
      RxPagingAdapter.progressShown(adapter),
      RxPagingAdapter.messageClicks(adapter),
      reloadPages,
      pageCount
    );
  }

  public static <T> Observable<Integer> pagesWithRefresh(
    /*Observable<?> can't be used because of startWith*/
    final Observable<T> loadPageRequests,/*loadNextPage*/
    final Observable<?> repeatLatestPageRequests,/*reload current page*/
    final Observable<Integer> restartFromPageRequests,/*refresh whole list*/
    final int pageCount
  )
  {
    return restartFromPageRequests.switchMap(new Func1<Integer, Observable<Integer>>() {
      @Override
      public Observable<Integer> call(Integer startPage) {
        return pages(loadPageRequests, repeatLatestPageRequests, startPage, pageCount);
      }
    });
  }

  public static Observable<Integer> pages(PagingAdapter adapter, int startPage, int pageCount) {
    return pages(adapter, startPage, pageCount, true);
  }

  public static Observable<Integer> pages(PagingAdapter adapter, int startPage, int pageCount,
    boolean showProgressOnMessageClick)
  {
    Observable<Void> messageClicks = RxPagingAdapter.messageClicks(adapter);
    if (showProgressOnMessageClick) {
      messageClicks = messageClicks.doOnNext(new ShowProgressAction<Void>(adapter));
    }
    return pages(
      RxPagingAdapter.progressShown(adapter),
      messageClicks,
      startPage,
      pageCount
    );
  }

  public static <T, R> Observable<Integer> pages(
    Observable<T> loadPageRequests,/*loadNextPage*/
    Observable<R> repeatLatestPageRequests,/*reload current page*/
    int startPage, int pageCount
  )
  {
    return pageNumbers(loadPageRequests, startPage, pageCount)
      .compose(Operators.<Integer, R>repeatLatestWhen(repeatLatestPageRequests));
  }

  public static Observable<PageEvent> pageEvents(PagingAdapter adapter,
    int startPage, int pageCount)
  {
    return pageEvents(adapter, startPage, pageCount, true);
  }

  public static Observable<PageEvent> pageEvents(PagingAdapter adapter,
    int startPage, int pageCount, boolean showProgressOnMessageClick)
  {
    Observable<Void> messageClicks = RxPagingAdapter.messageClicks(adapter);
    if (showProgressOnMessageClick) {
      messageClicks = messageClicks.doOnNext(new ShowProgressAction<Void>(adapter));
    }
    return pageEvents(
      RxPagingAdapter.progressShown(adapter),
      messageClicks,
      startPage,
      pageCount
    );
  }

  public static <T> Observable<PageEvent> pageEvents(
    Observable<T> loadPageRequests,/*loadNextPage*/
    final Observable<?> repeatLatestPageRequests,/*reload current page*/
    int startPage,
    int pageCount
  )
  {
    return pageNumbers(loadPageRequests, startPage, pageCount)
      .map(new Func1<Integer, PageEvent>() {
        @Override
        public PageEvent call(Integer page) {
          return PageEvent.load(page);
        }
      })
      .switchMap(new Func1<PageEvent, Observable<PageEvent>>() {
        @Override
        public Observable<PageEvent> call(final PageEvent pageEvent) {
          final PageEvent reloadEvent = PageEvent.reload(pageEvent.getPage());
          return repeatLatestPageRequests.map(new Func1<Object, PageEvent>() {
            @Override
            public PageEvent call(Object o) {
              return reloadEvent;
            }
          }).startWith(pageEvent);
        }
      });
  }

  public static Observable<PageEvent> pageEventsWithRefresh(PagingAdapter adapter,
    int pageCount, Observable<Integer> reloadPages)
  {
    return pageEventsWithRefresh(
      RxPagingAdapter.progressShown(adapter),
      RxPagingAdapter.messageClicks(adapter),
      reloadPages,
      pageCount
    );
  }

  /*Observable<?> can't be used for loadPageRequests because of startWith*/
  public static <T> Observable<PageEvent> pageEventsWithRefresh(
    final Observable<T> loadPageRequests,/*loadNextPage*/
    final Observable<?> repeatLatestPageRequests,/*reload current page*/
    final Observable<Integer> restartFromPageRequests,/*refresh whole list*/
    final int pageCount
  )
  {
    return restartFromPageRequests.switchMap(new Func1<Integer, Observable<PageEvent>>() {
      @Override
      public Observable<PageEvent> call(Integer startPage) {
        return pageEvents(loadPageRequests, repeatLatestPageRequests, startPage, pageCount);
      }
    });
  }

  private static <T> Observable<Integer> pageNumbers(Observable<T> loadPageRequests,
    int startPage, int pageCount)
  {
    return Observable.range(startPage, pageCount)
      .compose(Operators.<Integer>takeOneWhen(loadPageRequests));
  }

  public static class PageEvent {
    private final boolean isReload;
    private final int page;

    private PageEvent(boolean isReload, int page) {
      this.isReload = isReload;
      this.page = page;
    }

    public boolean isReload() {
      return isReload;
    }

    public int getPage() {
      return page;
    }

    public static PageEvent load(int page) {
      return new PageEvent(false, page);
    }

    public static PageEvent reload(int page) {
      return new PageEvent(true, page);
    }
  }

  static class ShowProgressAction<T> implements Action1<T> {
    private final PagingAdapter adapter;

    ShowProgressAction(PagingAdapter adapter) {
      this.adapter = adapter;
    }

    @Override
    public void call(T t) {
      adapter.showProgress();
    }
  }
}
