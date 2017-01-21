package com.a6v.pagingadapter;

import android.support.v7.widget.RecyclerView;

public class AdapterDataObserverWrapper extends RecyclerView.AdapterDataObserver {
  private final RecyclerView.Adapter<?> adapter;

  public AdapterDataObserverWrapper(RecyclerView.Adapter<?> adapter) {
    this.adapter = adapter;
  }

  @Override
  public void onChanged() {
    adapter.notifyDataSetChanged();
  }

  @Override
  public void onItemRangeChanged(int positionStart, int itemCount) {
    adapter.notifyItemRangeChanged(positionStart, itemCount);
  }

  @Override
  public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
    adapter.notifyItemRangeChanged(positionStart, itemCount, payload);
  }

  @Override
  public void onItemRangeInserted(int positionStart, int itemCount) {
    adapter.notifyItemRangeInserted(positionStart, itemCount);
  }

  @Override
  public void onItemRangeRemoved(int positionStart, int itemCount) {
    adapter.notifyItemRangeRemoved(positionStart, itemCount);
  }

  @Override
  public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
    adapter.notifyDataSetChanged();//TODO better solution for items moved
  }
}
