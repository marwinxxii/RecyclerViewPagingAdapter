package com.a6v.pagingadapter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.*;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class PagingAdapter<T extends ViewHolder> extends RecyclerView.Adapter<ViewHolder> {
  public static final int PROGRESS_VIEW_TYPE = 100, MESSAGE_VIEW_TYPE = 200;

  private final Adapter<T> adapter;
  private final int progressLayoutRes;

  private final int progressViewType;
  private final int messageLayoutRes;
  private final int messageViewType;

  private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
  private final Runnable notifyLoadListenerRunnable = new Runnable() {
    @Override
    public void run() {
      if (onLoadListener != null) {
        onLoadListener.onLoadPage();
      }
    }
  };

  @Nullable private OnLoadListener onLoadListener;
  @Nullable private OnClickListener messageClickListener;

  private State state = State.IDLE;
  private LayoutInflater inflater;
  private int itemPosition = -1;
  private int itemCount = 0;
  @Nullable private CharSequence message;

  public PagingAdapter(Builder<T> builder) {
    adapter = builder.adapter;
    adapter.registerAdapterDataObserver(new AdapterDataObserverWrapper<>(this));
    progressLayoutRes = builder.progressLayoutRes;
    progressViewType = builder.progressViewType;
    onLoadListener = builder.onLoadListener;
    message = builder.message;
    messageLayoutRes = builder.messageLayoutRes;
    messageViewType = builder.messageViewType;
    messageClickListener = builder.messageClickListener;
  }

  @Override
  public int getItemCount() {
    int count = 0;
    switch (state) {
      case IDLE:
        count = 0;
        break;
      case READY_TO_LOAD:
      case LOADING:
      case MESSAGE:
        count = 1;
    }
    itemCount = adapter.getItemCount() + count;
    return itemCount;
  }

  @Override
  public int getItemViewType(int position) {
    if (position == itemCount - 1) {
      switch (state) {
        case READY_TO_LOAD:
        case LOADING:
          return PROGRESS_VIEW_TYPE;
        case MESSAGE:
          return MESSAGE_VIEW_TYPE;
      }
    }
    return adapter.getItemViewType(position);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (inflater == null) {
      inflater = LayoutInflater.from(parent.getContext());
    }
    if (viewType == progressViewType) {
      return new ProgressViewHolder(inflater.inflate(progressLayoutRes, parent, false));
    } else if (viewType == messageViewType) {
      return new MessageViewHolder(inflater.inflate(messageLayoutRes, parent, false));
    }
    return adapter.onCreateViewHolder(parent, viewType);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    if (holder instanceof ProgressViewHolder) {
      itemPosition = position;
      if (state == State.READY_TO_LOAD) {
        state = State.LOADING;
        mainThreadHandler.post(notifyLoadListenerRunnable);
      }
    } else if (holder instanceof MessageViewHolder) {
      itemPosition = position;
      MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
      messageViewHolder.bind(message, messageClickListener);
    } else {
      adapter.onBindViewHolder((T) holder, position);
    }
  }

  public void setOnLoadListener(@NonNull OnLoadListener onLoadListener) {
    //TODO check not null
    this.onLoadListener = onLoadListener;
  }

  public void removeOnLoadListener() {
    onLoadListener = null;
  }

  public void setMessageClickListener(@NonNull OnClickListener messageClickListener) {
    //TODO check not null
    this.messageClickListener = messageClickListener;
  }

  public void removeMessageClickListener() {
    messageClickListener = null;
  }

  public void showMessage(CharSequence message) {
    this.message = message;
    state = State.MESSAGE;
    notifyPagingItemChanged();
  }

  public void showProgress() {
    state = State.LOADING;
    notifyPagingItemChanged();
  }

  public void setCompleted(boolean enableNextPageLoading) {
    enableNextPageLoading(enableNextPageLoading);
    if (itemPosition >= 0) {
      notifyItemRemoved(itemPosition);
      itemPosition = -1;
    }
  }

  public void enableNextPageLoading(boolean enable) {
    state = enable ? State.READY_TO_LOAD : State.IDLE;
    //notify about change?
  }

  private void notifyPagingItemChanged() {
    if (itemPosition >= 0) {
      notifyItemChanged(itemPosition);
    } else {
      //TODO notify item inserted?
      notifyDataSetChanged();
    }
  }

  public static class Builder<T extends ViewHolder> {
    private final Adapter<T> adapter;
    private int progressLayoutRes = R.layout.rvpa_widget_list_progress;
    private int progressViewType = PROGRESS_VIEW_TYPE;
    private @Nullable OnLoadListener onLoadListener;
    private CharSequence message;
    private int messageLayoutRes = R.layout.rvpa_widget_list_message;
    private int messageViewType = MESSAGE_VIEW_TYPE;
    private @Nullable OnClickListener messageClickListener;

    public Builder(@NonNull Adapter<T> adapter) {
      this.adapter = adapter;
    }

    public Builder setProgressLayoutRes(@LayoutRes int progressLayoutRes) {
      this.progressLayoutRes = progressLayoutRes;
      return this;
    }

    public Builder setProgressViewType(int progressViewType) {
      this.progressViewType = progressViewType;
      return this;
    }

    public Builder setOnLoadListener(@NonNull OnLoadListener onLoadListener) {
      this.onLoadListener = onLoadListener;
      return this;
    }

    public Builder setMessage(CharSequence message) {
      this.message = message;
      return this;
    }

    public Builder setMessageLayoutRes(@LayoutRes int messageLayoutRes) {
      this.messageLayoutRes = messageLayoutRes;
      return this;
    }

    public Builder setMessageViewType(int messageViewType) {
      this.messageViewType = messageViewType;
      return this;
    }

    public Builder setMessageClickListener(@NonNull OnClickListener messageClickListener) {
      this.messageClickListener = messageClickListener;
      return this;
    }

    public PagingAdapter<T> build() {
      return new PagingAdapter<>(this);
    }
  }

  public interface OnLoadListener {
    void onLoadPage();
  }

  private enum State {
    IDLE, READY_TO_LOAD, LOADING, MESSAGE
  }
}
