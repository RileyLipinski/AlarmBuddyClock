package edu.ust.alarmbuddy.ui.friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

/***
 * @author Keghan Halloran
 * This adapter is necessary to correctly adapt the information in a users friends list
 * to our recyclerview.
 */
public class ProfileAdapter extends
	RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> implements Filterable {

	private final ArrayList<Profile> mProfileList;
	private final ArrayList<Profile> mProfileListFull;

	/***
	 * @author Keghan Halloran
	 * This class is used by the ProfileAdapter. It holds references to items as a means of increasing proformance.
	 */
	public static class ProfileViewHolder extends RecyclerView.ViewHolder {

		private ImageView mImageView;
		private TextView mTextView1;
		private TextView mTextView2;

		public ProfileViewHolder(@NonNull @NotNull View itemView) {
			super(itemView);
			mImageView = itemView.findViewById(R.id.imageView);
			mTextView1 = itemView.findViewById(R.id.textView);
			mTextView2 = itemView.findViewById(R.id.textView2);

		}
	}

	public ProfileAdapter(ArrayList<Profile> profileList) {
		mProfileList = profileList;
		mProfileListFull = new ArrayList<>(profileList);
	}

	@NonNull
	@NotNull
	@Override
	public ProfileViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.friend_list, parent, false);
		return new ProfileViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull @NotNull ProfileViewHolder holder, int position) {
		Profile currentItem = mProfileList.get(position);

		holder.mImageView.setImageResource(currentItem.getImageResource());
		holder.mTextView1.setText(currentItem.getText1());
		holder.mTextView2.setText(currentItem.getText2());

	}

	@Override
	public int getItemCount() {
		return mProfileList.size();
	}

	@Override
	public Filter getFilter() {
		return profileFilter;
	}

	private final Filter profileFilter = new Filter() {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			ArrayList<Profile> filteredList = new ArrayList<>();

			if (constraint == null || constraint.length() == 0) {
				filteredList.addAll(mProfileListFull);
			} else {
				String filterPattern = constraint.toString().toLowerCase().trim();

				for (Profile item : mProfileListFull) {
					if (item.getText1().toLowerCase().contains(filterPattern)) {
						filteredList.add(item);
					}
				}
			}
			FilterResults results = new FilterResults();
			results.values = filteredList;

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mProfileList.clear();
			mProfileList.addAll((ArrayList) results.values);
			notifyDataSetChanged();
		}
	};

}
