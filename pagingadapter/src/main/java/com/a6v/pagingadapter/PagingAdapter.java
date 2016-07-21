package com.a6v.pagingadapter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.*;
import android.view.LayoutInflater;
import android.view.View;
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
  private final Runnable notifyProgressShownRunnable = new Runnable() {
    @Override
    public void run() {
      if (progressShownListener != null) {
        progressShownListener.onProgressShown();
      }
    }
  };
  private final Runnable notifyMessageClickRunnable = new Runnable() {
    @Override
    public void run() {
      if (messageClickListener != null) {
        messageClickListener.onMessageClick();
      }
    }
  };
  private final OnClickListener realMessageClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      if (progressShownListener != null) {
        if (showProgressOnMessageClick) {
          showProgress();
        }
        mainThreadHandler.post(notifyMessageClickRunnable);
      }
    }
  };

  @Nullable private ProgressShownListener progressShownListener;
  @Nullable private MessageClickListener messageClickListener;

  private State state = State.IDLE;
  private LayoutInflater inflater;
  private int itemPosition = -1;
  private int itemCount = 0;
  private boolean showProgressOnMessageClick = true;
  @Nullable private CharSequence message;

  public PagingAdapter(Builder<T> builder) {
    adapter = builder.adapter;
    adapter.registerAdapterDataObserver(new AdapterDataObserverWrapper<>(this));
    progressLayoutRes = builder.progressLayoutRes;
    progressViewType = builder.progressViewType;
    progressShownListener = builder.progressShownListener;
    message = builder.message;
    messageLayoutRes = builder.messageLayoutRes;
    messageViewType = builder.messageViewType;
    messageClickListener = builder.messageClickListener;
    showProgressOnMessageClick = builder.showProgressOnMessageClick;
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
        mainThreadHandler.post(notifyProgressShownRunnable);
      }
    } else if (holder instanceof MessageViewHolder) {
      itemPosition = position;
      MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
      messageViewHolder.bind(message, realMessageClickListener);
    } else {
      adapter.onBindViewHolder((T) holder, position);
    }
  }

  public void setProgressShownListener(@NonNull ProgressShownListener progressShownListener) {
    //TODO check not null
    this.progressShownListener = progressShownListener;
  }

  public void removeProgressShownListener() {
    mainThreadHandler.removeCallbacks(notifyProgressShownRunnable);
    progressShownListener = null;
  }

  public void setMessageClickListener(@NonNull MessageClickListener messageClickListener) {
    //TODO check not null
    this.messageClickListener = messageClickListener;
  }

  public void removeMessageClickListener() {
    mainThreadHandler.removeCallbacks(notifyMessageClickRunnable);
    messageClickListener = null;
  }

  public void showMessage(CharSequence message) {
    this.message = message;
    switchState(State.MESSAGE);
  }

  public void showProgress() {
    if (!State.LOADING.equals(state)) {
      switchState(State.LOADING);
    }
  }

  public void setCompleted(boolean enableNextPageLoading) {
    if (State.LOADING.equals(state)) {
      if (enableNextPageLoading) {
        switchState(State.READY_TO_LOAD);
      } else {
        switchState(State.IDLE);
      }
    } else {
      throw new IllegalStateException("Not loading adapter can't be marked as completed");
    }
  }

  public void enableLoadingPages() {
    switch (state) {
      case IDLE:
      case MESSAGE:
        switchState(State.READY_TO_LOAD);
        break;
      case LOADING:
        throw new IllegalStateException("Can't start new load while loading");
      case READY_TO_LOAD:
        //do nothing
        break;
    }
  }

  private void switchState(State state) {
    this.state = state;
    switch (state) {
      case READY_TO_LOAD:
      case IDLE:
        if (itemPosition >= 0) {
          notifyItemRemoved(itemPosition);
          itemPosition = -1;
        }
        break;
      case LOADING:
      case MESSAGE:
        if (itemPosition >= 0) {
          notifyItemChanged(itemPosition);
        } else {
          notifyDataSetChanged();
        }
        break;
    }
  }

  public static class Builder<T extends ViewHolder> {
    private final Adapter<T> adapter;
    private int progressLayoutRes = R.layout.rvpa_widget_list_progress;
    private int progressViewType = PROGRESS_VIEW_TYPE;
    private @Nullable ProgressShownListener progressShownListener;
    private CharSequence message;
    private int messageLayoutRes = R.layout.rvpa_widget_list_message;
    private int messageViewType = MESSAGE_VIEW_TYPE;
    private @Nullable MessageClickListener messageClickListener;
    private boolean showProgressOnMessageClick = true;

    public Builder(@NonNull Adapter<T> adapter) {
      this.adapter = adapter;
    }

    public Builder<T> setProgressLayoutRes(@LayoutRes int progressLayoutRes) {
      this.progressLayoutRes = progressLayoutRes;
      return this;
    }

    public Builder<T> setProgressViewType(int progressViewType) {
      this.progressViewType = progressViewType;
      return this;
    }

    public Builder<T> setProgressShownListener(@NonNull ProgressShownListener progressShownListener) {
      this.progressShownListener = progressShownListener;
      return this;
    }

    public Builder<T> setMessage(CharSequence message) {
      this.message = message;
      return this;
    }

    public Builder<T> setMessageLayoutRes(@LayoutRes int messageLayoutRes) {
      this.messageLayoutRes = messageLayoutRes;
      return this;
    }

    public Builder<T> setMessageViewType(int messageViewType) {
      this.messageViewType = messageViewType;
      return this;
    }

    public Builder<T> setMessageClickListener(@NonNull MessageClickListener messageClickListener) {
      this.messageClickListener = messageClickListener;
      return this;
    }

    public PagingAdapter<T> build() {
      return new PagingAdapter<>(this);
    }

    public Builder<T> showProgressOnMessageClick(boolean showProgressOnMessageClick) {
      this.showProgressOnMessageClick = showProgressOnMessageClick;
      return this;
    }
  }

  public interface ProgressShownListener {
    void onProgressShown();
  }

  public interface MessageClickListener {
    void onMessageClick();
  }

  private enum State {
    IDLE, READY_TO_LOAD, LOADING, MESSAGE
  }
}
