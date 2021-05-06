package edu.ust.alarmbuddy;

import android.app.Activity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.ust.alarmbuddy.ui.soundlist.SoundListAdapter;

import java.util.ArrayList;

public class SoundListActivity extends Activity {

	private RecyclerView recyclerView;
	private ArrayList<String> soundList;
	private RecyclerView.LayoutManager layoutManager;
	private SoundListAdapter soundListAdapter;
	private JsonArray json;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_list);

		String jsonString = getIntent().getStringExtra("json");
		this.json = JsonParser.parseString(jsonString).getAsJsonArray();

		layoutManager = new LinearLayoutManager(getApplicationContext());

		recyclerView = findViewById(R.id.soundList);
		soundList = new ArrayList<>(json.size());
		int i = 0;
		//TODO check against last seen to avoid marking new sounds
		//TODO add extra metadata (who from, etc.)
		for(JsonElement x : json) {
			soundList.add("NEW SOUND " + x.getAsJsonObject().get("soundID").getAsInt());
		}
		soundListAdapter = new SoundListAdapter(soundList);

		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(soundListAdapter);
	}
}