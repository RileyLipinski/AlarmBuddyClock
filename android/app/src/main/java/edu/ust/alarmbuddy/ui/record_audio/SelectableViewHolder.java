package edu.ust.alarmbuddy.ui.record_audio;

import android.graphics.Color;
import android.view.View;
import android.widget.CheckedTextView;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;

public class SelectableViewHolder extends RecyclerView.ViewHolder{

    CheckedTextView textView;
    SelectableProfile profile;
    OnItemSelectedListener itemSelectedListener;


    public SelectableViewHolder(View view, OnItemSelectedListener listener) {
        super(view);
        itemSelectedListener = listener;
        textView = (CheckedTextView) view.findViewById(R.id.checked_text_item);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (profile.isSelected()) {
                    setChecked(false);
                } else {
                    setChecked(true);
                }
                itemSelectedListener.onItemSelected(profile);

            }
        });
    }

    public void setChecked(boolean value) {
        if (value) {
            textView.setBackgroundColor(Color.LTGRAY);
        } else {
            textView.setBackground(null);
        }
        profile.setSelected(value);
        textView.setChecked(value);
    }

    public interface OnItemSelectedListener {
        void onItemSelected(SelectableProfile item);
    }

}