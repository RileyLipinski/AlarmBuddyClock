package edu.ust.alarmbuddy.ui.soundlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.ui.soundlist.SoundListAdapter.SoundListViewHolder;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class SoundListAdapter extends RecyclerView.Adapter<SoundListViewHolder> {

	private List<String> soundList;

	public SoundListAdapter(List<String> soundList) {
		this.soundList = soundList;
	}

	@NonNull
	@NotNull
	@Override
	public SoundListViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent,
		int viewType) {
		View v = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.sound_list_entry, parent, false);
		return new SoundListViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull @NotNull SoundListViewHolder holder, int position) {
		String current = soundList.get(position);
		holder.name.setText(current);
	}

	@Override
	public int getItemCount() {
		return soundList.size();
	}

	protected static class SoundListViewHolder extends RecyclerView.ViewHolder {

		private TextView name;

		public SoundListViewHolder(@NonNull @NotNull View itemView) {
			super(itemView);
			name = itemView.findViewById(R.id.soundName);
		}
	}

	public void empty() {
		soundList.clear();
		notifyDataSetChanged();
	}
}
