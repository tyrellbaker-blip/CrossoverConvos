package com.example.crossoverconvos;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, resendEmailButton; // Added button for resending email
    private TextView textViewRegister;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        buttonLogin.setOnClickListener(v -> loginUser());
        textViewRegister.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
        TextView textViewResendEmail = findViewById(R.id.textViewResendEmail);
        textViewResendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ResendVerificationActivity.class);
                startActivity(intent);
            }
        });
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
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            startActivity(new Intent(MainActivity.this, UserFeedActivity.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Please verify your email address", Toast.LENGTH_LONG).show();
                            resendEmailButton.setVisibility(View.VISIBLE); // Show the resend button if email is not verified
                        }
                    } else {
                        // Handling specific exceptions for different scenarios
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

    private void resendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(reloadTask -> {
                if (reloadTask.isSuccessful() && !user.isEmailVerified()) {
                    user.sendEmailVerification()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Confirmation message if email is sent successfully
                                    Toast.makeText(MainActivity.this, "Verification email resent. Please check your email.", Toast.LENGTH_LONG).show();
                                } else {
                                    // Error message if there was a problem sending the email
                                    Toast.makeText(MainActivity.this, "Failed to resend verification email. Please try again later.", Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    // Message if the user's email is already verified or there was an error reloading the user
                    if (user.isEmailVerified()) {
                        Toast.makeText(MainActivity.this, "Email is already verified.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error: Unable to resend verification email at this time.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            // Message if no user is signed in
            Toast.makeText(MainActivity.this, "No signed-in user found to verify.", Toast.LENGTH_LONG).show();
        }
    }
}