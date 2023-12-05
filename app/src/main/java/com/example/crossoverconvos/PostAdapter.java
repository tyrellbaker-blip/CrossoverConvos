package com.example.crossoverconvos;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> postList;
    private Context context;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.textViewContent.setText(post.getContent());

        if (post.getUserId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
            holder.buttonEditPost.setVisibility(View.VISIBLE);
        } else {
            holder.buttonEditPost.setVisibility(View.GONE);
        }

        holder.buttonEditPost.setOnClickListener(v -> openEditDialog(post, position));

        holder.buttonShowComments.setOnClickListener(v -> {
            boolean isVisible = holder.layoutCommentInput.getVisibility() == View.VISIBLE;
            holder.layoutCommentInput.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            holder.recyclerViewComments.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            if (!isVisible) {
                CommentAdapter commentAdapter = new CommentAdapter(new ArrayList<>());
                holder.recyclerViewComments.setAdapter(commentAdapter);
                holder.recyclerViewComments.setLayoutManager(new LinearLayoutManager(context));
                fetchCommentsForPost(post.getPostId(), commentAdapter);
            }
        });

        holder.buttonPostComment.setOnClickListener(v -> {
            String commentText = holder.editTextWriteComment.getText().toString();
            if (!commentText.isEmpty()) {
                postComment(post.getPostId(), FirebaseAuth.getInstance().getCurrentUser().getUid(), commentText);
                holder.editTextWriteComment.setText("");
            }
        });
    }

    private void openEditDialog(Post post, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText editText = new EditText(context);
        editText.setText(post.getContent());

        builder.setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String updatedContent = editText.getText().toString();
                    updatePostInFirestore(post.getPostId(), updatedContent, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updatePostInFirestore(String postId, String updatedContent, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").document(postId)
                .update("content", updatedContent)
                .addOnSuccessListener(aVoid -> {
                    postList.get(position).setContent(updatedContent);
                    notifyItemChanged(position);
                    Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update post", Toast.LENGTH_SHORT).show());
    }

    private void fetchCommentsForPost(String postId, CommentAdapter commentAdapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("comments")
                .whereEqualTo("postID", postId)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        Comment comment = snapshot.toObject(Comment.class);
                        comments.add(comment);
                    }
                    commentAdapter.updateComments(comments);
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to load comments", Toast.LENGTH_SHORT).show());
    }

    private void postComment(String postId, String userId, String content) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("postID", postId);
        commentData.put("userID", userId);
        commentData.put("content", content);
        commentData.put("timestamp", new Timestamp(new Date()));

        db.collection("comments")
                .add(commentData)
                .addOnSuccessListener(documentReference -> Toast.makeText(context, "Comment posted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContent;
        Button buttonEditPost, buttonShowComments, buttonPostComment;
        EditText editTextWriteComment;
        RecyclerView recyclerViewComments;
        LinearLayout layoutCommentInput;

        PostViewHolder(View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            buttonEditPost = itemView.findViewById(R.id.buttonEditPost);
            buttonShowComments = itemView.findViewById(R.id.buttonShowComments);
            buttonPostComment = itemView.findViewById(R.id.buttonPostComment);
            editTextWriteComment = itemView.findViewById(R.id.editTextWriteComment);
            recyclerViewComments = itemView.findViewById(R.id.recyclerViewComments);
            layoutCommentInput = itemView.findViewById(R.id.layoutCommentInput);
        }
    }
}
