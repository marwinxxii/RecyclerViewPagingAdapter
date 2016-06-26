package com.a6v.pagingadapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

public class ProgressViewHolder extends RecyclerView.ViewHolder {
  private final ProgressBar progress;

  public ProgressViewHolder(View view) {
    super(view);
    this.progress = (ProgressBar) view.findViewById(R.id.rvpa_list_progress);
  }
}
