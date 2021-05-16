package edu.ust.alarmbuddy.common;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import edu.ust.alarmbuddy.ui.friends.Profile;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfilePictures {

    /**
     * Gets the profile picture of the given user.
     *
     * @param context Application context
     * @param username The username of the user you wish to retrieve the profile picture of.
     *                 Must be friends with the given user.
     *
     * @return The profile picture as a bitmap image.
     */
    public static Bitmap getProfilePic(Context context, String username) {
        // image stored at /data/data/yourapp/app_data/app_imageDir/username.png
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File myFile = new File(directory, username + ".png");
        Bitmap bitmap = null;

        // look for existing image
        File file = new File(directory,username + ".png");
        if(!file.exists()){
            Log.i("Picture exists locally", "false");
            // get image from database
            if (getPicFromDatabase(username, context) != null) {
                try {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(myFile));
                }
                catch (FileNotFoundException e) {
                    Log.e("Error getting image as bitmap", e.toString());
                }
            }
        }
        else{
            Log.i("Picture exists locally", "true");
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(myFile));
            }
            catch (FileNotFoundException e) {
                Log.e("Error getting image as bitmap", e.toString());
            }
        }

        return bitmap;
    }

    /**
     * Get the path to the given user's profile picture.
     *
     * @param context Application context
     * @param username The username of the user you wish to retrieve the profile picture of.
     *                 Must be friends with the given user.
     *
     * @return The absolute path to the profile picture.
     */
    public static String getProfilePicPath(Context context, String username) {
        // image stored at /data/data/yourapp/app_data/app_imageDir/username.png
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File myFile = new File(directory, username + ".png");

        // look for existing image
        File file = new File(directory,username + ".png");
        if(!file.exists()){
            Log.i("Picture exists locally", "false");
            // get image from database
            getPicFromDatabase(username, context);
        }
        else{
            Log.i("Picture exists locally", "true");
        }

        return myFile.getAbsolutePath();
    }

    // downloads image from database and returns the path to the image
    private static String getPicFromDatabase(String username, Context context) {
        OkHttpClient client = new OkHttpClient();
        String myUsername = UserData.getString(context, "username");
        String token = UserData.getString(context, "token");
        final Bitmap[] bitmap = new Bitmap[1];
        File directory = context.getDir("imageDir", Context.MODE_PRIVATE);
        File myFile = new File(directory,username + ".png");

        Request request = new Request.Builder()
                .url(AlarmBuddyHttp.API_URL + "/getProfilePicture/" + myUsername + "/" + username)
                .header("Authorization", token)
                .get()
                .build();

        //execute the request and wait for a response
        final CountDownLatch latch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.e("Retrieve Profile Picture", "Failure " + e);
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i("Retrieve Profile Picture", "Success");
                    bitmap[0] = BitmapFactory.decodeStream(response.body().byteStream());
                    bitmap[0].compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(myFile));
                    latch.countDown();

                }
                else {
                    Log.e("Retrieve Profile Picture", "Failure " + response.toString());
                }
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return myFile.getAbsolutePath();
    }


}

