package com.a6v.pagingadapter.sample;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

public class WebApi {
  public Observable<List<String>> loadPage(final int page, final int pageSize) {
    return Observable
      .timer(new Random().nextInt(2000) + 1000L, TimeUnit.MILLISECONDS)
      .flatMap(new Func1<Long, Observable<Integer>>() {
        @Override
        public Observable<Integer> call(Long aLong) {
          if (new Random().nextBoolean()) {
            return Observable.range(page * pageSize, pageSize);
          } else {
            return Observable.error(new RuntimeException());
          }
        }
      })
      .map(new Func1<Integer, String>() {
        @Override
        public String call(Integer item) {
          return "item " + item;
        }
      })
      .toList();
  }
}
