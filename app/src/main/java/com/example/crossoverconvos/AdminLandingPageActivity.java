package com.example.crossoverconvos;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminLandingPageActivity extends UserManagement {
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_landing_page);

        ImageView imageViewAddUser = findViewById(R.id.imageViewAddUser);
        ImageView imageViewDeleteUser = findViewById(R.id.imageViewDeleteUser);
        FloatingActionButton fabAdminLogout = findViewById(R.id.fabAdminLogout);

        imageViewAddUser.setOnClickListener(v -> openAddUserDialog());
        imageViewDeleteUser.setOnClickListener(v -> openDeleteUserDialog());
        fabAdminLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminLandingPageActivity.this, MainActivity.class));
            finish();
        });
    }

    private void openAddUserDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.content_register, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        final EditText editTextFirstName = dialogView.findViewById(R.id.editTextFirstName);
        final EditText editTextLastName = dialogView.findViewById(R.id.editTextLastName);
        final EditText editTextEmail = dialogView.findViewById(R.id.editTextEmail);
        final EditText editTextPassword = dialogView.findViewById(R.id.editTextPassword);
        final EditText editTextConfirmPassword = dialogView.findViewById(R.id.editTextConfirmPassword);
        final EditText editTextDateOfBirth = dialogView.findViewById(R.id.editTextDateOfBirth);
        final EditText editTextSecurityAnswer = dialogView.findViewById(R.id.editTextSecurityAnswer);
        final Spinner nbaTeamsSpinner = dialogView.findViewById(R.id.nbaTeamsSpinner);
        final Spinner securityQuestionSpinner = dialogView.findViewById(R.id.security_question_spinner);
        final Button togglePasswordVisibilityButton = dialogView.findViewById(R.id.toggle_password_visibility);

        populateNBATeamsSpinner(nbaTeamsSpinner);
        populateSecurityQuestions(securityQuestionSpinner);
        togglePasswordVisibilityButton.setOnClickListener(v -> togglePasswordVisibility(editTextPassword, editTextConfirmPassword, togglePasswordVisibilityButton));

        builder.setPositiveButton("Register", (dialog, which) -> {
            if (validateInput(editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextConfirmPassword)) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                Map<String, Object> userData = new HashMap<>();
                userData.put("firstName", editTextFirstName.getText().toString());
                userData.put("lastName", editTextLastName.getText().toString());
                userData.put("dateOfBirth", editTextDateOfBirth.getText().toString());
                userData.put("favoriteTeam", nbaTeamsSpinner.getSelectedItem().toString());
                userData.put("securityQuestion", securityQuestionSpinner.getSelectedItem().toString());
                userData.put("securityAnswer", editTextSecurityAnswer.getText().toString().trim());
                addUserWithCloudFunction(email, password, userData);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void togglePasswordVisibility(EditText password, EditText confirmPassword, Button toggleButton) {
        if (isPasswordVisible) {
            password.setInputType(129);
            confirmPassword.setInputType(129);
            toggleButton.setText(R.string.show_password);
            isPasswordVisible = false;
        } else {
            password.setInputType(145);
            confirmPassword.setInputType(145);
            toggleButton.setText(R.string.hide_password);
            isPasswordVisible = true;
        }
    }

    private boolean validateInput(EditText firstName, EditText lastName, EditText email, EditText password, EditText confirmPassword) {
        boolean isValid = true;
        if (TextUtils.isEmpty(firstName.getText().toString().trim())) {
            firstName.setError("First name is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(lastName.getText().toString().trim())) {
            lastName.setError("Last name is required");
            isValid = false;
        }
        String emailText = email.getText().toString().trim();
        if (TextUtils.isEmpty(emailText) || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            email.setError("Valid email is required");
            isValid = false;
        }
        String passwordText = password.getText().toString();
        if (TextUtils.isEmpty(passwordText) || !isValidPassword(passwordText)) {
            password.setError("Password must meet complexity requirements");
            isValid = false;
        }
        if (!passwordText.equals(confirmPassword.getText().toString())) {
            confirmPassword.setError("Passwords do not match");
            isValid = false;
        }
        return isValid;
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            Toast.makeText(this, "Password must contain at least one uppercase letter", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.matches(".*[a-z].*")) {
            Toast.makeText(this, "Password must contain at least one lowercase letter", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            Toast.makeText(this, "Password must contain at least one digit", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.matches(".*[@#$%^&+=!].*")) {
            Toast.makeText(this, "Password must contain at least one special character (@#$%^&+=!)", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void populateNBATeamsSpinner(Spinner nbaTeamsSpinner) {
        String[] nbaTeams = getResources().getStringArray(R.array.nba_teams);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nbaTeams);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nbaTeamsSpinner.setAdapter(adapter);
    }

    private void populateSecurityQuestions(Spinner securityQuestionSpinner) {
        String[] securityQuestions = getResources().getStringArray(R.array.security_questions);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, securityQuestions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        securityQuestionSpinner.setAdapter(adapter);
    }

    private void openDeleteUserDialog() {
        final EditText editTextUserEmail = new EditText(this);
        editTextUserEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editTextUserEmail.setHint("User Email");
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setView(editTextUserEmail)
                .setPositiveButton("Delete", (dialog, which) -> {
                    String email = editTextUserEmail.getText().toString();
                    deleteUser(email);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void deleteUser(String email) {
        // Prepare data to send to the Cloud Function
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);

        // Call the Cloud Function for deleting users
        FirebaseFunctions.getInstance()
                .getHttpsCallable("deleteUser")
                .call(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminLandingPageActivity.this, "User successfully deleted", Toast.LENGTH_LONG).show();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Failed to delete user";
                        Toast.makeText(AdminLandingPageActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addUserWithCloudFunction(String email, String password, Map<String, Object> userData) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("password", password);
        data.putAll(userData); // Include all userData fields in the data map

        FirebaseFunctions.getInstance()
                .getHttpsCallable("createUser")
                .call(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AdminLandingPageActivity.this, "User successfully created", Toast.LENGTH_LONG).show();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Failed to create user";
                        Toast.makeText(AdminLandingPageActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onUserCreationSuccess() {
        Toast.makeText(this, "User successfully created", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onUserCreationFailure(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}

