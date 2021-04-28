package edu.ust.alarmbuddy.ui.friends;

/***
 * @author Keghan Halloran
 * creates a Profile object that contains and image and two text fields
 */
public class Profile {

	private final int mImageResource;
	private final String mText1;
	private final String mText2;

	public Profile(int imageResource, String text1, String text2) {
		mImageResource = imageResource;
		mText1 = text1;
		mText2 = text2;
	}

	public int getImageResource() {
		return mImageResource;
	}

	public String getText1() {
		return mText1;
	}

	public String getText2() {
		return mText2;
	}
}
