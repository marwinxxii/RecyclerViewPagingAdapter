package com.a6v.pagingadapter.sample.widgets;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class MessageViewHolder extends RecyclerView.ViewHolder
  implements com.a6v.pagingadapter.MessageViewHolder
{
  private final TextView text;

  public MessageViewHolder(View itemView) {
    super(itemView);
    text = (TextView) itemView;
  }

  @Override
  public void bindMessage(@Nullable CharSequence message, @Nullable View.OnClickListener clickListener) {
    text.setText(message);
    if (clickListener != null) {
      text.setOnClickListener(clickListener);
    }
  }
}
