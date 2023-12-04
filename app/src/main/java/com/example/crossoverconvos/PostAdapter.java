package com.example.crossoverconvos;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

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

        if (post.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            holder.buttonEditPost.setVisibility(View.VISIBLE);
        } else {
            holder.buttonEditPost.setVisibility(View.GONE);
        }

        holder.buttonEditPost.setOnClickListener(v -> openEditDialog(post, position));
    }

    private void openEditDialog(Post post, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText editText = new EditText(context);
        editText.setText(post.getContent());

        builder.setView(editText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String updatedContent = editText.getText().toString();
                        updatePostInFirestore(post.getPostId(), updatedContent, position);
                    }
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
                })
                .addOnFailureListener(e -> {
                    // Handle failure (e.g., show a toast message)
                });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView textViewContent;
        Button buttonEditPost;

        PostViewHolder(View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            buttonEditPost = itemView.findViewById(R.id.buttonEditPost);
        }
    }
}