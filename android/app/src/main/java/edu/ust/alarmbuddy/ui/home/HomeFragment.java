package edu.ust.alarmbuddy.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import edu.ust.alarmbuddy.R;

public class HomeFragment extends Fragment {

	private HomeViewModel homeViewModel;
	private View root;

	public View onCreateView(@NonNull LayoutInflater inflater,
		ViewGroup container, Bundle savedInstanceState) {
		homeViewModel =
			ViewModelProviders.of(this).get(HomeViewModel.class);
		root = inflater.inflate(R.layout.fragment_home, container, false);

		return root;
	}


}