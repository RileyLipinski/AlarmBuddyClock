package edu.ust.alarmbuddy.ui.record_audio;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.UserData;
import edu.ust.alarmbuddy.ui.friends.Profile;
import edu.ust.alarmbuddy.ui.friends.ProfileAdapter;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Displays user's friends in a checklist and allows user to
 * select any number of friends to send a sound to
 *
 * Tutorial: https://medium.com/@maydin/multi-and-single-selection-in-recyclerview-d29587a7dee2
 */
public class SelectFriendsFragment extends Fragment {

    private ImageButton backButton;
    private RecyclerView selectionList;
    private SelectableAdapter adapter;
    private ArrayList<Profile> profileList;

    private View root;

    public SelectFriendsFragment() {    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectFriendsFragment.
     */
    public static SelectFriendsFragment newInstance() {
        return new SelectFriendsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_select_friends, container, false);
        backButton = root.findViewById(R.id.SelectFriendsBackButton);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.remove(fm.findFragmentById(R.id.nav_host_fragment));
                ft.commit();
            }
        });
    }

    // gets friends from database and converts them to profile objects
    private Profile[] getFriends() {
        OkHttpClient client = new OkHttpClient();
        Profile[] friends = new Profile[]{};

        String username = "";
        String token = "";

        try {
            username = UserData.getString(getContext(), "username");
            token = UserData.getString(getContext(), "token");
        } catch (Exception e) {
            Log.e("Get Friends", "Could not retrieve username or token");
        }

        Request request = new Request.Builder()
                .url("https://alarmbuddy.wm.r.appspot.com/FriendsWith/" + username)
                .header("Authorization", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.i("Failure", e.toString());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.i("Response", response.toString() + " / " +response.body().string());
                final String myResponse;
                if (response.isSuccessful()) {
                    myResponse = response.body().string();
                    response.close();
                    Pattern pattern = Pattern.compile("\"([^\"]*)\"");
                    Matcher matcher = pattern.matcher(myResponse);
                    ArrayList<String> result = new ArrayList<>();

                    while (matcher.find()) {
                        result.add(matcher.group(1));
                    }
                    for (int i = 1; i < result.size(); i += 2) {
                        //nameList.add(result.get(i));
                    }
                }
            }
        });
        return new Profile[]{};
    }

}