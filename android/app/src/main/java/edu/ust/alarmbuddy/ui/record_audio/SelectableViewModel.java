package edu.ust.alarmbuddy.ui.record_audio;

import androidx.annotation.NonNull;

public class SelectableViewModel {
    private String friendName;
    private Boolean isSelected;

    public SelectableViewModel(@NonNull final String name) {
        setFriendName(name);
        setSelectedStatus(false);
    }

    @NonNull
    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(@NonNull final String friendName) {
        this.friendName = friendName;
    }

    public Boolean getSelectedStatus() {
        return isSelected;
    }

    public void setSelectedStatus(Boolean status) {
        isSelected = status;
    }

}
