package com.a6v.pagingadapter;

import android.support.annotation.NonNull;
import android.view.View;

public interface IPagingAdapter {
  void showProgress();

  void hideProgress(boolean enableNextPageLoading);

  void showMessage(CharSequence message);

  void hideMessage(boolean enableNextPageLoading);

  void setProgressShownListener(@NonNull ProgressShownListener progressShownListener);

  void removeProgressShownListener();

  void setMessageClickListener(@NonNull View.OnClickListener messageClickListener);

  void removeMessageClickListener();

  void setShowProgressEnabled(boolean enabled);
}
