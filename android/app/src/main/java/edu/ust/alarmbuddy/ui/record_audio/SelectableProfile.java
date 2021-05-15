package edu.ust.alarmbuddy.ui.record_audio;

import edu.ust.alarmbuddy.ui.friends.Profile;

/**
 * Profile wrapper class that allows profile to be selected/deselected
 */
public class SelectableProfile extends Profile {

    private boolean isSelected = false;

    public SelectableProfile(Profile profile) {
        super(profile.getImageResource(), profile.getText1(), profile.getText2());
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }


}
