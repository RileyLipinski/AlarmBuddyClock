package edu.ust.alarmbuddy.ui.record_audio;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.ust.alarmbuddy.R;

public class RecordAudioFragment extends Fragment {

    private RecordAudioViewModel mViewModel;

    public static RecordAudioFragment newInstance() {
        return new RecordAudioFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record_audio, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(RecordAudioViewModel.class);
        // TODO: Use the ViewModel
    }

}