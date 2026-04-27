package com.skillsphere.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillsphere.app.R;
import com.skillsphere.app.databinding.ActivityLoginBinding;
import com.skillsphere.app.models.User;
import com.skillsphere.app.utils.Constants;
import com.skillsphere.app.utils.FirebaseHelper;
import com.skillsphere.app.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseHelper.getAuth();
        db = FirebaseFirestore.getInstance();

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvSignUp.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (!FirebaseHelper.isValidEmail(email)) {
            binding.etEmail.setError(getString(R.string.error_invalid_email));
            return;
        }
        if (!FirebaseHelper.isValidPassword(password)) {
            binding.etPassword.setError(getString(R.string.error_password_short));
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        String userId = auth.getCurrentUser().getUid();
                        // Load user data into SessionManager for name display
                        db.collection(Constants.COLLECTION_USERS).document(userId).get()
                                .addOnSuccessListener(doc -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    User user = doc.toObject(User.class);
                                    if (user != null) {
                                        user.setId(userId);
                                        SessionManager.getInstance(this).saveUser(user);
                                    }
                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    // Still navigate even if user doc fetch fails
                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                });
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
