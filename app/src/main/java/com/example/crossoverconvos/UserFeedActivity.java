package com.example.crossoverconvos;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

        fetchPosts();
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
                    postList.add(0, newPost); // Add at the top of the list
                    postAdapter.notifyItemInserted(0);
                    editTextPost.setText(""); // Clear the input field
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
                            post.setPostId(snapshot.getId()); // Set the post ID
                            postList.add(post);
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }
}