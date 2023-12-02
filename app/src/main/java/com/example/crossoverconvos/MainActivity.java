package com.example.crossoverconvos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonLogout; // Added logout button
    private FirebaseAuth auth; // Firebase Auth instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogout = findViewById(R.id.buttonLogout);

        buttonLogin.setOnClickListener(v -> loginUser());
        buttonLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            updateUIForLoggedOutUser();
        });

        // Check authentication state when the activity is created
        checkAuthenticationState();
        updateUIBasedOnLoginStatus();
    }

    private void updateUIBasedOnLoginStatus() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // User is logged in
            buttonLogout.setVisibility(View.VISIBLE);
            // Show main content
        } else {
            // User is not logged in
            buttonLogout.setVisibility(View.GONE);
            // Show login interface
        }
    }

    private void updateUIForLoggedOutUser() {
        buttonLogout.setVisibility(View.GONE);
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter email and password", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Directly go to UserFeedActivity without checking email verification
                        startActivity(new Intent(MainActivity.this, UserFeedActivity.class));
                        finish();
                    } else {
                        // Handle login failure
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(MainActivity.this, "User not found.", Toast.LENGTH_LONG).show();
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(MainActivity.this, "Invalid credentials. Please try again.", Toast.LENGTH_LONG).show();
                        } else {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed";
                            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void logoutUser() {
        auth.signOut();
        resetToLoginScreen();
    }

    private void resetToLoginScreen() {
        // Reset UI elements to initial state for login
        editTextEmail.setText("");
        editTextPassword.setText("");
}
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        if (auth.getCurrentUser() != null) {
            // User is signed in, navigate to UserFeedActivity or appropriate screen
            navigateToUserFeed();
        } else {
            // User is not signed in, stay on this login screen
            resetToLoginScreen();
        }
    }
    private void navigateToUserFeed() {
        Intent intent = new Intent(MainActivity.this, UserFeedActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Check authentication state when the activity resumes
        checkAuthenticationState();
    }

    private void checkAuthenticationState() {
        if (auth.getCurrentUser() != null) {
            Log.d("MainActivity", "User logged in: " + auth.getCurrentUser().getUid());
            Toast.makeText(this, "User is logged in. UID: " + auth.getCurrentUser().getUid(), Toast.LENGTH_LONG).show();
        } else {
            Log.e("MainActivity", "User not logged in");
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_LONG).show();
        }
    }

}
