package com.skillsphere.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.skillsphere.app.R;
import com.skillsphere.app.databinding.ActivitySignupBinding;
import com.skillsphere.app.models.User;
import com.skillsphere.app.utils.Constants;
import com.skillsphere.app.utils.FirebaseHelper;
import com.skillsphere.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth auth;
    private List<String> selectedSkills = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseHelper.getAuth();

        setupListeners();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnCreateAccount.setOnClickListener(v -> attemptSignup());

        // Placeholder for Skills Selection - In a real app, open a Dialog or BottomSheet here
        binding.btnSelectSkills.setOnClickListener(v -> {
            Toast.makeText(this, "Open Skills Selection Dialog/Activity", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to SkillSelectionActivity or open BottomSheet
        });

        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptSignup() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String dept = binding.etDepartment.getText().toString().trim();
        String year = binding.etYear.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirm = binding.etConfirmPassword.getText().toString().trim();

        // Validation
        if (name.isEmpty()) { binding.etName.setError(getString(R.string.error_empty_name)); return; }
        if (!FirebaseHelper.isValidEmail(email)) { binding.etEmail.setError(getString(R.string.error_invalid_email)); return; }
        if (dept.isEmpty()) { binding.etDepartment.setError(getString(R.string.error_empty_department)); return; }
        if (year.isEmpty()) { binding.etYear.setError(getString(R.string.error_empty_year)); return; }
        if (!FirebaseHelper.isValidPassword(password)) { binding.etPassword.setError(getString(R.string.error_password_short)); return; }
        if (!password.equals(confirm)) { binding.etConfirmPassword.setError(getString(R.string.error_passwords_dont_match)); return; }
        if (selectedSkills.size() < Constants.MIN_SKILLS_PER_USER) {
            Toast.makeText(this, getString(R.string.error_select_skills), Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Auth Creation
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();
                    saveUserToFirestore(userId, name, email, dept, year);
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveUserToFirestore(String userId, String name, String email, String dept, String year) {
        User user = new User(userId, name, email);
        user.setDepartment(dept);
        user.setYear(year);
        user.setSkills(selectedSkills);

        FirebaseHelper.getUserRef(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    SessionManager.getInstance(this).saveUser(user);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show());
    }

    // Call this method whenever a skill is added to update the UI
    private void addSkillChip(String skill) {
        if (!selectedSkills.contains(skill)) {
            selectedSkills.add(skill);
            Chip chip = new Chip(this);
            chip.setText(skill);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                binding.chipGroupSkills.removeView(chip);
                selectedSkills.remove(skill);
            });
            binding.chipGroupSkills.addView(chip);
        }
    }
}