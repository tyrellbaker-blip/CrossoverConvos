package com.example.crossoverconvos;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * This abstract class provides user management functionality.
 */
public abstract class UserManagement extends AppCompatActivity {
    protected FirebaseAuth auth = FirebaseAuth.getInstance();
    protected FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    /**
     * Creates a new user with the provided email, password, and user data.
     *
     * @param email    The email of the new user.
     * @param password The password of the new user.
     * @param userData A Map containing additional user data.
     */
    protected void createUser(String email, String password, Map<String, Object> userData) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser newUser = task.getResult().getUser();
                        if (newUser != null) {
                            firestore.collection("users").document(newUser.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> onUserCreationSuccess())
                                    .addOnFailureListener(e -> onUserCreationFailure(e.getMessage()));
                        }
                    } else {
                        onUserCreationFailure(task.getException().getMessage());
                    }
                });
    }
    /**
     * Called when a user is successfully created.
     */
    protected abstract void onUserCreationSuccess();
    /**
     * Called when there is a failure in user creation.
     *
     * @param errorMessage The error message describing the failure.
     */
    protected abstract void onUserCreationFailure(String errorMessage);
}