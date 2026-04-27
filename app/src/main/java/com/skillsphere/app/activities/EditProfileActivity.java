package com.skillsphere.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skillsphere.app.R;
import com.skillsphere.app.databinding.ActivityEditProfileBinding;
import com.skillsphere.app.models.User;
import com.skillsphere.app.utils.Constants;
import com.skillsphere.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private SessionManager sessionManager;
    private List<String> selectedSkills = new ArrayList<>();
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        sessionManager = SessionManager.getInstance(this);

        setupListeners();
        loadUserData();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveSkills.setOnClickListener(v -> saveSkillsToFirestore());
        binding.btnLogout.setOnClickListener(v -> confirmLogout());

        binding.etSearchSkills.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSkills(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout_title))
                .setMessage(getString(R.string.logout_message))
                .setPositiveButton(getString(R.string.logout), (dialog, which) -> {
                    auth.signOut();
                    sessionManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        db.collection(Constants.COLLECTION_USERS).document(userId).get()
                .addOnSuccessListener(doc -> {
                    currentUser = doc.toObject(User.class);
                    if (currentUser != null) {
                        if (currentUser.getSkills() != null) {
                            selectedSkills.addAll(currentUser.getSkills());
                        }
                        setupSkillGroups();
                        updateCounters();
                    }
                });
    }

    private void setupSkillGroups() {
        populateGroup(binding.groupLanguages, Constants.SKILLS_LANGUAGES);
        populateGroup(binding.groupFrameworks, Constants.SKILLS_FRAMEWORKS);
        populateGroup(binding.groupTools, Constants.SKILLS_TOOLS);
        populateGroup(binding.groupAiMl, Constants.SKILLS_AI_ML);
        populateGroup(binding.groupDesign, Constants.SKILLS_DESIGN);
        populateGroup(binding.groupDatabases, Constants.SKILLS_DATABASES);
    }

    private void populateGroup(ChipGroup group, List<String> skills) {
        group.removeAllViews();
        for (String skill : skills) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_skill_chip_choice, group, false);
            chip.setText(skill);
            
            boolean isSelected = selectedSkills.contains(skill);
            chip.setChecked(isSelected);
            if (isSelected) {
                chip.setText(skill + " ✓");
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedSkills.contains(skill)) selectedSkills.add(skill);
                    chip.setText(skill + " ✓");
                } else {
                    selectedSkills.remove(skill);
                    chip.setText(skill);
                }
                updateCounters();
            });
            group.addView(chip);
        }
    }

    private void updateCounters() {
        int count = selectedSkills.size();
        binding.tvSelectionCount.setText("Tap to select · " + count + " selected");
        binding.btnSaveSkills.setText("Save " + count + " Skills");
    }

    private void filterSkills(String query) {
        String q = query.toLowerCase(Locale.getDefault());
        filterGroup(binding.groupLanguages, q);
        filterGroup(binding.groupFrameworks, q);
        filterGroup(binding.groupTools, q);
        filterGroup(binding.groupAiMl, q);
        filterGroup(binding.groupDesign, q);
        filterGroup(binding.groupDatabases, q);
    }

    private void filterGroup(ChipGroup group, String query) {
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip) group.getChildAt(i);
            String text = chip.getText().toString().replace(" ✓", "").toLowerCase(Locale.getDefault());
            chip.setVisibility(text.contains(query) ? View.VISIBLE : View.GONE);
        }
    }

    private void saveSkillsToFirestore() {
        if (auth.getCurrentUser() == null) return;
        
        binding.btnSaveSkills.setEnabled(false);
        String userId = auth.getCurrentUser().getUid();

        db.collection(Constants.COLLECTION_USERS).document(userId)
                .update("skills", selectedSkills)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.btnSaveSkills.setEnabled(true);
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }
}
