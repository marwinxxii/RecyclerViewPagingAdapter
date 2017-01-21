package com.a6v.pagingadapter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.*;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.a6v.pagingadapter.PagingAdapter.State.KIND_IDLE;
import static com.a6v.pagingadapter.PagingAdapter.State.KIND_LOADING;
import static com.a6v.pagingadapter.PagingAdapter.State.KIND_MESSAGE;
import static com.a6v.pagingadapter.PagingAdapter.State.KIND_READY_TO_LOAD;

public class PagingAdapter extends RecyclerView.Adapter {
  public static final int PROGRESS_VIEW_TYPE = 100, MESSAGE_VIEW_TYPE = 200;

  private final Adapter adapter;
  private final LayoutInflater inflater;

  @LayoutRes private final int progressLayoutRes;
  private final int progressViewType;

  @LayoutRes private final int messageLayoutRes;
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

  @Nullable ProgressShownListener progressShownListener;
  @Nullable OnClickListener messageClickListener;

  private State state = State.IDLE;
  private int itemCount = 0;

  public PagingAdapter(Builder builder) {
    adapter = builder.adapter;
    adapter.registerAdapterDataObserver(new AdapterDataObserverWrapper(this));
    inflater = builder.inflater;
    progressLayoutRes = builder.progressLayoutRes;
    progressViewType = builder.progressViewType;
    messageLayoutRes = builder.messageLayoutRes;
    messageViewType = builder.messageViewType;
  }

  @Override
  public int getItemCount() {
    int count = 0;
    switch (state.kind) {
      case KIND_IDLE:
        count = 0;
        break;
      case KIND_READY_TO_LOAD:
      case KIND_LOADING:
      case KIND_MESSAGE:
        count = 1;
    }
    itemCount = adapter.getItemCount() + count;
    return itemCount;
  }

