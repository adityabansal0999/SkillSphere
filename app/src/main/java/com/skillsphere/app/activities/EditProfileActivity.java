package com.skillsphere.app.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.skillsphere.app.R;
import com.skillsphere.app.databinding.ActivityEditProfileBinding;
import com.skillsphere.app.models.User;
import com.skillsphere.app.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private ActivityEditProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Uri pendingImageUri = null;
    private List<String> selectedSkills = new ArrayList<>();
    private User currentUser;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) launchImagePicker();
                else Toast.makeText(this, "Gallery permission required", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    pendingImageUri = result.getData().getData();
                    updateProfileImageUI(pendingImageUri.toString(), true);
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

        setupUI();
        loadUserData();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.fabEditPhoto.setOnClickListener(v -> checkPermissionAndPick());
        binding.btnSaveProfile.setOnClickListener(v -> startUpdateFlow());

        // Search logic
        binding.etSearchSkills.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSkills(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        populateAllSkillGroups();
    }

    private void loadUserData() {
        if (auth.getCurrentUser() == null) return;
        showLoading(true);

        db.collection(Constants.COLLECTION_USERS)
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            populateFields(currentUser);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void populateFields(User user) {
        binding.etName.setText(user.getName());
        binding.etEmail.setText(user.getEmail());
        binding.etUsername.setText(user.getUsername());
        binding.etPhone.setText(user.getPhone());
        binding.etBio.setText(user.getBio());
        binding.etUniversity.setText(user.getUniversity());
        binding.etDepartment.setText(user.getDepartment());
        binding.etYear.setText(user.getYear());
        binding.etLocation.setText(user.getLocation());
        binding.etLinkedin.setText(user.getLinkedin());

        updateProfileImageUI(user.getPhotoUrl(), false);

        if (user.getSkills() != null) {
            selectedSkills = new ArrayList<>(user.getSkills());
            updateSkillSelectionUI();
        }
    }

    private void updateProfileImageUI(String urlOrUri, boolean isUri) {
        if (urlOrUri != null && !urlOrUri.isEmpty()) {
            binding.ivProfilePhoto.setVisibility(View.VISIBLE);
            binding.tvInitialsEdit.setVisibility(View.GONE);
            Glide.with(this)
                    .load(urlOrUri)
                    .circleCrop()
                    .placeholder(R.drawable.bg_avatar_circle)
                    .into(binding.ivProfilePhoto);
        } else {
            binding.ivProfilePhoto.setVisibility(View.GONE);
            binding.tvInitialsEdit.setVisibility(View.VISIBLE);
            binding.tvInitialsEdit.setText(currentUser != null ? currentUser.getInitials() : "?");
        }
    }

    private void populateAllSkillGroups() {
        setupSkillGroup(binding.groupLanguages, Constants.SKILLS_LANGUAGES);
        setupSkillGroup(binding.groupFrameworks, Constants.SKILLS_FRAMEWORKS);
        setupSkillGroup(binding.groupTools, Constants.SKILLS_TOOLS);
        setupSkillGroup(binding.groupAiMl, Constants.SKILLS_AI_ML);
        setupSkillGroup(binding.groupDesign, Constants.SKILLS_DESIGN);
        setupSkillGroup(binding.groupDatabases, Constants.SKILLS_DATABASES);
    }

    private void setupSkillGroup(ChipGroup group, List<String> skills) {
        group.removeAllViews();
        
        // Set tag on the label view (TextView) above the group for identification during filtering
        if (group.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) group.getParent();
            int index = parent.indexOfChild(group);
            if (index > 0) {
                parent.getChildAt(index - 1).setTag(group.getId() + "_label");
            }
        }

        for (String skill : skills) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_skill_chip_clickable, group, false);
            chip.setText(skill);
            chip.setCheckable(true);
            chip.setChecked(selectedSkills.contains(skill));
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedSkills.contains(skill)) selectedSkills.add(skill);
                } else {
                    selectedSkills.remove(skill);
                }
                updateSelectionCountLabel();
            });
            group.addView(chip);
        }
    }

    private void updateSkillSelectionUI() {
        updateGroupSelection(binding.groupLanguages);
        updateGroupSelection(binding.groupFrameworks);
        updateGroupSelection(binding.groupTools);
        updateGroupSelection(binding.groupAiMl);
        updateGroupSelection(binding.groupDesign);
        updateGroupSelection(binding.groupDatabases);
        updateSelectionCountLabel();
    }

    private void updateGroupSelection(ChipGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip) group.getChildAt(i);
            chip.setChecked(selectedSkills.contains(chip.getText().toString()));
        }
    }

    private void updateSelectionCountLabel() {
        binding.tvSelectionCount.setText(selectedSkills.size() + " skills selected");
    }

    private void filterSkills(String query) {
        String lowerQuery = query.toLowerCase().trim();
        filterGroup(binding.groupLanguages, lowerQuery);
        filterGroup(binding.groupFrameworks, lowerQuery);
        filterGroup(binding.groupTools, lowerQuery);
        filterGroup(binding.groupAiMl, lowerQuery);
        filterGroup(binding.groupDesign, lowerQuery);
        filterGroup(binding.groupDatabases, lowerQuery);
    }

    private void filterGroup(ChipGroup group, String query) {
        boolean anyVisible = false;
        for (int i = 0; i < group.getChildCount(); i++) {
            View chip = group.getChildAt(i);
            String text = ((Chip) chip).getText().toString().toLowerCase();
            if (text.contains(query)) {
                chip.setVisibility(View.VISIBLE);
                anyVisible = true;
            } else {
                chip.setVisibility(View.GONE);
            }
        }
        // Optionally hide the header TextView above the group if no chips are visible
        ViewParent parent = group.getParent();
        if (parent instanceof View) {
            View label = ((View) parent).findViewWithTag(group.getId() + "_label");
            if (label != null) label.setVisibility(anyVisible ? View.VISIBLE : View.GONE);
        }
    }

    private void checkPermissionAndPick() {
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 
                ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            launchImagePicker();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void startUpdateFlow() {
        String name = binding.etName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.etName.setError("Name is required");
            return;
        }

        showLoading(true);
        if (pendingImageUri != null) {
            uploadToCloudinary();
        } else {
            saveToFirestore(null);
        }
    }

    private void uploadToCloudinary() {
        MediaManager.get().upload(pendingImageUri)
                .unsigned("skillsphere_upload")
                .option("resource_type", "image")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) { Log.d(TAG, "Upload Started"); }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String secureUrl = (String) resultData.get("secure_url");
                        saveToFirestore(secureUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        showLoading(false);
                        Log.e(TAG, "Cloudinary Error: " + error.getDescription());
                        Snackbar.make(binding.getRoot(), "Upload failed: " + error.getDescription(), Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { showLoading(false); }
                }).dispatch();
    }

    private void saveToFirestore(String photoUrl) {
        if (auth.getCurrentUser() == null) return;

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", binding.etName.getText().toString().trim());
        userUpdates.put("username", binding.etUsername.getText().toString().trim());
        userUpdates.put("phone", binding.etPhone.getText().toString().trim());
        userUpdates.put("bio", binding.etBio.getText().toString().trim());
        userUpdates.put("university", binding.etUniversity.getText().toString().trim());
        userUpdates.put("department", binding.etDepartment.getText().toString().trim());
        userUpdates.put("year", binding.etYear.getText().toString().trim());
        userUpdates.put("location", binding.etLocation.getText().toString().trim());
        userUpdates.put("linkedin", binding.etLinkedin.getText().toString().trim());
        userUpdates.put("skills", selectedSkills);
        
        if (photoUrl != null) {
            userUpdates.put("photoUrl", photoUrl);
        }

        db.collection(Constants.COLLECTION_USERS)
                .document(auth.getCurrentUser().getUid())
                .update(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSaveProfile.setEnabled(!isLoading);
        binding.fabEditPhoto.setEnabled(!isLoading);
    }
}
