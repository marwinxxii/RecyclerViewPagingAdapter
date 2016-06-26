package com.a6v.pagingadapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class MessageViewHolder extends RecyclerView.ViewHolder {
  private final TextView messageView;

  public MessageViewHolder(View view) {
    super(view);
    this.messageView = (TextView) view.findViewById(R.id.rvpa_list_message);
  }

  public void bind(CharSequence message, @Nullable View.OnClickListener clickListener) {
    messageView.setText(message);
    messageView.setOnClickListener(clickListener);
  }
}
