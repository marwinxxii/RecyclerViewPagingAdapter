package com.a6v.pagingadapter.sample.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.a6v.pagingadapter.sample.R;
import com.a6v.pagingadapter.sample.widgets.MessageViewHolder;

import java.util.List;

public class StringItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  public static final int PROGRESS_VIEW_TYPE = 1, MESSAGE_VIEW_TYPE = 2;

  private final List<String> items;
  private final LayoutInflater inflater;

  public StringItemsAdapter(List<String> items, LayoutInflater inflater) {
    this.items = items;
    this.inflater = inflater;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case PROGRESS_VIEW_TYPE:
        return new ProgressViewHolder(inflater.inflate(R.layout.widget_list_progress, parent, false));
      case MESSAGE_VIEW_TYPE:
        return new MessageViewHolder(inflater.inflate(R.layout.widget_list_message, parent, false));
      case 0:
      default:
        return new StringViewHolder(inflater.inflate(R.layout.widget_string_item, parent, false));
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    switch (holder.getItemViewType()) {
      case 0:
        ((StringViewHolder) holder).bind(items.get(position));
        break;
    }
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  public static class StringViewHolder extends RecyclerView.ViewHolder {
    public StringViewHolder(View itemView) {
      super(itemView);
    }

    public void bind(String text) {
      ((TextView) itemView).setText(text);
    }
  }

  public static class ProgressViewHolder extends RecyclerView.ViewHolder {
    public ProgressViewHolder(View itemView) {
      super(itemView);
    }
  }
}
