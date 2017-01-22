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
import android.view.ViewGroup;

import com.a6v.pagingadapter.PagingAdapterDelegate;
import com.a6v.pagingadapter.sample.ButtonItems;
import com.a6v.pagingadapter.sample.R;
import com.a6v.pagingadapter.sample.widgets.ButtonViewHolder;
import com.a6v.pagingadapter.sample.widgets.MessageViewHolder;
import com.a6v.pagingadapter.sample.widgets.ProgressViewHolder;

import java.util.List;

public class AdapterWithDelegateActivity extends AppCompatActivity {
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.widget_list_items);
    RecyclerView view = (RecyclerView) findViewById(android.R.id.list);
    view.setHasFixedSize(true);
    view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    view.setAdapter(new Adapter());
  }

  static class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static final int PROGRESS_VIEW_TYPE = 0, MESSAGE_VIEW_TYPE = 1, BUTTON_VIEW_TYPE = 2;

    private final List<Pair<CharSequence, View.OnClickListener>> items;
    private final PagingAdapterDelegate pagingDelegate;
    private LayoutInflater inflater;

    public Adapter() {
      pagingDelegate = new PagingAdapterDelegate(this, PROGRESS_VIEW_TYPE, MESSAGE_VIEW_TYPE);
      items = ButtonItems.get(pagingDelegate);
    }

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
      if (!pagingDelegate.onBindViewHolder(holder, position)) {
        ButtonViewHolder buttonHolder = (ButtonViewHolder) holder;
        Pair<CharSequence, View.OnClickListener> item = items.get(position);
        buttonHolder.bind(item.first, item.second);
      }
    }

    @Override
    public int getItemCount() {
      return pagingDelegate.getItemCount(getInnerItemCount());
    }

    @Override
    public int getItemViewType(int position) {
      Integer delegateType = pagingDelegate.getItemViewType(position, getInnerItemCount());
      if (delegateType != null) {
        return delegateType;
      }
      return BUTTON_VIEW_TYPE;
    }

    public PagingAdapterDelegate getPagingDelegate() {
      return pagingDelegate;
    }

    private int getInnerItemCount() {
      return items.size();
    }

    private View inflate(ViewGroup parent, @LayoutRes int layout) {
      if (inflater == null) {
        inflater = LayoutInflater.from(parent.getContext());
      }
      return inflater.inflate(layout, parent, false);
    }
  }
}
