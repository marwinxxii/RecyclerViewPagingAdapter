package com.a6v.pagingadapter.rx;

import com.a6v.pagingadapter.PagingAdapter;

import rx.Observable;
import rx.functions.Func1;

public final class RxPager {
  private RxPager() {
    throw new AssertionError("No instances.");
  }

  public static Observable<Integer> pagesWithRefresh(final PagingAdapter<?> adapter,
    final int pageCount, Observable<Integer> reloadPages)
  {
    return pagesWithRefresh(
      RxPagingAdapter.loadRequests(adapter),
      RxPagingAdapter.messageClicks(adapter),
      reloadPages,
      pageCount
    );
  }

  /*Observable<?> can't be used for loadPageRequests because of startWith*/
  public static <T> Observable<Integer> pagesWithRefresh(
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

  public static Observable<Integer> pages(final PagingAdapter<?> adapter,
    final int startPage, final int pageCount)
  {
    return pages(
      RxPagingAdapter.loadRequests(adapter),
      RxPagingAdapter.messageClicks(adapter),
      startPage,
      pageCount
    );
  }

  public static <T, R> Observable<Integer> pages(
    final Observable<T> loadPageRequests,/*loadNextPage*/
    final Observable<R> repeatLatestPageRequests,/*reload current page*/
    final int startPage,
    final int pageCount
  )
  {
    return loadPagesImpl(loadPageRequests, startPage, pageCount)
      .compose(Operators.<Integer, R>repeatLatestWhen(repeatLatestPageRequests));
  }

  public static Observable<PageEvent> pageEvents(final PagingAdapter<?> adapter,
    final int startPage, final int pageCount)
  {
    return pageEvents(
      RxPagingAdapter.loadRequests(adapter),
      RxPagingAdapter.messageClicks(adapter),
      startPage,
      pageCount
    );
  }

  public static <T> Observable<PageEvent> pageEvents(
    final Observable<T> loadPageRequests,/*loadNextPage*/
    final Observable<?> repeatLatestPageRequests,/*reload current page*/
    final int startPage,
    final int pageCount
  )
  {
    return loadPagesImpl(loadPageRequests, startPage, pageCount)
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

  public static Observable<PageEvent> pageEventsWithRefresh(final PagingAdapter<?> adapter,
    final int pageCount, Observable<Integer> reloadPages)
  {
    return pageEventsWithRefresh(
      RxPagingAdapter.loadRequests(adapter),
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

  private static <T> Observable<Integer> loadPagesImpl(Observable<T> loadPageRequests,
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
}
