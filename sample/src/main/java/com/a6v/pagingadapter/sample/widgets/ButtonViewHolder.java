package com.a6v.pagingadapter.sample.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

public class ButtonViewHolder extends RecyclerView.ViewHolder {
  public ButtonViewHolder(View itemView) {
    super(itemView);
  }

  public void bind(CharSequence text, View.OnClickListener clickListener) {
    Button button = (Button) itemView;
    button.setText(text);
    button.setOnClickListener(clickListener);
  }
}
