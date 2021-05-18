package edu.ust.alarmbuddy.ui.record_audio;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.ui.friends.Profile;
import java.util.ArrayList;
import java.util.List;

public class SelectableProfileAdapter extends RecyclerView.Adapter implements
	SelectableViewHolder.OnItemSelectedListener {

	private List<SelectableProfile> mProfiles;
	SelectableViewHolder.OnItemSelectedListener listener;

	public SelectableProfileAdapter(SelectableViewHolder.OnItemSelectedListener listener,
		final List<SelectableProfile> selectableProfiles) {
		mProfiles = new ArrayList<>();
		if (selectableProfiles != null) {
			this.mProfiles.addAll(selectableProfiles);
		}
		this.listener = listener;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
		final View view = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.selected_profile, parent, false);
		return new SelectableViewHolder(view, (SelectableViewHolder.OnItemSelectedListener) this);
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {

		SelectableViewHolder holder = (SelectableViewHolder) viewHolder;
		SelectableProfile selectableProfile = mProfiles.get(position);
		String name = selectableProfile.getText1();
		holder.textView.setText(name);

		TypedValue value = new TypedValue();
		holder.textView.getContext().getTheme()
			.resolveAttribute(android.R.attr.listChoiceIndicatorMultiple, value, true);
		int checkMarkDrawableResId = value.resourceId;
		holder.textView.setCheckMarkDrawable(checkMarkDrawableResId);

		holder.profile = selectableProfile;
		holder.setChecked(holder.profile.isSelected());
	}

	@Override
	public int getItemCount() {
		return mProfiles.size();
	}

	@Override
	public int getItemViewType(final int position) {
		return R.layout.selected_profile;
	}

	public List<Profile> getSelectedItems() {

		List<Profile> selectedItems = new ArrayList<>();
		for (SelectableProfile item : mProfiles) {
			if (item.isSelected()) {
				selectedItems.add(item);
			}
		}
		return selectedItems;
	}

	@Override
	public void onItemSelected(SelectableProfile item) {
        /*
        if (!isMultiSelectionEnabled) {

            for (SelectableItem selectableItem : mValues) {
                if (!selectableItem.equals(item)
                        && selectableItem.isSelected()) {
                    selectableItem.setSelected(false);
                } else if (selectableItem.equals(item)
                        && item.isSelected()) {
                    selectableItem.setSelected(true);
                }
            }
            notifyDataSetChanged();
        }
        */
		listener.onItemSelected(item);
	}

	public void unselectAll() {
		for (int i = 0; i < mProfiles.size(); i++) {
			SelectableProfile currProfile = mProfiles.get(i);
			currProfile.setSelected(false);
		}
		notifyDataSetChanged();
	}
}
