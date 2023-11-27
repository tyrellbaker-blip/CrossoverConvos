package com.example.crossoverconvos;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextDateOfBirth;
    private Spinner nbaTeamsSpinner, securityQuestionSpinner;
    private Button buttonRegister, togglePasswordVisibilityButton;
    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
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
                        // Registration successful
                        sendVerificationEmail();
                    } else {
                        // Registration failed
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Email sent
                            Toast.makeText(RegisterActivity.this, "Verification email sent. Please check your email.", Toast.LENGTH_LONG).show();
                            // Redirect to login or other activity as needed
                        } else {
                            // Failed to send email
                            Toast.makeText(RegisterActivity.this, "Failed to send verification email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
        securityQuestionSpinner = findViewById(R.id.security_question_spinner);
        nbaTeamsSpinner = findViewById(R.id.nbaTeamsSpinner);
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

        // Validate First Name
        if (TextUtils.isEmpty(editTextFirstName.getText().toString().trim())) {
            editTextFirstName.setError("First name is required");
            isValid = false;
        }

        // Validate Last Name
        if (TextUtils.isEmpty(editTextLastName.getText().toString().trim())) {
            editTextLastName.setError("Last name is required");
            isValid = false;
        }

        // Validate Email
        String email = editTextEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Valid email is required");
            isValid = false;
        }

        // Validate Password
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
        if (TextUtils.isEmpty(password) || !isValidPassword(password)) {
            editTextPassword.setError("Password must meet complexity requirements");
            isValid = false;
        }

        // Validate Confirm Password
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        // Add other validation logic as necessary

        return isValid;
    }

    private boolean isValidPassword(String password) {
        // Example complexity check: at least 6 characters with 3 uppercase, 2 digits, and 2 special characters
        return password.matches("(?=.*[A-Z].*[A-Z].*[A-Z])(?=.*[0-9].*[0-9])(?=.*[@#$%^&+=].*[@#$%^&+=]).{6,}");
    }
}