  @Override
  public int getItemViewType(int position) {
    if (position == itemCount - 1) {
      switch (state.kind) {
        case KIND_READY_TO_LOAD:
        case KIND_LOADING:
          return PROGRESS_VIEW_TYPE;
        case KIND_MESSAGE:
          return MESSAGE_VIEW_TYPE;
        case KIND_IDLE:
          break;
      }
    }
    return adapter.getItemViewType(position);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == progressViewType) {
      return new ProgressViewHolder(inflater.inflate(progressLayoutRes, parent, false));
    } else if (viewType == messageViewType) {
      return new MessageViewHolder(inflater.inflate(messageLayoutRes, parent, false));
    }
    return adapter.onCreateViewHolder(parent, viewType);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    switch (state.kind) {
      case State.KIND_IDLE:
        break;
      case State.KIND_READY_TO_LOAD:
        if (holder instanceof ProgressViewHolder) {
          switchState(new LoadingState((ProgressViewHolder) holder));
          mainThreadHandler.post(notifyProgressShownRunnable);
          return;
        }
        break;
      case State.KIND_LOADING:
        if (holder instanceof ProgressViewHolder) {
          LoadingState loading = (LoadingState) this.state;
          if (loading.viewHolder == null) {
            loading.viewHolder = (ProgressViewHolder) holder;
          }
          return;
        }
        break;
      case State.KIND_MESSAGE:
        if (holder instanceof MessageViewHolder) {
          MessageState ms = (MessageState) this.state;
          if (ms.viewHolder == null) {
            ms.viewHolder = (MessageViewHolder) holder;
          }
          ms.viewHolder.bind(ms.message, messageClickListener);
          return;
        }
        break;
    }
    adapter.onBindViewHolder(holder, position);
  }

  public void setProgressShownListener(@Nullable ProgressShownListener progressShownListener) {
    this.progressShownListener = progressShownListener;
  }

  public void removeProgressShownListener() {
    mainThreadHandler.removeCallbacks(notifyProgressShownRunnable);
    progressShownListener = null;
  }

  public void setMessageClickListener(@Nullable OnClickListener messageClickListener) {
    this.messageClickListener = messageClickListener;
  }

  public void removeMessageClickListener() {
    messageClickListener = null;
  }

  public void showMessage(CharSequence message) {
    switchState(new MessageState(message, null));
  }

  public void hideMessage(boolean enableNextPageLoading) {
    if (state.kind == KIND_MESSAGE) {
      switchState(enableNextPageLoading ? State.READY_TO_LOAD : State.IDLE);
    }
  }

  public void showProgress() {
    if (state.kind != State.KIND_LOADING) {
      switchState(new LoadingState(null));
    }
  }

  public void hideProgress(boolean enableNextPageLoading) {
    if (state.kind == State.KIND_LOADING) {
      switchState(enableNextPageLoading ? State.READY_TO_LOAD : State.IDLE);
    }
  }

  public void startLoadingPages() {
    switch (state.kind) {
      case KIND_IDLE:
        switchState(State.READY_TO_LOAD);
        break;
      case KIND_MESSAGE:
        hideMessage(true);
        break;
      case KIND_LOADING:
        throw new IllegalStateException("Can't start new load when already loading");
      case KIND_READY_TO_LOAD:
        //do nothing
        break;
    }
  }

  private void switchState(State state) {
    State previous = this.state;
    int previousPosition = getItemPosition(previous);
    this.state = state;
    switch (state.kind) {
      case State.KIND_IDLE:
      case State.KIND_READY_TO_LOAD:
        if (previousPosition != RecyclerView.NO_POSITION) {
          notifyItemRemoved(previousPosition);
        }
        break;
      case State.KIND_LOADING:
      case State.KIND_MESSAGE:
        if (previousPosition != RecyclerView.NO_POSITION) {
          notifyItemChanged(previousPosition);
        } else if (previous.kind == KIND_IDLE) {
          notifyDataSetChanged();//TODO inserted
        }
        break;
    }
  }

  private static int getItemPosition(State state) {
    int position;
    switch (state.kind) {
      case State.KIND_LOADING:
        ProgressViewHolder pvh = ((LoadingState) state).viewHolder;
        position = pvh != null ? pvh.getAdapterPosition() : RecyclerView.NO_POSITION;
        break;
      case State.KIND_MESSAGE:
        MessageViewHolder mvh = ((MessageState) state).viewHolder;
        position = mvh != null ? mvh.getAdapterPosition() : RecyclerView.NO_POSITION;
        break;
      case State.KIND_IDLE:
      case State.KIND_READY_TO_LOAD:
      default:
        position = RecyclerView.NO_POSITION;
        break;
    }
    return position;
  }

  public static class Builder {
    final Adapter<?> adapter;
    final LayoutInflater inflater;

    @LayoutRes int progressLayoutRes = R.layout.rvpa_widget_list_progress;
    int progressViewType = PROGRESS_VIEW_TYPE;

    @LayoutRes int messageLayoutRes = R.layout.rvpa_widget_list_message;
    int messageViewType = MESSAGE_VIEW_TYPE;

    public Builder(@NonNull Adapter<?> adapter, @NonNull LayoutInflater inflater) {
      this.adapter = adapter;
      this.inflater = inflater;
    }

    public Builder setProgressLayoutRes(@LayoutRes int progressLayoutRes) {
      this.progressLayoutRes = progressLayoutRes;
      return this;
    }

    public Builder setProgressViewType(int progressViewType) {
      this.progressViewType = progressViewType;
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

    public PagingAdapter build() {
      return new PagingAdapter(this);
    }
  }

  public interface ProgressShownListener {
    void onProgressShown();
  }

  static class State {
    static final int KIND_IDLE = 0, KIND_READY_TO_LOAD = 1, KIND_LOADING = 2, KIND_MESSAGE = 3;
    static final State IDLE = new State(KIND_IDLE);
    static final State READY_TO_LOAD = new State(KIND_READY_TO_LOAD);

    @Kind public final int kind;

    protected State(@Kind int kind) {
      this.kind = kind;
    }

    @IntDef({KIND_IDLE, KIND_READY_TO_LOAD, KIND_LOADING, KIND_MESSAGE})
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @interface Kind {
    }
  }

  static class LoadingState extends State {
    @Nullable public ProgressViewHolder viewHolder;

    LoadingState(@Nullable ProgressViewHolder viewHolder) {
      super(KIND_LOADING);
      this.viewHolder = viewHolder;
    }
  }

  static class MessageState extends State {
    public final CharSequence message;
    @Nullable public MessageViewHolder viewHolder;

    MessageState(CharSequence message, @Nullable MessageViewHolder viewHolder) {
      super(KIND_MESSAGE);
      this.message = message;
      this.viewHolder = viewHolder;
    }
  }
}
