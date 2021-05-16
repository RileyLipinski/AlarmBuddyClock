package edu.ust.alarmbuddy.ui.friends;

import android.graphics.Bitmap;

/***
 * @author Keghan Halloran
 * creates a Profile object that contains and image and two text fields
 */
public class Profile {

	private final int mImageResource;
	private final Bitmap picture;
	private String mText1;
	private final String mText2;

	public Profile(int imageResource, String text1, String text2) {
		mImageResource = imageResource;
		mText1 = text1;
		mText2 = text2;
		picture = null;
	}

	public Profile (Bitmap PFP, String text1, String text2){
		picture = PFP;
		mText1 = text1;
		mText2 = text2;
		mImageResource = 0;
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

	public Bitmap getPicture(){
		return picture;
	}

	public void setText1(String name) {
		mText1 = name;
	}
}
