package com.example.crossoverconvos;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextDateOfBirth, editTextPhoneNumber, editTextSecurityAnswer;
    private Spinner nbaTeamsSpinner, securityQuestionSpinner;
    private CheckBox checkBoxEmailConsent;
    private Button buttonRegister, togglePasswordVisibilityButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initUI();
        populateNBATeamsSpinner();
        populateSecurityQuestions();

        buttonRegister.setOnClickListener(v -> {
            if (validateInput()) {
                registerUser();
            }
        });

        togglePasswordVisibilityButton.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserData();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String firstName = editTextFirstName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();
            String dateOfBirth = editTextDateOfBirth.getText().toString().trim();
            String favoriteTeam = nbaTeamsSpinner.getSelectedItem().toString();
            String securityQuestion = securityQuestionSpinner.getSelectedItem().toString();
            String securityAnswer = editTextSecurityAnswer.getText().toString().trim();

            Map<String, Object> userData = new HashMap<>();
            userData.put("firstName", firstName);
            userData.put("lastName", lastName);
            userData.put("dateOfBirth", dateOfBirth);
            userData.put("favoriteTeam", favoriteTeam);
            userData.put("securityQuestion", securityQuestion);
            userData.put("securityAnswer", securityAnswer);

            db.collection("users").document(userId).set(userData)
                    .addOnSuccessListener(aVoid -> {
                        sendVerificationEmail();
                    })
                    .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show());
        }
    }

    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Verification email sent. Please check your email.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void initUI() {
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth);
        editTextSecurityAnswer = findViewById(R.id.editTextSecurityAnswer);
        securityQuestionSpinner = findViewById(R.id.security_question_spinner);
        nbaTeamsSpinner = findViewById(R.id.nbaTeamsSpinner);
        checkBoxEmailConsent = findViewById(R.id.checkBoxEmailConsent);
        buttonRegister = findViewById(R.id.buttonRegister);
        togglePasswordVisibilityButton = findViewById(R.id.toggle_password_visibility);
    }

    private void populateNBATeamsSpinner() {
        String[] nbaTeams = getResources().getStringArray(R.array.nba_teams);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nbaTeams);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nbaTeamsSpinner.setAdapter(adapter);
    }

    private void populateSecurityQuestions() {
        String[] securityQuestions = getResources().getStringArray(R.array.security_questions);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, securityQuestions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        securityQuestionSpinner.setAdapter(adapter);
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            editTextPassword.setInputType(129); // 129 refers to "textPassword" type
            editTextConfirmPassword.setInputType(129);
            togglePasswordVisibilityButton.setText(R.string.show_password);
            isPasswordVisible = false;
        } else {
            editTextPassword.setInputType(145); // 145 refers to "textVisiblePassword" type
            editTextConfirmPassword.setInputType(145);
            togglePasswordVisibilityButton.setText(R.string.hide_password);
            isPasswordVisible = true;
        }
    }

    private boolean validateInput() {
        boolean isValid = true;

        if (TextUtils.isEmpty(editTextFirstName.getText().toString().trim())) {
            editTextFirstName.setError("First name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(editTextLastName.getText().toString().trim())) {
            editTextLastName.setError("Last name is required");
            isValid = false;
        }

        String email = editTextEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Valid email is required");
            isValid = false;
        }

        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
        if (TextUtils.isEmpty(password) || !isValidPassword(password)) {
            editTextPassword.setError("Password must meet complexity requirements");
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        return isValid && checkBoxEmailConsent.isChecked() && checkBoxTextConsent.isChecked();
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
}