package edu.ust.alarmbuddy.ui.friends;

import android.app.Application;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import edu.ust.alarmbuddy.common.UserData;
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
	private int flag = 1;

	/***
	 * @author Keghan Halloran
	 * This class is used by the ProfileAdapter. It holds references to items as a means of increasing proformance.
	 */
	public static class ProfileViewHolder extends RecyclerView.ViewHolder {

		private ImageView mImageView;
		private TextView mTextView1;
		private TextView mTextView2;
		private View view;
		private Profile currentProfile;
		private int flag;

		public ProfileViewHolder(@NonNull @NotNull View itemView, int num) {
			super(itemView);
			view = itemView;
			flag = num;
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Click(flag);
				}
			});
			mImageView = itemView.findViewById(R.id.imageView);
			mTextView1 = itemView.findViewById(R.id.textView);
			mTextView2 = itemView.findViewById(R.id.textView2);
			currentProfile = null;

		}

		public void Click(int flag){

			if (flag==0){
				Intent intent = new Intent(itemView.getContext(), Friend_Options.class);
				intent.putExtra("name", mTextView1.getText().toString());
				itemView.getContext().startActivity(intent);
			}
			else{
				Intent intent = new Intent(itemView.getContext(), Request_Options.class);
				intent.putExtra("name", mTextView1.getText().toString());
				itemView.getContext().startActivity(intent);
			}

		}
	}

	public ProfileAdapter(ArrayList<Profile> profileList, int num) {
		mProfileList = profileList;
		mProfileListFull = new ArrayList<>(profileList);
		flag = num;
	}

	@NonNull
	@NotNull
	@Override
	public ProfileViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
			.inflate(R.layout.friend_list, parent, false);
		return new ProfileViewHolder(v,flag);
	}

	@Override
	public void onBindViewHolder(@NonNull @NotNull ProfileViewHolder holder, int position) {
		holder.currentProfile = mProfileList.get(position);


		holder.mImageView.setImageResource(holder.currentProfile.getImageResource());
		holder.mTextView1.setText(holder.currentProfile.getText1());
		holder.mTextView2.setText(holder.currentProfile.getText2());

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
