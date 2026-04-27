package com.skillsphere.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

    // All available skills
    private final List<String> ALL_SKILLS = buildAllSkills();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseHelper.getAuth();
        setupListeners();
    }

    private List<String> buildAllSkills() {
        List<String> skills = new ArrayList<>();
        skills.addAll(Constants.SKILLS_LANGUAGES);
        skills.addAll(Constants.SKILLS_FRAMEWORKS);
        skills.addAll(Constants.SKILLS_TOOLS);
        skills.addAll(Constants.SKILLS_AI_ML);
        skills.addAll(Constants.SKILLS_DESIGN);
        return skills;
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCreateAccount.setOnClickListener(v -> attemptSignup());
        binding.tvLogin.setOnClickListener(v -> finish());

        // Working skill picker using AlertDialog with multi-choice
        binding.btnSelectSkills.setOnClickListener(v -> showSkillPickerDialog());
    }

    private void showSkillPickerDialog() {
        String[] skillArray = ALL_SKILLS.toArray(new String[0]);
        boolean[] checkedItems = new boolean[skillArray.length];
        for (int i = 0; i < skillArray.length; i++) {
            checkedItems[i] = selectedSkills.contains(skillArray[i]);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select Your Skills")
                .setMultiChoiceItems(skillArray, checkedItems, (dialogInterface, which, isChecked) -> {
                    if (isChecked) {
                        if (!selectedSkills.contains(skillArray[which]))
                            selectedSkills.add(skillArray[which]);
                    } else {
                        selectedSkills.remove(skillArray[which]);
                    }
                    // Update button text dynamically
                    AlertDialog d = (AlertDialog) dialogInterface;
                    d.getButton(AlertDialog.BUTTON_POSITIVE).setText("Done (" + selectedSkills.size() + " selected)");
                })
                .setPositiveButton("Done (" + selectedSkills.size() + " selected)", (d, which) -> {
                    updateSkillChips();
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void updateSkillChips() {
        binding.chipGroupSkills.removeAllViews();
        for (String skill : selectedSkills) {
            Chip chip = new Chip(this);
            chip.setText(skill);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                selectedSkills.remove(skill);
                binding.chipGroupSkills.removeView(chip);
            });
            binding.chipGroupSkills.addView(chip);
        }
    }

    private void attemptSignup() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String dept = binding.etDepartment.getText().toString().trim();
        String year = binding.etYear.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirm = binding.etConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) { binding.etName.setError(getString(R.string.error_empty_name)); return; }
        if (!FirebaseHelper.isValidEmail(email)) { binding.etEmail.setError(getString(R.string.error_invalid_email)); return; }
        if (dept.isEmpty()) { binding.etDepartment.setError(getString(R.string.error_empty_department)); return; }
        if (year.isEmpty()) { binding.etYear.setError(getString(R.string.error_empty_year)); return; }
        if (!FirebaseHelper.isValidPassword(password)) { binding.etPassword.setError(getString(R.string.error_password_short)); return; }
        if (!password.equals(confirm)) { binding.etConfirmPassword.setError(getString(R.string.error_passwords_dont_match)); return; }

        binding.progressBar.setVisibility(View.VISIBLE);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();
                    saveUserToFirestore(userId, name, email, dept, year);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserToFirestore(String userId, String name, String email, String dept, String year) {
        User user = new User(userId, name, email);
        user.setDepartment(dept);
        user.setYear(year);
        user.setSkills(selectedSkills);

        FirebaseHelper.getUserRef(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    SessionManager.getInstance(this).saveUser(user);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show();
                });
    }
}
