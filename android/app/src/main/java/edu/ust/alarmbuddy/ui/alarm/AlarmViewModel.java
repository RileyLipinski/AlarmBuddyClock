package edu.ust.alarmbuddy.ui.alarm;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.jetbrains.annotations.NotNull;

public class AlarmViewModel extends AndroidViewModel {


    public AlarmViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    /*private final MutableLiveData<String> mText;

    public AlarmViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is alarm fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }*/

    public void insert(Alarm alarm) {
    }
}