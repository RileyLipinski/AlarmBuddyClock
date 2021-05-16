package edu.ust.alarmbuddy.ui.record_audio;

import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.ui.friends.Profile;

public class SelectableViewHolder extends RecyclerView.ViewHolder{

    //private TextView friendName;
    private OnItemSelectedListener itemSelectedListener;
    CheckedTextView textView;
    SelectableProfile profile = new SelectableProfile("");

    public SelectableViewHolder(final View itemView, OnItemSelectedListener listener) {
        super(itemView);
        //friendName = (TextView) itemView.findViewById(R.id.friend_name);
        itemSelectedListener = listener;
        textView = (CheckedTextView) itemView.findViewById(R.id.friend_name);
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

    public void bindData(final SelectableViewModel viewModel) {
        profile.setText1(viewModel.getFriendName());
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
