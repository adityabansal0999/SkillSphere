package com.skillsphere.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.skillsphere.app.R;
import com.skillsphere.app.databinding.ActivityLoginBinding;
import com.skillsphere.app.utils.FirebaseHelper;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseHelper.getAuth();

        // Login Button Click (XML ID: btn_Login)
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        // Navigation to Signup (XML ID: tvSignUp)
        binding.tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validations
        if (!FirebaseHelper.isValidEmail(email)) {
            binding.etEmail.setError(getString(R.string.error_invalid_email));
            return;
        }
        if (!FirebaseHelper.isValidPassword(password)) {
            binding.etPassword.setError(getString(R.string.error_password_short));
            return;
        }

        // Show Progress (XML ID: progressBar)
        binding.progressBar.setVisibility(View.VISIBLE);

        // Firebase Login
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(this, MainActivity.class);
                        // Clear the stack so user can't go back to login screen
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}