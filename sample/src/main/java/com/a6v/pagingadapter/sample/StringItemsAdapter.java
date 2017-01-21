package com.a6v.pagingadapter.sample;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class StringItemsAdapter extends RecyclerView.Adapter<StringItemsAdapter.StringViewHolder> {
  private final List<String> items;
  private final LayoutInflater inflater;

  public StringItemsAdapter(List<String> items, LayoutInflater inflater) {
    this.items = items;
    this.inflater = inflater;
  }

  @Override
  public StringViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new StringViewHolder(inflater.inflate(R.layout.widget_string_item, parent, false));
  }

  @Override
  public void onBindViewHolder(StringViewHolder holder, int position) {
    holder.bind(items.get(position));
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
      ((TextView)itemView).setText(text);
    }
  }
}
