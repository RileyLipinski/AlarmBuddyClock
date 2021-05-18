package edu.ust.alarmbuddy.ui.user_information;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

public class UserInformationViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public UserInformationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the user information page");
    }
}