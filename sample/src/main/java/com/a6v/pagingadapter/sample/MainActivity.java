package com.a6v.pagingadapter.sample;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.a6v.pagingadapter.PagingAdapter;
import com.a6v.pagingadapter.rx.RxPager;
import com.a6v.pagingadapter.rx.RxPager.PageEvent;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    RecyclerView view = (RecyclerView) findViewById(R.id.list_items);
    final SwipeRefreshLayout swipe = (SwipeRefreshLayout) findViewById(R.id.feed_swipe);

    final int pageSize = 10;
    final WebApi webApi = new WebApi();
    final ArrayList<String> items = new ArrayList<>();
    final StringItemsAdapter itemsAdapter = new StringItemsAdapter(items);
    final PagingAdapter<StringItemsAdapter.StringViewHolder> pagingAdapter = new PagingAdapter.Builder<>(itemsAdapter)
      .showProgressOnMessageClick(false)//we have custom progress logic because of swipe
      .build();
    view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    view.setAdapter(pagingAdapter);
    Observable<Integer> swipeToRefresh = RxSwipeRefreshLayout.refreshes(swipe).map(new Func1<Void, Integer>() {
      @Override
      public Integer call(Void aVoid) {
        return 0;
      }
    });
    Observable<Integer> reloads = savedInstanceState == null ? swipeToRefresh.startWith(0) : swipeToRefresh;
    RxPager.pageEventsWithRefresh(pagingAdapter, 100, reloads)
      .doOnNext(new Action1<PageEvent>() {
        @Override
        public void call(PageEvent pageEvent) {
          if (pageEvent.getPage() == 0) {
            setIsRefreshingCompat(swipe, true);
            pagingAdapter.setCompleted(false);//do not show loader in list when refresh is shown
          } else {
            if (pageEvent.isReload()) {
              pagingAdapter.showProgress();//show progress when error clicked and page is reloaded
            }
          }
        }
      })
      .map(new Func1<PageEvent, Integer>() {
        @Override
        public Integer call(PageEvent pageEvent) {
          return pageEvent.getPage();
        }
      })
      .switchMap(new Func1<Integer, Observable<List<String>>>() {
        @Override
        public Observable<List<String>> call(final Integer page) {
          return webApi.loadPage(page, pageSize)
            .retry(2)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(new Action1<List<String>>() {
              @Override
              public void call(List<String> strings) {
                if (page == 0) {
                  items.clear();
                }
              }
            })
            .doOnError(new Action1<Throwable>() {
              @Override
              public void call(Throwable throwable) {
                logError(throwable);
                pagingAdapter.showMessage("Error. Tap to reload");
                setIsRefreshingCompat(swipe, false);
              }
            })
            .onErrorResumeNext(Observable.<List<String>>empty());
        }
      })
      .takeUntil(new Func1<List<String>, Boolean>() {
        @Override
        public Boolean call(List<String> strings) {
          return strings.size() < pageSize;//stop when last page is found
        }
      })
      .subscribe(new Observer<List<String>>() {
        @Override
        public void onCompleted() {
          pagingAdapter.setCompleted(false);//prevent any further loading
        }

        @Override
        public void onError(Throwable e) {
          logError(e);//should not happen
        }

        @Override
        public void onNext(List<String> strings) {
          items.addAll(strings);
          if (items.isEmpty()) {
            pagingAdapter.showMessage("Empty");
          } else {
            pagingAdapter.setCompleted(true);
            itemsAdapter.notifyDataSetChanged();
          }
          setIsRefreshingCompat(swipe, false);
        }
      });
  }

  public static void setIsRefreshingCompat(final SwipeRefreshLayout swipe, final boolean isRefreshing) {
    swipe.post(new Runnable() {
      @Override
      public void run() {
        swipe.setRefreshing(isRefreshing);
      }
    });
  }

  public static void logError(Throwable error) {
    Log.d("PagingSample", "error", error);
  }
}
