package edu.ust.alarmbuddy.ui.friends;

import android.os.Bundle;
import android.view.*;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import edu.ust.alarmbuddy.R;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class FriendsFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private ProfileAdapter mAdapter;
    private final RecyclerView.LayoutManager mlayoutManager;
    private ArrayList<Profile> mProfileList;

    public FriendsFragment() {
        mRecyclerView = null;
        mAdapter = null;
        mlayoutManager = new LinearLayoutManager(getContext());
        mProfileList = null;
    }

    //reads names from a txt file and then populates and returns a arraylist of profiles sorted by the names associated with them
    private void populateArray() {
        ArrayList<Profile> profileList = new ArrayList<>();
        ArrayList<String> nameList = new ArrayList<>();
        String name;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(requireContext().getAssets().open("100_common_names.txt")));
            while ((name = br.readLine()) != null) {
                nameList.add(name);
            }
            //sorts the list of names alphabetically before using them to create Profile objects
            Collections.sort(nameList, String::compareToIgnoreCase);

            //uses the sorted names to create Profile objects
            for (String s : nameList) {
                profileList.add(new Profile(R.drawable.ic_baseline_account_box, s, "details"));
            }
            br.close();
        } catch (IOException e) {
            //if a file exception is thrown, clears profileList and adds 100 default profile objects
            profileList.clear();
            for (int i = 0; i < 100; i++) {
                profileList.add(new Profile(R.drawable.ic_baseline_account_box, "DefaultName", "details"));
            }
        } finally {
            mProfileList = profileList;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_friends, container, false);

        setHasOptionsMenu(true);
        buildRecyclerView(v);

        return v;
    }

    private ArrayList<Profile> getMProfileList() {
        return this.mProfileList;
    }

    private void buildRecyclerView(View v) {
        mRecyclerView = v.findViewById(R.id.recyclerView);
        populateArray();
        mAdapter = new ProfileAdapter(getMProfileList());
        mRecyclerView.setLayoutManager(mlayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);

        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) menu.findItem(R.id.action_search).getActionView();
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