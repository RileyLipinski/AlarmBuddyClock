package edu.ust.alarmbuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.ust.alarmbuddy.ui.soundlist.SoundListAdapter;
import java.util.ArrayList;

public class SoundListActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_list);

		listSounds(getIntent().getStringExtra("json"));
		setupButton();
	}

	private void listSounds(String jsonString) {
		JsonArray json = JsonParser.parseString(jsonString).getAsJsonArray();

		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		RecyclerView recyclerView = findViewById(R.id.soundList);
		ArrayList<String> soundList = new ArrayList<>(json.size());
		int i = 0;

		for (JsonElement x : json) {
			String title = x.getAsJsonObject().get("soundName").getAsString();
			String sender = x.getAsJsonObject().get("sharedBy").getAsString();
			if(sender.equals("")) {
				sender = "LEGACY";
			}
			String newSound = ++i + ": \"" + title + "\" from " + sender;

			soundList.add(newSound);
		}
		SoundListAdapter soundListAdapter = new SoundListAdapter(soundList);

		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(soundListAdapter);
	}

	private void setupButton() {
		findViewById(R.id.soundListBackButton)
			.setOnClickListener(view -> startActivity(new Intent(this, MainActivity.class)));
	}
}