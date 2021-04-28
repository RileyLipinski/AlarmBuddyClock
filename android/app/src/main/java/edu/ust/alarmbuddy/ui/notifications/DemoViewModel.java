package edu.ust.alarmbuddy.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DemoViewModel extends ViewModel {

	private final MutableLiveData<String> mText;

	public DemoViewModel() {
		mText = new MutableLiveData<>();
		mText.setValue("This is demo fragment");
	}

	public LiveData<String> getText() {
		return mText;
	}
}