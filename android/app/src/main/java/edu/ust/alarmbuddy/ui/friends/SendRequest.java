package edu.ust.alarmbuddy.ui.friends;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import edu.ust.alarmbuddy.R;

/***
 * @author Keghan Halloran
 * This activity provides the framework for sending friend requests
 * it currently does nothing other than displaying a message to the user
 * since we do not have the ability to add to a users friends list
 * or send a user a friend request yet. Implementing the intended usage
 * is dependant on further collaboration with the database team.
 */
public class SendRequest extends AppCompatActivity {
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_request);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Friend Request");
        actionBar.setDisplayHomeAsUpEnabled(true);

        button = findViewById(R.id.button);
        button.setOnClickListener(v -> showToast());


    }

    private void showToast(){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.friend_request_toast, findViewById(R.id.toast_root));
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0,0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    //allows the back arrow at the top of this activity to go back to the Friends Fragment instead of a parent activity
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == android.R.id.home){
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}