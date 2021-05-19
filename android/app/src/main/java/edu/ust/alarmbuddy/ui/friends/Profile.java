package edu.ust.alarmbuddy.ui.friends;

import android.graphics.Bitmap;

/***
 * @author Keghan Halloran
 * creates a Profile object that contains and image and a text field.
 * contains an overloaded constructor to allow for both Bitmaps and
 * drawable resources.
 */
public class Profile {

	private final int mImageResource;
	private final Bitmap picture;
	private String mText1;

	public Profile(int imageResource, String text1) {
		mImageResource = imageResource;
		mText1 = text1;
		picture = null;
	}

	public Profile(Bitmap PFP, String text1) {
		picture = PFP;
		mText1 = text1;
		mImageResource = 0;
	}

	public int getImageResource() {
		return mImageResource;
	}

	public String getText1() {
		return mText1;
	}


	public Bitmap getPicture() {
		return picture;
	}

	public void setText1(String name) {
		mText1 = name;
	}
}
