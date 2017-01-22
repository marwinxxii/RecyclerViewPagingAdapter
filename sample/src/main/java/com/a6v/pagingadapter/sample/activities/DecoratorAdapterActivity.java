package com.a6v.pagingadapter.sample.activities;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.a6v.pagingadapter.PagingAdapterDecorator;
import com.a6v.pagingadapter.sample.ButtonItems;
import com.a6v.pagingadapter.sample.R;
import com.a6v.pagingadapter.sample.widgets.ButtonViewHolder;
import com.a6v.pagingadapter.sample.widgets.MessageViewHolder;
import com.a6v.pagingadapter.sample.widgets.ProgressViewHolder;

import java.util.Collections;
import java.util.List;

public class DecoratorAdapterActivity extends AppCompatActivity {
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.widget_list_items);
    RecyclerView view = (RecyclerView) findViewById(android.R.id.list);
    view.setHasFixedSize(true);
    view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

    Adapter itemsAdapter = new Adapter();
    final PagingAdapterDecorator pagingAdapter = new PagingAdapterDecorator(itemsAdapter,
      Adapter.PROGRESS_VIEW_TYPE, Adapter.MESSAGE_VIEW_TYPE);
    itemsAdapter.setItems(ButtonItems.get(pagingAdapter));
    view.setAdapter(pagingAdapter);
  }

  static class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static final int PROGRESS_VIEW_TYPE = 0, MESSAGE_VIEW_TYPE = 1, BUTTON_VIEW_TYPE = 2;

    private List<Pair<CharSequence, OnClickListener>> items = Collections.emptyList();
    private LayoutInflater inflater;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      switch (viewType) {
        case PROGRESS_VIEW_TYPE:
          return new ProgressViewHolder(inflate(parent, R.layout.widget_list_progress));
        case MESSAGE_VIEW_TYPE:
          return new MessageViewHolder(inflate(parent, R.layout.widget_list_message));
      }
      return new ButtonViewHolder(inflate(parent, R.layout.widget_list_btn));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
      ButtonViewHolder buttonHolder = (ButtonViewHolder) holder;
      Pair<CharSequence, OnClickListener> item = items.get(position);
      buttonHolder.bind(item.first, item.second);
    }

    @Override
    public int getItemCount() {
      return items.size();
    }

    @Override
    public int getItemViewType(int position) {
      return BUTTON_VIEW_TYPE;
    }

    public void setItems(List<Pair<CharSequence, OnClickListener>> items) {
      this.items = items;
    }

    private View inflate(ViewGroup parent, @LayoutRes int layout) {
      if (inflater == null) {
        inflater = LayoutInflater.from(parent.getContext());
      }
      return inflater.inflate(layout, parent, false);
    }
  }
}
