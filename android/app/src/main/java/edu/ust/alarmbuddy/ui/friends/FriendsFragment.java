package edu.ust.alarmbuddy.ui.friends;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class FriendsFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private ProfileAdapter mAdapter;
    private final RecyclerView.LayoutManager mlayoutManager;
    private ArrayList<Profile> mProfileList;
    private Button button;

    public FriendsFragment() {
        mRecyclerView = null;
        mAdapter = null;
        mlayoutManager = new LinearLayoutManager(getContext());
        mProfileList = null;
        button = null;
    }

    //gets a list of friend names from the database and then populates and returns a arraylist of profiles sorted by the names associated with them
    private void populateArray() throws InterruptedException {
        ArrayList<Profile> profileList = new ArrayList<>();
        ArrayList<String> nameList = new ArrayList<>();

        for (int i=0; i<20; i++)
        {
            nameList.add("Default");
        }


        getRequest(nameList);

        //sorts the list of names alphabetically before using them to create Profile objects
        Collections.sort(nameList, String::compareToIgnoreCase);

        //uses the sorted names to create Profile objects
        for (String s : nameList) {
            profileList.add(new Profile(R.drawable.ic_baseline_account_box, s, "details")); }

        mProfileList = profileList;
}

    public static void getRequest(ArrayList<String> nameList) throws InterruptedException {

        OkHttpClient client = new OkHttpClient();
        String token ="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IktIYW5kcm9pZCIsImlhdCI6MTYxODExMDQ1MiwiZXhwIjoxNjE4MTk2ODUyfQ.QDDyk9yQgGvGkl1gdND-MpsR8bBCHEagsTadwznOjNw";
        Request request = new Request.Builder()
                //{"auth":true,"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IktIYW5kcm9pZCIsImlhdCI6MTYxODExMDQ1MiwiZXhwIjoxNjE4MTk2ODUyfQ.QDDyk9yQgGvGkl1gdND-MpsR8bBCHEagsTadwznOjNw"}
                .url("https://alarmbuddy.wm.r.appspot.com/FriendsWith/KHandroid")
                .header("Authorization",token)
                .build();

        //insures that the get request is completed before the code continues
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                nameList.add("Failure");
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()){
                    final String myResponse = response.body().string();
                    nameList.add(myResponse);
                }
                else {
                    nameList.add("ElseResponse");
                }
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)  {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_friends, container, false);

        buildButton(v);

        setHasOptionsMenu(true);
        try {
            buildRecyclerView(v);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return v;
    }

    private ArrayList<Profile> getMProfileList(){
        return this.mProfileList;
    }

    private void buildButton(View v){
        button = v.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSendRequest();
            }
        });
    }

    public void openSendRequest(){
        Intent intent = new Intent(getActivity(), SendRequest.class);
        startActivity(intent);
    }

    private void buildRecyclerView(View v) throws InterruptedException {
        mRecyclerView = v.findViewById(R.id.recyclerView);
        populateArray();
        mAdapter = new ProfileAdapter(getMProfileList());
        mRecyclerView.setLayoutManager(mlayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.search_menu, menu);

        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

}