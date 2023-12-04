package com.example.crossoverconvos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonAdminLogin;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonAdminLogin = findViewById(R.id.buttonAdminLogin);

        buttonLogin.setOnClickListener(v -> loginUser());
        buttonAdminLogin.setOnClickListener(v -> showAdminLoginDialog());

        TextView textViewRegister = findViewById(R.id.textViewRegister);
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        checkAuthenticationState();
    }


    private void showAdminLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.admin_dialog, null);
        builder.setView(dialogView);

        final EditText editTextAdminEmail = dialogView.findViewById(R.id.editTextAdminEmail);
        final EditText editTextAdminPassword = dialogView.findViewById(R.id.editTextAdminPassword);

        builder.setPositiveButton("Login", (dialog, which) -> {
            String email = editTextAdminEmail.getText().toString().trim();
            String password = editTextAdminPassword.getText().toString().trim();
            authenticateAdmin(email, password);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void authenticateAdmin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        if (FirebaseAuth.getInstance().getCurrentUser() != null &&
                                FirebaseAuth.getInstance().getCurrentUser().getUid().equals("4FRGkuFpHhVVaKSXitxm407myR92")) {
                            Intent intent = new Intent(MainActivity.this, AdminLandingPageActivity.class);
                            startActivity(intent);
                        } else {

                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(MainActivity.this, "Unauthorized access.", Toast.LENGTH_LONG).show();
                        }
                    } else {

                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_LONG).show();
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
                        startActivity(new Intent(MainActivity.this, UserFeedActivity.class));
                        finish();
                    } else {
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

    @Override
    protected void onStart() {
        super.onStart();
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