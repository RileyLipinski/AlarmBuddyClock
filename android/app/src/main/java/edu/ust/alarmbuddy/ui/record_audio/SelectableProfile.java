package edu.ust.alarmbuddy.ui.record_audio;

import edu.ust.alarmbuddy.ui.friends.Profile;

public class SelectableProfile extends Profile {
    private Boolean isSelected;

    // no image resource
    public SelectableProfile (String name) {
        super(-1, name, "");
        isSelected = false;
    }

    public Boolean isSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

}
