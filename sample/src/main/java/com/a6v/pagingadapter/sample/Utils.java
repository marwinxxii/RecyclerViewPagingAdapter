package com.a6v.pagingadapter.sample;

import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

public final class Utils {
  private Utils() {
    throw new AssertionError("No instances.");
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
