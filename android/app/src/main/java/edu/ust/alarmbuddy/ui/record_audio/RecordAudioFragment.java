package edu.ust.alarmbuddy.ui.record_audio;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import edu.ust.alarmbuddy.R;
import java.io.File;
import java.io.IOException;

public class RecordAudioFragment extends Fragment {

	private View root;
	private RecordAudioViewModel mViewModel;

	private static final String LOG_TAG = "AudioRecord";
	private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
	private final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 2;

	private File audioFile = null;
	private static String fileName = null;

	private Button recordButton = null;
	private Button playButton = null;

	private TextView debugText = null;

	private MediaRecorder recorder = null;
	private MediaPlayer player = null;

	private boolean micPermission = false;
	private boolean mStartRecording = true;
	private boolean mStartPlaying = true;


	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
		@Nullable Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.fragment_record_audio, container, false);

		// where to store recorded audio file
		audioFile = new File(Environment.getExternalStorageDirectory(),
			"audio_test.3gp");
		// name to access audio file
		fileName = audioFile.getAbsolutePath();

		// for debugging purposes, delete later
		debugText = root.findViewById(R.id.recordText);

		// set buttons
		recordButton = root.findViewById(R.id.recordButton);
		playButton = root.findViewById(R.id.playButton);

		recordButton.setText("Start recording");
		playButton.setText("Start playing");

		// request user for permission to access microphone
		requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,
			Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

		// set listeners for button clicks
		View.OnClickListener recordClicker = new View.OnClickListener() {
			public void onClick(View v) {
				// only starts recording if mic can be used
				if (micPermission == true) {
					// when clicked, start or stop recording
					onRecord(mStartRecording);
					if (mStartRecording) {
						recordButton.setText("Stop recording");
					} else {
						recordButton.setText("Start recording");
					}
					mStartRecording = !mStartRecording;
				}
			}
		};
		recordButton.setOnClickListener(recordClicker);

		// set listener for play button
		View.OnClickListener playClicker = new View.OnClickListener() {
			public void onClick(View v) {
				// check if there is a recorded file to play
				if (audioFile.exists()) {
					// when clicked, start or stop playing sound
					onPlay(mStartPlaying);
					if (mStartPlaying) {
						playButton.setText("Stop playing");
					} else {
						playButton.setText("Start playing");
					}
					mStartPlaying = !mStartPlaying;
				} else {
					debugText.setText("there is no audio to play");
				}
			}
		};
		playButton.setOnClickListener(playClicker);

		return root;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mViewModel = ViewModelProviders.of(this).get(RecordAudioViewModel.class);
		// TODO: Use the ViewModel
	}

	public static RecordAudioFragment newInstance() {
		return new RecordAudioFragment();
	}

	// when app ends, free recorder and player resources
	@Override
	public void onStop() {
		super.onStop();
		if (recorder != null) {
			recorder.release();
			recorder = null;
		}

		if (player != null) {
			player.release();
			player = null;
		}
	}

	// handling callback for permission request
	@Override
	public void onRequestPermissionsResult(int requestCode,
		String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_RECORD_AUDIO: {
				if (grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay!
					micPermission = true;
					debugText.setText("mic permission granted");
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(getActivity(), "Permissions Denied to record audio",
						Toast.LENGTH_LONG).show();
					debugText.setText("mic permission not granted");
				}
				return;
			}
			case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
				if (grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay!
					micPermission = true;
					debugText.setText("write permission granted");
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(getActivity(), "Permissions Denied to write to storage",
						Toast.LENGTH_LONG).show();
					debugText.setText("storage permission not granted");
				}
				return;
			}
		}
	}


	private void onRecord(boolean start) {
		if (start) {
			startRecording();
		} else {
			stopRecording();
		}
	}

	private void onPlay(boolean start) {
		if (start) {
			startPlaying();
		} else {
			stopPlaying();
		}
	}

	private void startPlaying() {
		player = new MediaPlayer();
		try {
			player.setDataSource(fileName);
			debugText.setText("playing from " + fileName);
		} catch (IOException e) {
			Log.e(LOG_TAG, "setDataSource() failed");
		}
		try {
			player.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed: ");
		}

		player.start();
	}

	private void stopPlaying() {
		player.release();
		player = null;
	}

	private void startRecording() {
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setOutputFile(fileName);
		debugText.setText("recording to " + fileName);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			recorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}

		recorder.start();
	}

	private void stopRecording() {
		recorder.stop();
		recorder.release();
		recorder = null;
	}


}