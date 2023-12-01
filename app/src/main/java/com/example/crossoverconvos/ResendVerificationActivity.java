package com.example.crossoverconvos;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;

public class ResendVerificationActivity extends AppCompatActivity {

    private EditText editTextResendEmail;
    private Button buttonSubmitResend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resend_verification);

        // Initialize the EditText and Button
        editTextResendEmail = findViewById(R.id.editTextResendEmail);
        buttonSubmitResend = findViewById(R.id.buttonSubmitResend);

        // Set an OnClickListener for the button
        buttonSubmitResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendConfirmationEmail();
            }
        });
    }

    private void resendConfirmationEmail() {
        String email = editTextResendEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter an email address", Toast.LENGTH_LONG).show();
            return;
        }

        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://crossoverconvos.app/login") // Your redirect URL
                .setAndroidPackageName("com.example.crossoverconvos", true, null) // Your package name
                .build();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendSignInLinkToEmail(email, actionCodeSettings)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verification email resent. Please check your email.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Failed to resend verification email. Please try again later.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
