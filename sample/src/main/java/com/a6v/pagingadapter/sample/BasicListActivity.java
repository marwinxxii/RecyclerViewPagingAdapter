package com.a6v.pagingadapter.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.a6v.pagingadapter.PagingAdapter;
import com.a6v.pagingadapter.rx.RxPager;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.a6v.pagingadapter.sample.Utils.logError;

public class BasicListActivity extends AppCompatActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.widget_list_items);
    RecyclerView view = (RecyclerView) findViewById(R.id.list_items);

    final int pageSize = 10;
    final WebApi webApi = new WebApi();
    final ArrayList<String> items = new ArrayList<>();
    LayoutInflater inflater = getLayoutInflater();
    final StringItemsAdapter itemsAdapter = new StringItemsAdapter(items, inflater);
    final PagingAdapter pagingAdapter = new PagingAdapter.Builder(itemsAdapter, inflater).build();
    view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    view.setAdapter(pagingAdapter);
    RxPager.pages(pagingAdapter, 0, 100)
      .flatMap(new Func1<Integer, Observable<List<String>>>() {
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
                pagingAdapter.showMessage(getString(R.string.list_error));
              }
            })
            .onErrorResumeNext(Observable.<List<String>>empty());
        }
      })
      .subscribe(new Observer<List<String>>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
          logError(e);//should not happen
        }

        @Override
        public void onNext(List<String> strings) {
          //takeUntil can also be used, to complete sequence, when last page was received
          boolean lastPage = strings.size() < pageSize;
          items.addAll(strings);
          if (items.isEmpty()) {
            pagingAdapter.showMessage(getString(R.string.list_empty));
          } else {
            pagingAdapter.hideProgress(!lastPage);
            itemsAdapter.notifyDataSetChanged();
          }
        }
      });
    pagingAdapter.startLoadingPages();
  }
}
