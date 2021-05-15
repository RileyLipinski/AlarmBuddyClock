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

public class SelectableAdapter extends RecyclerView.Adapter implements SelectableViewHolder.OnItemSelectedListener {

    private final List<SelectableProfile> profiles;
    SelectableViewHolder.OnItemSelectedListener listener;


    public SelectableAdapter(SelectableViewHolder.OnItemSelectedListener listener,
                             List<Profile> incomingProfiles, List<SelectableProfile> profiles1) {
        this.listener = listener;

        profiles = new ArrayList<>();
        for (Profile profile : incomingProfiles) {
            profiles.add(new SelectableProfile(profile));
        }
    }

    @Override
    public SelectableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.checked_item, parent, false);

        return new SelectableViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        SelectableViewHolder holder = (SelectableViewHolder) viewHolder;
        SelectableProfile selectableProfile = profiles.get(position);
        // what holds the profile's name?
        String name = selectableProfile.getText1();
        holder.textView.setText(name);

        TypedValue value = new TypedValue();
        holder.textView.getContext().getTheme().resolveAttribute(android.R.attr.listChoiceIndicatorMultiple, value, true);
        int checkMarkDrawableResId = value.resourceId;
        holder.textView.setCheckMarkDrawable(checkMarkDrawableResId);


        holder.profile = selectableProfile;
        holder.setChecked(holder.profile.isSelected());
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    public List<Profile> getSelectedItems() {

        List<Profile> selectedItems = new ArrayList<>();
        for (SelectableProfile profile : profiles) {
            if (profile.isSelected()) {
                selectedItems.add(profile);
            }
        }
        return selectedItems;
    }

    @Override
    public void onItemSelected(SelectableProfile profile) {
        listener.onItemSelected(profile);
    }
}