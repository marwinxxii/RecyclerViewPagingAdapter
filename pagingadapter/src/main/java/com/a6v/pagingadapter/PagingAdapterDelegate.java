package com.a6v.pagingadapter;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.a6v.pagingadapter.PagingAdapterDelegate.State.KIND_IDLE;
import static com.a6v.pagingadapter.PagingAdapterDelegate.State.KIND_LOADING;
import static com.a6v.pagingadapter.PagingAdapterDelegate.State.KIND_MESSAGE;
import static com.a6v.pagingadapter.PagingAdapterDelegate.State.KIND_READY_TO_LOAD;

public class PagingAdapterDelegate implements IPagingAdapter {
  private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
  private final Runnable notifyProgressShownRunnable = new Runnable() {
    @Override
    public void run() {
      if (progressShownListener != null) {
        progressShownListener.onProgressShown();
      }
    }
  };
  private final RecyclerView.Adapter adapter;
  private final int progressViewType;
  private final long progressItemId;
  private final int messageViewType;
  private final long messageItemId;

  @Nullable ProgressShownListener progressShownListener;
  @Nullable private View.OnClickListener messageClickListener;

  private State state = State.IDLE;

  public PagingAdapterDelegate(RecyclerView.Adapter adapter,
    int progressViewType,
    long progressItemId,
    int messageViewType,
    long messageItemId)
  {
    if (progressViewType == messageViewType) {
      throw new IllegalArgumentException("progressViewType can't be equal to messageViewType");
    }
    if (progressItemId == messageItemId && progressItemId != RecyclerView.NO_ID) {
      throw new IllegalArgumentException("progressItemId can't be equal to messageItemId");
    }
    this.progressViewType = progressViewType;
    this.progressItemId = progressItemId;
    this.messageViewType = messageViewType;
    this.messageItemId = messageItemId;
    this.adapter = adapter;
  }

  public PagingAdapterDelegate(RecyclerView.Adapter adapter, int progressViewType,
    int messageViewType)
  {
    this(adapter, progressViewType, RecyclerView.NO_ID, messageViewType, RecyclerView.NO_ID);
  }

  public int getItemCount(int itemCount) {
    final int extraItems;
    switch (state.kind) {
      case KIND_READY_TO_LOAD:
      case KIND_LOADING:
      case KIND_MESSAGE:
        extraItems = 1;
        break;
      case KIND_IDLE:
      default:
        extraItems = 0;
        break;
    }
    return itemCount + extraItems;
  }

  @Nullable
  public Integer getItemViewType(int position, int itemCount) {
    switch (state.kind) {
      case KIND_IDLE:
        break;
      case KIND_READY_TO_LOAD:
      case KIND_LOADING:
        if (position == itemCount) {
          return progressViewType;
        }
        break;
      case KIND_MESSAGE:
        if (position == itemCount) {
          return messageViewType;
        }
        break;
    }
    return null;
  }

  @Nullable
  public Long getItemId(int position, int itemCount) {
    if (adapter.hasStableIds()) {
      switch (state.kind) {
        case State.KIND_IDLE:
          break;
        case State.KIND_LOADING:
        case State.KIND_READY_TO_LOAD:
          final Integer pivt = getItemViewType(position, itemCount);
          if (pivt != null && pivt == progressViewType) {
            return progressItemId;
          }
          break;
        case State.KIND_MESSAGE:
          final Integer mivt = getItemViewType(position, itemCount);
          if (mivt != null && mivt == messageViewType) {
            return messageItemId;
          }
          break;
      }
      return null;
    }
    return RecyclerView.NO_ID;
  }

