package edu.ust.alarmbuddy.ui.soundlist;

import android.app.Activity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;
import java.util.ArrayList;

public class SoundListActivity extends Activity {

	private RecyclerView recyclerView;
	private ArrayList<String> soundList;
	private RecyclerView.LayoutManager layoutManager;
	private SoundListAdapter soundListAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_list);

		layoutManager = new LinearLayoutManager(getApplicationContext());

		recyclerView = findViewById(R.id.soundList);
		soundList = new ArrayList<>(2);
		soundList.add("one");
		soundList.add("two");
		soundListAdapter = new SoundListAdapter(soundList);

		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(soundListAdapter);
	}
}