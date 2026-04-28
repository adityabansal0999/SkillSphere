package com.skillsphere.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.skillsphere.app.databinding.ActivityEditProfileBinding;
import com.skillsphere.app.models.User;
import com.skillsphere.app.utils.Constants;
import com.skillsphere.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private SessionManager sessionManager;
    private List<String> selectedSkills = new ArrayList<>();
    private User currentUser;
    private Uri pendingImageUri = null; // photo staged for upload

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        pendingImageUri = uri;
                        // Show preview immediately
                        binding.tvInitialsEdit.setVisibility(View.GONE);
                        binding.ivProfilePhoto.setVisibility(View.VISIBLE);
                        Glide.with(this).load(uri).circleCrop().into(binding.ivProfilePhoto);
                        Toast.makeText(this, "Photo selected — tap Update Profile to save", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance("gs://skillsphere-31c44.firebasestorage.app");
        sessionManager = SessionManager.getInstance(this);

        setupListeners();
        loadUserData();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LISTENERS
    // ─────────────────────────────────────────────────────────────────────────

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSaveProfile.setOnClickListener(v -> validateAndSave());

        // Photo change (tap image OR FAB)
        binding.ivProfilePhoto.setOnClickListener(v -> launchImagePicker());
        binding.tvInitialsEdit.setOnClickListener(v -> launchImagePicker());
        binding.fabEditPhoto.setOnClickListener(v -> launchImagePicker());

        // Skill search filter
        binding.etSearchSkills.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSkills(s.toString());
            }
        });
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOAD DATA
    // ─────────────────────────────────────────────────────────────────────────

    private void loadUserData() {
        if (auth.getCurrentUser() == null) { finish(); return; }
        String userId = auth.getCurrentUser().getUid();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSaveProfile.setEnabled(false);

        db.collection(Constants.COLLECTION_USERS).document(userId).get()
                .addOnSuccessListener(doc -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSaveProfile.setEnabled(true);
                    if (doc.exists()) {
                        currentUser = doc.toObject(User.class);
                        if (currentUser != null) {
                            currentUser.setId(doc.getId());
                            populateFields(currentUser);
                        }
                    } else {
                        // Create a dummy user object if document doesn't exist yet
                        currentUser = new User(userId, "", auth.getCurrentUser().getEmail());
                    }
                    setupSkillGroups();
                    updateCounters();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSaveProfile.setEnabled(true);
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void populateFields(User user) {
        binding.etName.setText(safeStr(user.getName()));
        binding.etEmail.setText(safeStr(user.getEmail()));
        binding.etUsername.setText(safeStr(user.getUsername()));
        binding.etBio.setText(safeStr(user.getBio()));
        binding.etPhone.setText(safeStr(user.getPhone()));
        binding.etUniversity.setText(safeStr(user.getUniversity()));
        binding.etDepartment.setText(safeStr(user.getDepartment()));
        binding.etYear.setText(safeStr(user.getYear()));
        binding.etLocation.setText(safeStr(user.getLocation()));
        binding.etLinkedin.setText(safeStr(user.getLinkedin()));

        if (user.getSkills() != null) {
            selectedSkills.clear();
            selectedSkills.addAll(user.getSkills());
        }

        // Profile photo
        if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
            binding.tvInitialsEdit.setVisibility(View.GONE);
            binding.ivProfilePhoto.setVisibility(View.VISIBLE);
            Glide.with(this).load(user.getPhotoUrl()).circleCrop()
                    .placeholder(com.skillsphere.app.R.drawable.bg_avatar_circle)
                    .into(binding.ivProfilePhoto);
        } else {
            binding.ivProfilePhoto.setVisibility(View.GONE);
            binding.tvInitialsEdit.setVisibility(View.VISIBLE);
            binding.tvInitialsEdit.setText(user.getInitials());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VALIDATE & SAVE
    // ─────────────────────────────────────────────────────────────────────────

    private void validateAndSave() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String linkedin = binding.etLinkedin.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            binding.etName.setError("Full Name is required");
            binding.etName.requestFocus();
            return;
        }
        if (name.length() < 2) {
            binding.etName.setError("Name must be at least 2 characters");
            binding.etName.requestFocus();
            return;
        }
        if (!phone.isEmpty() && !Patterns.PHONE.matcher(phone).matches()) {
            binding.etPhone.setError("Enter a valid phone number");
            binding.etPhone.requestFocus();
            return;
        }
        if (!linkedin.isEmpty() && !linkedin.startsWith("http")) {
            binding.etLinkedin.setError("Enter a valid URL (starting with http/https)");
            binding.etLinkedin.requestFocus();
            return;
        }

        showLoading(true);

        // If a new photo was selected, upload first then save
        if (pendingImageUri != null) {
            uploadPhotoThenSave(name);
        } else {
            saveProfileData(name, currentUser != null ? currentUser.getPhotoUrl() : null);
        }
    }

    private void uploadPhotoThenSave(String name) {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        StorageReference fileRef = storage.getReference()
                .child(Constants.STORAGE_PATH_PROFILE + userId + ".jpg");

        fileRef.putFile(pendingImageUri)
                .addOnSuccessListener(snap -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveProfileData(name, uri.toString()))
                        .addOnFailureListener(e -> handleSaveError("Photo upload URL failed: " + e.getMessage())))
                .addOnFailureListener(e -> handleSaveError("Photo upload failed: " + e.getMessage()));
    }

    private void saveProfileData(String name, String photoUrl) {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("username", binding.etUsername.getText().toString().trim());
        updates.put("bio", binding.etBio.getText().toString().trim());
        updates.put("phone", binding.etPhone.getText().toString().trim());
        updates.put("university", binding.etUniversity.getText().toString().trim());
        updates.put("department", binding.etDepartment.getText().toString().trim());
        updates.put("year", binding.etYear.getText().toString().trim());
        updates.put("location", binding.etLocation.getText().toString().trim());
        updates.put("linkedin", binding.etLinkedin.getText().toString().trim());
        updates.put("skills", selectedSkills);
        if (photoUrl != null) {
            updates.put("photoUrl", photoUrl);
        }

        // Use set with SetOptions.merge() instead of update() to create the document if it doesn't exist
        db.collection(Constants.COLLECTION_USERS).document(userId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(v -> {
                    // Update session cache
                    if (currentUser != null) {
                        currentUser.setName(name);
                        currentUser.setUsername(binding.etUsername.getText().toString().trim());
                        currentUser.setBio(binding.etBio.getText().toString().trim());
                        currentUser.setPhone(binding.etPhone.getText().toString().trim());
                        currentUser.setUniversity(binding.etUniversity.getText().toString().trim());
                        currentUser.setDepartment(binding.etDepartment.getText().toString().trim());
                        currentUser.setYear(binding.etYear.getText().toString().trim());
                        currentUser.setLocation(binding.etLocation.getText().toString().trim());
                        currentUser.setLinkedin(binding.etLinkedin.getText().toString().trim());
                        currentUser.setSkills(selectedSkills);
                        if (photoUrl != null) currentUser.setPhotoUrl(photoUrl);
                        sessionManager.saveUser(currentUser);
                    }

                    showLoading(false);
                    Snackbar.make(binding.getRoot(), "✅ Profile updated successfully!", Snackbar.LENGTH_SHORT).show();

                    // Small delay so the snackbar is seen, then finish
                    binding.getRoot().postDelayed(() -> {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }, 1000);
                })
                .addOnFailureListener(e -> handleSaveError("Update failed: " + e.getMessage()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SKILLS
    // ─────────────────────────────────────────────────────────────────────────

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
            Chip chip = new Chip(this);
            chip.setText(skill);
            chip.setCheckable(true);
            chip.setChecked(selectedSkills.contains(skill));
            chip.setChipBackgroundColorResource(com.skillsphere.app.R.color.chip_gray_bg);
            chip.setCheckedIconVisible(true);

            chip.setOnCheckedChangeListener((btn, isChecked) -> {
                if (isChecked) {
                    if (!selectedSkills.contains(skill)) selectedSkills.add(skill);
                } else {
                    selectedSkills.remove(skill);
                }
                updateCounters();
                // Tint selected chips green
                chip.setChipBackgroundColorResource(
                        isChecked ? com.skillsphere.app.R.color.chip_green_bg
                                : com.skillsphere.app.R.color.chip_gray_bg);
                chip.setTextColor(getResources().getColor(
                        isChecked ? com.skillsphere.app.R.color.chip_green_text
                                : com.skillsphere.app.R.color.chip_gray_text, null));
            });

            // Apply colour for pre-selected chips
            if (selectedSkills.contains(skill)) {
                chip.setChipBackgroundColorResource(com.skillsphere.app.R.color.chip_green_bg);
                chip.setTextColor(getResources().getColor(com.skillsphere.app.R.color.chip_green_text, null));
            } else {
                chip.setTextColor(getResources().getColor(com.skillsphere.app.R.color.chip_gray_text, null));
            }

            group.addView(chip);
        }
    }

    private void updateCounters() {
        binding.tvSelectionCount.setText(selectedSkills.size() + " skills selected");
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
            boolean matches = chip.getText().toString().toLowerCase(Locale.getDefault()).contains(query);
            chip.setVisibility(matches ? View.VISIBLE : View.GONE);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnSaveProfile.setEnabled(!show);
        binding.btnSaveProfile.setText(show ? "Saving…" : "Update Profile");
    }

    private void handleSaveError(String msg) {
        showLoading(false);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private String safeStr(String s) {
        return s != null ? s : "";
    }
}