package com.a6v.pagingadapter.sample;

import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;

import com.a6v.pagingadapter.IPagingAdapter;

import java.util.Arrays;
import java.util.List;

public final class ButtonItems {
  private ButtonItems() {
    throw new AssertionError("No instances.");
  }

  public static List<Pair<CharSequence, OnClickListener>> get(final IPagingAdapter pagingAdapter) {
    return Arrays.asList(
      new Pair<CharSequence, OnClickListener>("Show progress", new OnClickListener() {
        @Override
        public void onClick(View v) {
          pagingAdapter.showProgress();
        }
      }),
      new Pair<CharSequence, OnClickListener>("Hide progress", new OnClickListener() {
        @Override
        public void onClick(View v) {
          pagingAdapter.hideProgress(false);
        }
      }),
      new Pair<CharSequence, OnClickListener>("Show message 1", new OnClickListener() {
        @Override
        public void onClick(View v) {
          pagingAdapter.showMessage("First message");
        }
      }),
      new Pair<CharSequence, OnClickListener>("Show message 2", new OnClickListener() {
        @Override
        public void onClick(View v) {
          pagingAdapter.showMessage("Second message");
        }
      }),
      new Pair<CharSequence, OnClickListener>("Hide message", new OnClickListener() {
        @Override
        public void onClick(View v) {
          pagingAdapter.hideMessage(false);
        }
      })
    );
  }
}
