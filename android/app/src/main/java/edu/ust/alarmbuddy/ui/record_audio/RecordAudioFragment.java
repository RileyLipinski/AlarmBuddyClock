package edu.ust.alarmbuddy.ui.record_audio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.common.UserData;
import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RecordAudioFragment extends Fragment {

	private View root;
	private RecordAudioViewModel mViewModel;

	private static final String LOG_TAG = "AudioRecord";
	private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
	private final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 2;

	private File audioFile = null;
	private File formattedAudioFile = null;

	private Button recordButton = null;
	private Button playButton = null;
	private Button uploadSoundButton = null;

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
			"android_test.3gpp");

		// set buttons
		recordButton = root.findViewById(R.id.recordButton);
		playButton = root.findViewById(R.id.playButton);
		uploadSoundButton = root.findViewById(R.id.uploadButton);

		recordButton.setText("Start recording");
		playButton.setText("Play sound");

		// request user for permission to access microphone and storage
		requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,
			Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

		// set listeners for record, play, and upload
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

		View.OnClickListener playClicker = new View.OnClickListener() {
			public void onClick(View v) {
				// check if there is a recorded file to play
				if (audioFile.exists()) {
					// when clicked, start or stop playing sound
					onPlay(mStartPlaying);
					if (mStartPlaying) {
						playButton.setText("Stop playing");
					} else {
						playButton.setText("Play sound");
					}
					mStartPlaying = !mStartPlaying;
				}
			}
		};
		playButton.setOnClickListener(playClicker);

		View.OnClickListener uploadClicker = new View.OnClickListener() {
			public void onClick(View v) {
				if (audioFile.exists()) {
					uploadSound(getContext(), audioFile.getAbsolutePath());
				}
			}
		};
		uploadSoundButton.setOnClickListener(uploadClicker);

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
	}

	public void uploadSound(Context context, String sound_path) {
		/*
		try {
			sound_path = "file:///android_asset/multiply.mp3"; //"file:///edu.ust.alarmbuddy/raw/multiply";
		}
		catch (Exception e) {
			Log.e("Upload Sound", "sound path " + e);
		}*/
		if (FFmpeg.getInstance(context).isSupported()) {

			OkHttpClient client = new OkHttpClient();
			FFmpeg ffmpeg = FFmpeg.getInstance(context);


			formattedAudioFile = new File(Environment.getExternalStorageDirectory(), "a.mp3");
			try {
				String[] cmd = new String[]{"-y", "-i", sound_path, formattedAudioFile.getAbsolutePath()};
				ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
					@Override
					public void onFailure(String message) {
						Log.i("Convert to mp3", "Failure " + message);
					}

					@Override
					public void onSuccess(String message) {
						Log.i("Convert to mp3", "Success " + message);

						String mime = getMimeType(formattedAudioFile.getAbsolutePath());

						String username = "";
						String token = "";
						try {
							username = UserData.getString(context, "username");
							token = UserData.getString(context, "token");
						} catch (Exception e) {
							Log.e("Upload Sound", "Could not retrieve username or token");
						}

						RequestBody fileContent = RequestBody.create(new File(formattedAudioFile.getAbsolutePath()),
								MediaType.parse("audio/mpeg"));

						RequestBody body = new MultipartBody.Builder()
								.setType(MultipartBody.FORM)
								.addFormDataPart("file", formattedAudioFile.getName(), fileContent)
								.addFormDataPart("soundDescription", "alarm sound")
								.build();

						//RequestBody body = RequestBody.create(data, QUERYSTRING);
						Request request = new Request.Builder()
								.url("https://alarmbuddy.wm.r.appspot.com/upload/" + username)
								//.url("http://192.168.1.15:3000/upload/" + username)
								.header("Authorization", token)
								.post(body)
								.build();

						Log.i("Upload Sound", request.toString());
						try {
							Log.i("Upload Sound", fileContent.toString() + " " + fileContent.contentLength()
									+ " " + fileContent.contentType());
						}
						catch (IOException e) {
							Log.e("upload", "contentLength " + e);
						}

						client.newCall(request).enqueue(new Callback() {
							@Override
							public void onFailure(@NotNull Call call, @NotNull IOException e) {
								Log.i("Failure", e.toString());
							}

							@Override
							public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
								Log.i("Response", response.toString() + " / " +response.body().string());
							}
						});
					}


				});
			} catch (Exception e) {
				Log.e("Convert from wav to mp3", "Exception: " + e);
			}

			//String mime = getMimeType(sound_path);

			//Log.i("Upload Sound", sound_path + "  " + mime + formattedAudioFile.getName());
			Log.i("Upload Sound", sound_path + "  " + formattedAudioFile.getAbsolutePath());

		}

/*
		//execute the request and wait for a response
		final String[] stringResponse = new String[1];
		final CountDownLatch latch = new CountDownLatch(1);
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				call.cancel();
				latch.countDown();
				Log.i("call request", "onFailure");
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				stringResponse[0] = response.body().string();
				latch.countDown();
				Log.i("call request", "onResponse");
			}
		});
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


		Call call = client.newCall(request);
		try {
			Response response = call.execute();
			Log.i("Upload Sound", response.toString());
		}
		catch(IOException e) {
			Log.e("Execute Call", "IOException" + e);
		}
*/

/*
		return stringResponse[0] != null && stringResponse[0].substring(8, 12).equals("true");
*/

	}

	public static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		return type;
	}


}
