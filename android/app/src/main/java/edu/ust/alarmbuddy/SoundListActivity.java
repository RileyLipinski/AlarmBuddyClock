package edu.ust.alarmbuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import edu.ust.alarmbuddy.ui.soundlist.SoundListAdapter;
import java.util.ArrayList;

public class SoundListActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_list);
		returnToMainButton();
	}

	/**
	 * Rebuilds the list of sounds received from the database each time the activity is restarted.
	 */
	@Override
	public void onResume() {
		super.onResume();

		RecyclerView recyclerView = findViewById(R.id.soundList);
		SoundListAdapter soundListAdapter = (SoundListAdapter) recyclerView.getAdapter();
		if (soundListAdapter != null) {
			soundListAdapter.empty();
		}

		listSounds(getIntent().getStringExtra("json"));
	}

	private void listSounds(String jsonString) {
		Log.i(SoundListActivity.class.getName(), "Attempting to list this json: " + jsonString);
		JsonArray json;
		try {
			json = JsonParser.parseString(jsonString).getAsJsonArray();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			return;
		}

		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
		RecyclerView recyclerView = findViewById(R.id.soundList);
		ArrayList<String> soundList = new ArrayList<>(json.size());
		int i = 0;

		for (JsonElement x : json) {
			String title = x.getAsJsonObject().get("soundName").getAsString();
			String sender = x.getAsJsonObject().get("sharedBy").getAsString();
			int soundId = x.getAsJsonObject().get("soundID").getAsInt();

			if (sender.equals("")) {
				sender = "LEGACY";
			}
			String newSound = soundId + ": \"" + title + "\" from " + sender;
//			String newSound = ++i + ": \"" + title + "\" from " + sender;
			soundList.add(newSound);
		}
		SoundListAdapter soundListAdapter = new SoundListAdapter(soundList);

		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(soundListAdapter);
	}

	private void returnToMainButton() {
		findViewById(R.id.soundListBackButton)
			.setOnClickListener(view -> startActivity(new Intent(this, MainActivity.class)));
	}
}