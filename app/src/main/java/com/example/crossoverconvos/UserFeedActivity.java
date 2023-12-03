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
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        editTextPost = findViewById(R.id.editTextPost);
        buttonPost = findViewById(R.id.buttonPost);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        recyclerViewPosts.setAdapter(postAdapter);
        buttonPost.setOnClickListener(view -> submitPost());
        fabLogout = findViewById(R.id.fabLogout);
        fabLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(UserFeedActivity.this, MainActivity.class));
            finish();
        });

        fetchPosts(); // Fetch posts when the activity is created
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetInactivityTimer();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetInactivityTimer();
    }

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
            // Handle logout button action
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        }
        // Handle other menu items if necessary
        return super.onOptionsItemSelected(item);
    }

    private void submitPost() {
        String content = editTextPost.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter text for the post", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Post newPost = new Post();
        newPost.setUserId(userId);
        newPost.setContent(content);
        newPost.setTimestamp(new Date());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .add(newPost)
                .addOnSuccessListener(documentReference -> {
                    newPost.setPostId(documentReference.getId());
                    postList.add(0, newPost);
                    postAdapter.notifyItemInserted(0);
                    editTextPost.setText("");
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add post", Toast.LENGTH_SHORT).show());
    }

    private void fetchPosts() {
        FirebaseFirestore.getInstance().collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading posts", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    postList.clear();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                            Post post = snapshot.toObject(Post.class);
                            post.setPostId(snapshot.getId());
                            postList.add(post);
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }
}
