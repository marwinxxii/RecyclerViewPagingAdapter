package com.a6v.pagingadapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class PagingAdapterDecorator extends RecyclerView.Adapter<RecyclerView.ViewHolder>
  implements IPagingAdapter
{
  private final RecyclerView.Adapter adapter;
  private final PagingAdapterDelegate delegate;

  public PagingAdapterDecorator(RecyclerView.Adapter adapter,
    int progressViewType,
    int messageViewType)
  {
    this(adapter, new PagingAdapterDelegate(adapter, progressViewType, messageViewType));
  }

  private PagingAdapterDecorator(RecyclerView.Adapter adapter, PagingAdapterDelegate delegate) {
    this.adapter = adapter;
    adapter.registerAdapterDataObserver(new AdapterDataObserverWrapper(this));
    this.delegate = delegate;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return adapter.createViewHolder(parent, viewType);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    if (!delegate.onBindViewHolder(holder, position)) {
      adapter.onBindViewHolder(holder, position);
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    if (!delegate.onBindViewHolder(holder, position)) {
      adapter.onBindViewHolder(holder, position, payloads);
    }
  }

  @Override
  public int getItemCount() {
    return delegate.getItemCount(adapter.getItemCount());
  }

  @Override
  public int getItemViewType(int position) {
    Integer itemViewType = delegate.getItemViewType(position, adapter.getItemCount());
    if (itemViewType != null) {
      return itemViewType;
    }
    return adapter.getItemViewType(position);
  }

  @Override
  public long getItemId(int position) {
    Long itemId = delegate.getItemId(position, adapter.getItemCount());
    if (itemId != null) {
      return itemId;
    }
    return adapter.getItemId(position);
  }

  @Override
  public void hideProgress(boolean enableNextPageLoading) {
    delegate.hideProgress(enableNextPageLoading);
  }

  @Override
  public void showProgress() {
    delegate.showProgress();
  }

  @Override
  public void hideMessage(boolean enableNextPageLoading) {
    delegate.hideMessage(enableNextPageLoading);
  }

  @Override
  public void showMessage(CharSequence message) {
    delegate.showMessage(message);
  }

  @Override
  public void removeMessageClickListener() {
    delegate.removeMessageClickListener();
  }

  @Override
  public void setMessageClickListener(@NonNull View.OnClickListener messageClickListener) {
    delegate.setMessageClickListener(messageClickListener);
  }

  @Override
  public void removeProgressShownListener() {
    delegate.removeProgressShownListener();
  }

  @Override
  public void setProgressShownListener(@NonNull ProgressShownListener progressShownListener) {
    delegate.setProgressShownListener(progressShownListener);
  }

  @Override
  public void setShowProgressEnabled(boolean enabled) {
    delegate.setShowProgressEnabled(enabled);
  }

  public static PagingAdapterDecorator withStableIds(RecyclerView.Adapter adapter,
    int progressViewType,
    long progressItemId,
    int messageViewType,
    long messageItemId)
  {
    adapter.setHasStableIds(true);
    return new PagingAdapterDecorator(adapter,
      new PagingAdapterDelegate(
        adapter, progressViewType, progressItemId, messageViewType, messageItemId)
    );
  }
}
