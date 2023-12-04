package com.example.crossoverconvos;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Activity class for displaying and managing a user feed.
 * This activity allows users to view posts, create new posts, and handle user inactivity.
 * It includes functionalities for posting to and fetching from Firebase Firestore.
 */
public class UserFeedActivity extends AppCompatActivity {
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private EditText editTextPost;
    private Button buttonPost;
    private Handler inactivityHandler = new Handler();
    private FloatingActionButton fabLogout;
    private Runnable inactivityRunnable = new Runnable() {
        @Override
        public void run() {
            // Handle user inactivity
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(UserFeedActivity.this, MainActivity.class));
            finish();
        }
    };
    private static final long INACTIVITY_TIMEOUT = 10 * 60 * 1000; // 10 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);
        // Initialization of UI components and setting up listeners
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetInactivityTimer();
    }

    /**
     * Overrides the onUserInteraction method to reset the inactivity timer.
     */
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetInactivityTimer();
    }

    /**
     * Resets the timer for user inactivity. If the user is inactive for a defined period,
     * the app will log them out automatically.
     */
    private void resetInactivityTimer() {
        inactivityHandler.removeCallbacks(inactivityRunnable);
        inactivityHandler.postDelayed(inactivityRunnable, INACTIVITY_TIMEOUT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_logout) {
            // Logout functionality
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Submits a new post to the Firebase Firestore database.
     * Validates the post content before submission.
     */
    private void submitPost() {
        String content = editTextPost.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter text for the post", Toast.LENGTH_SHORT).show();
            return;
        }
        // Post submission logic
    }

    /**
     * Fetches posts from the Firebase Firestore database and updates the RecyclerView.
     * Handles errors and updates UI accordingly.
     */
    private void fetchPosts() {
        FirebaseFirestore.getInstance().collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    // Handling Firestore data and UI updates
                });
    }
}
