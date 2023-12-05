package com.example.crossoverconvos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.textViewCommentContent.setText(comment.getContent());
        fetchAndFormatUserName(comment.getUserId(), holder.textViewCommenterName);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewCommenterName;
        public TextView textViewCommentContent;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewCommenterName = itemView.findViewById(R.id.textViewCommenterName);
            textViewCommentContent = itemView.findViewById(R.id.textViewCommentContent);
        }
    }
    public void updateComments(List<Comment> newComments) {
        commentList.clear();
        commentList.addAll(newComments);
        notifyDataSetChanged();
    }
    private void fetchAndFormatUserName(String userId, TextView nameTextView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String firstName = task.getResult().getString("First Name");
                String lastName = task.getResult().getString("Last Name");
                assert lastName != null;
                String formattedName = firstName + " " + lastName.charAt(0) + ".";
                nameTextView.setText(formattedName);
            } else {
                nameTextView.setText("Anonymous"); // Default name in case of failure
            }
        });
    }
}