  public boolean onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    switch (state.kind) {
      case State.KIND_IDLE:
        break;
      case State.KIND_READY_TO_LOAD:
        if (holder.getItemViewType() == progressViewType) {
          switchState(new LoadingState(holder));
          mainThreadHandler.post(notifyProgressShownRunnable);
          return true;
        }
        break;
      case State.KIND_LOADING:
        if (holder.getItemViewType() == progressViewType) {
          LoadingState loading = (LoadingState) this.state;
          if (loading.viewHolder == null) {
            loading.viewHolder = holder;
          }
          return true;
        }
        break;
      case State.KIND_MESSAGE:
        if (holder.getItemViewType() == messageViewType) {
          MessageState ms = (MessageState) this.state;
          if (ms.viewHolder == null) {
            ms.viewHolder = holder;
          }
          ((MessageViewHolder) ms.viewHolder).bindMessage(ms.message, messageClickListener);
          return true;
        }
        break;
    }
    return false;
  }

  @Override
  public void setProgressShownListener(@NonNull ProgressShownListener listener) {
    progressShownListener = listener;
  }

  @Override
  public void removeProgressShownListener() {
    mainThreadHandler.removeCallbacks(notifyProgressShownRunnable);
    progressShownListener = null;
  }

  @Override
  public void setMessageClickListener(@NonNull View.OnClickListener listener) {
    messageClickListener = listener;
  }

  @Override
  public void removeMessageClickListener() {
    messageClickListener = null;//unbind viewholder?
  }

  @Override
  public void showMessage(CharSequence message) {
    switchState(new MessageState(message, null));
  }

  @Override
  public void hideMessage(boolean enableNextPageLoading) {
    if (state.kind == KIND_MESSAGE) {
      switchState(enableNextPageLoading ? State.READY_TO_LOAD : State.IDLE);
    }
  }

  @Override
  public void showProgress() {
    if (state.kind != State.KIND_LOADING) {
      switchState(new LoadingState(null));
    }
  }

  @Override
  public void hideProgress(boolean enableNextPageLoading) {
    if (state.kind == State.KIND_LOADING) {
      switchState(enableNextPageLoading ? State.READY_TO_LOAD : State.IDLE);
    }
  }

  @Override
  public void setShowProgressEnabled(boolean enabled) {
    switch (state.kind) {
      case KIND_IDLE:
        if (enabled) {
          switchState(State.READY_TO_LOAD);
        }
        break;
      case KIND_MESSAGE:
      case KIND_LOADING:
        break;
      case KIND_READY_TO_LOAD:
        if (!enabled) {
          switchState(State.IDLE);
        }
        break;
    }
  }

  private void switchState(final State state) {
    final State previous = this.state;
    final int previousPosition = getItemPosition(previous);
    this.state = state;
    switch (state.kind) {
      case KIND_IDLE:
      case KIND_READY_TO_LOAD:
        if (previousPosition != RecyclerView.NO_POSITION) {
          adapter.notifyItemRemoved(previousPosition);
        }
        break;
      case KIND_LOADING:
      case KIND_MESSAGE:
        if (previousPosition != RecyclerView.NO_POSITION) {
          adapter.notifyItemChanged(previousPosition);
        } else {
          switch (previous.kind) {
            case KIND_IDLE:
              adapter.notifyItemInserted(adapter.getItemCount());
              break;
            case KIND_LOADING:
            case KIND_MESSAGE:
              adapter.notifyItemChanged(adapter.getItemCount());
              break;
            case KIND_READY_TO_LOAD:
              break;
          }
        }
        break;
    }
  }

  private static int getItemPosition(final State state) {
    final int position;
    switch (state.kind) {
      case KIND_LOADING:
        RecyclerView.ViewHolder pvh = ((LoadingState) state).viewHolder;
        position = pvh != null ? pvh.getAdapterPosition() : RecyclerView.NO_POSITION;
        break;
      case KIND_MESSAGE:
        RecyclerView.ViewHolder mvh = ((MessageState) state).viewHolder;
        position = mvh != null ? mvh.getAdapterPosition() : RecyclerView.NO_POSITION;
        break;
      case KIND_IDLE:
      case KIND_READY_TO_LOAD:
      default:
        position = RecyclerView.NO_POSITION;
        break;
    }
    return position;
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
    @Nullable public RecyclerView.ViewHolder viewHolder;

    LoadingState(@Nullable RecyclerView.ViewHolder viewHolder) {
      super(KIND_LOADING);
      this.viewHolder = viewHolder;
    }
  }

  static class MessageState extends State {
    public final CharSequence message;
    @Nullable public RecyclerView.ViewHolder viewHolder;

    MessageState(CharSequence message, @Nullable RecyclerView.ViewHolder viewHolder) {
      super(KIND_MESSAGE);
      this.message = message;
      this.viewHolder = viewHolder;
    }
  }
}
