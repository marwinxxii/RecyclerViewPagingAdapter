package com.a6v.pagingadapter;

import android.support.annotation.Nullable;
import android.view.View;

public interface MessageViewHolder {
  void bindMessage(@Nullable CharSequence message, @Nullable View.OnClickListener clickListener);
}
