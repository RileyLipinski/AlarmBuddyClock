package edu.ust.alarmbuddy.ui.alarms;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AlarmsViewModel extends ViewModel {

	private MutableLiveData<String> mText;

	public AlarmsViewModel() {
		mText = new MutableLiveData<>();
		mText.setValue("Alarm #1");
	}

	public LiveData<String> getText() {
		return mText;
	}
}