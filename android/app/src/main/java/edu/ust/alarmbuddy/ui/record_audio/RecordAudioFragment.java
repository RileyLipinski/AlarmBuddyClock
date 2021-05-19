package edu.ust.alarmbuddy.ui.record_audio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import edu.ust.alarmbuddy.R;
import java.io.File;
import java.io.IOException;

public class RecordAudioFragment extends Fragment {

	private View root;

	private static final String LOG_TAG = "AudioRecord";
	private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
	private final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 2;

	private File audioFile;

	private Button recordButton;
	private Button playButton;
	private Button sendButton;

	private TextView timerText;
	private TextView playText;

	private MediaRecorder recorder;
	private MediaPlayer player;

	private CountDownTimer recordTimer;
	private CountDownTimer playTimer;

	private long totalSeconds = 11;
	private long intervalSeconds = 1;
	private int duration = 0;

	private boolean micPermission = false;
	private boolean mStartRecording = true;
	private boolean mStartPlaying = true;
	private boolean hasRecordedAudio = false;


	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
		@Nullable Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.fragment_record_audio, container, false);

		// where to store recorded audio file
		audioFile = new File(getContext().getExternalFilesDir(""),
			"raw_recording.3gpp");

		// set buttons
		recordButton = root.findViewById(R.id.recordButton);
		playButton = root.findViewById(R.id.playButton);
		sendButton = root.findViewById(R.id.GoToSelectFriendsButton);

		playText = root.findViewById(R.id.play_text);
		timerText = root.findViewById(R.id.timer);
		recordTimer = new CountDownTimer(totalSeconds * 1000, intervalSeconds * 1000) {
			public void onTick(long millisUntilFinished) {
				timerText.setText(
					String.format(":%02d", (totalSeconds * 1000 - millisUntilFinished) / 1000));
				duration = (int) ((totalSeconds * 1000 - millisUntilFinished) / 1000);
			}

			public void onFinish() {
				stopRecording();
				recordButton.setText("Start recording");
				createSuccessToast();
				recordTimer.cancel();
				timerText.setText(":00");
				playText.setText(String.format(":00/:%02d", duration));
			}
		};

		playTimer = new CountDownTimer(totalSeconds * 1000, intervalSeconds * 1000) {
			public void onTick(long millisUntilFinished) {
				if (duration > 0) {
					playText.setText(String
						.format(":%02d/:%02d", (totalSeconds * 1000 - millisUntilFinished) / 1000,
							duration));
					if (((totalSeconds * 1000 - millisUntilFinished) / 1000) >= duration) {
						playTimer.cancel();
						playText.setText(String.format(":%02d/:%02d", duration, duration));
						playButton.setText("Play sound");
						onPlay(false);
						mStartPlaying = true;
					}
				}
			}

			public void onFinish() {
				playButton.setText("Play sound");
				playTimer.cancel();
			}
		};

		recordButton.setText("Start recording");
		playButton.setText("Play sound");

		// request user for permission to access microphone and storage
		requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,
			Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

		// set listeners for record, play, and upload
		recordButton.setOnClickListener(v -> {
			if (mStartPlaying) {
				// only starts recording if mic can be used
				if (micPermission == true) {
					// when clicked, start or stop recording
					onRecord(mStartRecording);
					if (mStartRecording) {
						recordButton.setText("Stop recording");
					} else {
						recordButton.setText("Start recording");
						timerText.setText(":00");
						createSuccessToast();
						playText.setText(String.format(":00/:%02d", duration));
					}
					mStartRecording = !mStartRecording;
				}
			} else {
				Toast.makeText(getContext(), "You are playing a sound!", 4).show();
			}
		});

		playButton.setOnClickListener(v -> {
			// check if there is a recorded file to play
			if (mStartRecording) {
				// when clicked, start or stop playing sound
				onPlay(mStartPlaying);
				if (mStartPlaying) {
					playButton.setText("Stop playing");
					if (duration > 0) {
						playTimer.start();
					} else {
						Toast.makeText(getContext(), "You haven't recorded a sound yet!", 4).show();
					}
				} else {
					playButton.setText("Play sound");
					playTimer.cancel();
				}
				mStartPlaying = !mStartPlaying;
			} else {
				Toast.makeText(getContext(), "You are recording a sound!", 4).show();
			}
		});

		sendButton.setOnClickListener(v -> {
			if (!hasRecordedAudio) {
				Toast.makeText(getContext(), "You have not recorded a sound yet!", 8).show();
			} else {
				Intent intent = new Intent(getActivity(), SelectableActivity.class);
				startActivity(intent);
			}
		});

		return root;
	}


	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
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
					micPermission = true;
				} else {
					Toast.makeText(getActivity(), "Permissions Denied to record audio",
						Toast.LENGTH_LONG).show();
				}
				return;
			}
			case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
				if (grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					micPermission = true;
				} else {
					Toast.makeText(getActivity(), "Permissions Denied to write to storage",
						Toast.LENGTH_LONG).show();
				}
				return;
			}
		}
	}

	private void onRecord(boolean start) {
		if (start) {
			startRecording();
			recordTimer.start();
		} else {
			stopRecording();
			recordTimer.cancel();

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
			player.setDataSource(audioFile.getAbsolutePath());
		} catch (IOException e) {
			Log.e(LOG_TAG, "setDataSource() failed");
		}
		try {
			player.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
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
		recorder.setOutputFile(audioFile.getAbsolutePath());
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
		hasRecordedAudio = true;
	}

	private void createSuccessToast() {
		Toast.makeText(getContext(), "Sound saved", 4).show();
	}

}
