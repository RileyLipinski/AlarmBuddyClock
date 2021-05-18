package edu.ust.alarmbuddy.ui.user_information;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.widget.*;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.ust.alarmbuddy.LoginActivity;
import edu.ust.alarmbuddy.MainActivity;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.R;
import edu.ust.alarmbuddy.common.AlarmBuddyHttp;
import edu.ust.alarmbuddy.common.ProfilePictures;
import edu.ust.alarmbuddy.common.UserData;
import edu.ust.alarmbuddy.ui.friends.SendRequest;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInformationFragment extends Fragment {

    private UserInformationViewModel mViewModel;
    private View view;
    private int flag;
    private String passwordInput = "";

    public static UserInformationFragment newInstance() {
        return new UserInformationFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //doLogout();

        View v = inflater.inflate(R.layout.user_information_fragment, container, false);
        view = v;
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.user_information_activity);
        UserInformationViewModel viewModel = ViewModelProviders.of(this).get(UserInformationViewModel.class);
        try {
            ArrayList<String> result = requestUserInformation(getActivity().getApplicationContext());
            final ImageView profilePic = v.findViewById(R.id.profilePic);
            profilePic.setImageBitmap(ProfilePictures.getProfilePic(getActivity().getApplicationContext(), result.get(0)));
            final TextView username = v.findViewById(R.id.username);
            username.setText("Username: " + result.get(0));
            final TextView firstName = v.findViewById(R.id.first_name);
            firstName.setText("First Name: " + result.get(2));
            final TextView lastName = v.findViewById(R.id.last_name);
            lastName.setText("Last Name: " + result.get(3));
            final TextView email = v.findViewById(R.id.email);
            email.setText("Email: " + result.get(4));
            final TextView phoneNumber = v.findViewById(R.id.phone_number);
            phoneNumber.setText("Phone Number: " + result.get(5));
            final TextView birthday = v.findViewById(R.id.birthday);
            birthday.setText("Birthday: " + result.get(7).split("T")[0]);
            final TextView createdDate = v.findViewById(R.id.created_date);
            createdDate.setText("Account Creation Date: " + result.get(6).split("T")[0]);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final Button logoutButton = v.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(view -> doLogout());

        final Button deleteAccountButton = v.findViewById(R.id.delete_account_button);
        deleteAccountButton.setOnClickListener(view -> {
            try {
                showAlert(view);
                //deleteAccount();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        return v;
        //return inflater.inflate(R.layout.user_information_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(UserInformationViewModel.class);
        // TODO: Use the ViewModel
    }

    public ArrayList<String> requestUserInformation(Context context) throws InterruptedException {

        OkHttpClient client = new OkHttpClient();
        ArrayList<String> infoList = new ArrayList<>();

        String token = "";
        try {
            token = UserData.getString(context, "token");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String username = "";
        try {
            username = UserData.getString(context, "username");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .url("https://alarmbuddy-312620.uc.r.appspot.com/users/" + username)
                .header("Authorization", token)
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
                countDownLatch.countDown();
                Log.e("Get User Information", "Failure " + e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response)
                    throws IOException {
                final String myResponse;
                if (response.isSuccessful()) {
                    myResponse = response.body().string();
                    response.close();
                    ArrayList<String> result = new ArrayList<>();
                    Pattern pattern = Pattern.compile("\"(.*?)\"");
                    Matcher matcher = pattern.matcher(myResponse);

                    while (matcher.find()) {
                        result.add(matcher.group(1));
                    }
                    for (int i = 2; i <= result.size(); i += 2) {
                        infoList.add(result.get(i));
                    }

                } else {
                    infoList.add("ElseResponse");
                }
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();

        return infoList;
    }

    public void doLogout() {
        try {
            UserData.clearSharedPreferences(getContext());
            Intent logoutIntent = new Intent(getContext(), LoginActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("event", "logout");
            logoutIntent.replaceExtras(bundle);
            startActivity(logoutIntent);
        } catch (GeneralSecurityException | IOException e) {
            Toast.makeText(getContext(), "LOGOUT FAILED", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private boolean authenticateDeleteAccount(String username, String password)
            throws IOException {
        //build the request
        String data = "username=" + username + "&password=" + password;
        URL url = new URL(AlarmBuddyHttp.API_URL + "/login");
        RequestBody body = RequestBody.create(data, MediaType
                .parse("application/x-www-form-urlencoded"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        //execute the request and wait for a response
        final String[] stringResponse = new String[1];
        final CountDownLatch latch = new CountDownLatch(1);
        AlarmBuddyHttp.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                latch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                stringResponse[0] = response.body().string();
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return stringResponse[0] != null && stringResponse[0].substring(8, 12).equals("true");
    }

    private void deleteAccount() throws InterruptedException {
        OkHttpClient client = new OkHttpClient();

        String token = UserData.getStringNotNull(getActivity().getApplicationContext(), "token");
        String username = UserData.getString(getActivity().getApplicationContext(), "username");

        String url =
                AlarmBuddyHttp.API_URL + "/deleteAccount/" + username;
        Log.i(SendRequest.class.getName(), "URL: " + url);

        Request request = new Request.Builder()
                .delete(RequestBody.create("", MediaType.parse("text/plain")))
                .url(url)
                .header("Authorization", token)
                .build();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response)
                    throws IOException {
                Log.i(SendRequest.class.getName(), "Code: " + response.code());
                Log.i(SendRequest.class.getName(), "Message: " + response.body().string());
                if (response.isSuccessful()) {
                    flag = 1;
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();

        if (flag == 1) {
            showToast("Account successfully deleted");
            doLogout();
        }
        else {
            showToast("Account could not be deleted");
        }
        flag = 0;
    }

    private void showToast(String input) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater
                .inflate(R.layout.friend_request_toast, view.findViewById(R.id.toast_root));

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(input);

        Toast toast = new Toast(getActivity().getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 175);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private void showAlert(View v) {
        final EditText input = new EditText(view.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        final AlertDialog.Builder verifyDelete = new AlertDialog.Builder(view.getContext());
        verifyDelete.setTitle("Enter password to delete account, this cannot be undone.");
        verifyDelete.setView(input);

        //Resource: https://stackoverflow.com/questions/10903754/input-text-dialog-android
        verifyDelete.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                passwordInput = input.getText().toString();
                String username = UserData.getString(getContext(), "username");
                try {
                    if (authenticateDeleteAccount(username, passwordInput)) {
                        deleteAccount();
                    }
                    else {
                        showToast("Incorrect Password");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        verifyDelete.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        verifyDelete.show();
    }
}